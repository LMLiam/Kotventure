'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { describe, test } = require('node:test');
const { MAX_RESULT_BYTES } = require('../actions/pr-metrics-comment/lib/metrics-result.js');
const { extractMetricsResultArchive } = require('./pr-metrics-publisher-archive.js');
const {
    MAX_ARTIFACT_BYTES,
    RESULT_FILE_NAME,
} = require('./pr-metrics-publisher-contract.js');
const {
    downloadMetricsArtifact,
    readMetricsArtifact,
} = require('./pr-metrics-publisher-storage.js');
const {
    PublicationRejectedError,
    selectMetricsArtifact,
    validateResultProvenance,
    validateWorkflowSource,
} = require('./pr-metrics-publisher-validation.js');
const {
    publishMetrics,
    resolveSource,
    validateSource,
} = require('./pr-metrics-publisher.js');
const {
    ARTIFACT_API_URL,
    ARTIFACT_ID,
    ARTIFACT_STORAGE_URL,
    HEAD_SHA,
    makeArtifact,
    makeArtifactFetch,
    makeDownloadOptions,
    makeGithub,
    makeInputs,
    makeResult,
    makeSource,
    makeTempDirectory,
    makeWorkflowRunContext,
    makeZip,
} = require('./pr-metrics-publisher-test-fixtures.js');

function assertRejected(callback, message) {
    assert.throws(callback, PublicationRejectedError, message);
}

function makeReadableBody(chunks) {
    let index = 0;
    let cancelled = false;

    return {
        body: {
            getReader() {
                return {
                    read: async () => {
                        if (index >= chunks.length) return { done: true };

                        return {
                            done: false,
                            value: chunks[index++],
                        };
                    },
                    cancel: async () => {
                        cancelled = true;
                    },
                };
            },
        },
        wasCancelled: () => cancelled,
    };
}

function assertArtifactRequests(requests) {
    assert.equal(requests.length, 2);

    const [apiRequest, storageRequest] = requests;
    const downloadUrl = `${ARTIFACT_API_URL}/repos/LMLiam/Kotventure/actions/artifacts/${ARTIFACT_ID}/zip`;

    assert.equal(apiRequest.location, downloadUrl);
    assert.equal(apiRequest.options.redirect, 'manual');
    assert.equal(apiRequest.options.headers.authorization, 'Bearer test-token');
    assert.equal(typeof apiRequest.options.signal?.aborted, 'boolean');

    assert.equal(storageRequest.location, ARTIFACT_STORAGE_URL);
    assert.equal(storageRequest.options.redirect, 'follow');
    assert.equal(typeof storageRequest.options.signal?.aborted, 'boolean');
}

describe('workflow source validation', () => {
    test('accepts a matching workflow, run, repository, and pull request', () => {
        assert.deepEqual(
                validateWorkflowSource(makeInputs()),
                makeSource(),
        );
    });

    test('rejects mismatched trusted workflow and pull-request identity', () => {
        const cases = [
            ['event run id', (inputs) => { inputs.eventRun.id = 101; }],
            ['event type', (inputs) => { inputs.eventRun.event = 'push'; }],
            ['event run attempt', (inputs) => { inputs.eventRun.run_attempt = 3; }],
            ['event head SHA', (inputs) => { inputs.eventRun.head_sha = 'c'.repeat(40); }],
            ['event workflow id', (inputs) => { inputs.eventRun.workflow_id = 56; }],
            ['event status', (inputs) => { inputs.eventRun.status = 'in_progress'; }],
            ['event conclusion', (inputs) => { inputs.eventRun.conclusion = 'failure'; }],
            ['run event', (inputs) => { inputs.run.event = 'push'; }],
            ['run status', (inputs) => { inputs.run.status = 'in_progress'; }],
            ['run conclusion', (inputs) => { inputs.run.conclusion = 'failure'; }],
            ['run repository', (inputs) => {
                inputs.run.repository = {
                    ...inputs.run.repository,
                    full_name: 'other/repo',
                };
            }],
            ['run repository id', (inputs) => {
                inputs.run.repository = {
                    ...inputs.run.repository,
                    id: 3,
                };
            }],
            ['workflow id', (inputs) => { inputs.workflow.id = 56; }],
            ['workflow name', (inputs) => { inputs.workflow.name = 'Untrusted CI'; }],
            ['workflow path', (inputs) => { inputs.workflow.path = '.github/workflows/untrusted.yml'; }],
            ['run pull request', (inputs) => { inputs.run.pull_requests[0].number = 43; }],
            ['resolved pull request', (inputs) => { inputs.pullNumber = 43; }],
            ['pull request state', (inputs) => { inputs.pullRequest.state = 'closed'; }],
            ['base repository', (inputs) => {
                inputs.pullRequest.base.repo = {
                    ...inputs.pullRequest.base.repo,
                    full_name: 'other/repo',
                };
            }],
            ['base repository id', (inputs) => {
                inputs.pullRequest.base.repo = {
                    ...inputs.pullRequest.base.repo,
                    id: 3,
                };
            }],
            ['base branch', (inputs) => { inputs.pullRequest.base.ref = 'release'; }],
            ['head repository', (inputs) => {
                inputs.run.head_repository = {
                    ...inputs.run.head_repository,
                    full_name: 'other/repo',
                };
            }],
            ['head repository id', (inputs) => {
                inputs.run.head_repository = {
                    ...inputs.run.head_repository,
                    id: 3,
                };
            }],
            ['head branch', (inputs) => { inputs.run.head_branch = 'other/branch'; }],
            ['head SHA', (inputs) => { inputs.pullRequest.head.sha = 'c'.repeat(40); }],
            ['run attempt', (inputs) => { inputs.run.run_attempt = 3; }],
            ['run workflow id', (inputs) => { inputs.run.workflow_id = 56; }],
        ];

        for (const [name, change] of cases) {
            const inputs = structuredClone(makeInputs());
            change(inputs);

            assertRejected(
                    () => validateWorkflowSource(inputs),
                    name,
            );
        }
    });

    test('resolves a pull request through the head commit when the run omits it', async () => {
        const inputs = makeInputs();
        inputs.run.pull_requests = [];

        const source = await resolveSource({
            github: makeGithub({
                run: inputs.run,
                associatedPullRequests: [
                    {
                        number: 42,
                    },
                ],
                artifacts: [
                    makeArtifact(),
                ],
            }),
            context: makeWorkflowRunContext(inputs.run),
        });

        assert.equal(source.pullRequest, 42);
        assert.equal(source.artifactName, 'pr-metrics-result-100-2');
    });
});

describe('metrics artifact validation', () => {
    test('selects only the exact current artifact', () => {
        const source = makeSource();
        const artifact = makeArtifact(source);

        assert.equal(
                selectMetricsArtifact({
                    artifacts: [artifact],
                    source,
                }).id,
                artifact.id,
        );

        const changes = [
            (value) => {
                value.expired = true;
            },
            (value) => {
                value.size_in_bytes = MAX_ARTIFACT_BYTES + 1;
            },
            (value) => {
                value.workflow_run.id = 101;
            },
            (value) => {
                value.workflow_run.head_sha = 'c'.repeat(40);
            },
        ];

        for (const change of changes) {
            const changed = structuredClone(artifact);
            change(changed);

            assertRejected(() => {
                selectMetricsArtifact({
                    artifacts: [changed],
                    source,
                });
            });
        }

        assertRejected(() => {
            selectMetricsArtifact({
                artifacts: [],
                source,
            });
        });

        assertRejected(() => {
            selectMetricsArtifact({
                artifacts: [
                    artifact,
                    structuredClone(artifact),
                ],
                source,
            });
        });
    });

    test('accepts only result provenance for the validated source', () => {
        const source = makeSource();
        const result = makeResult();

        assert.equal(
                validateResultProvenance(result, source),
                result,
        );

        const replacements = {
            runId: 101,
            runAttempt: 3,
            pullRequest: 43,
            baseSha: 'c'.repeat(40),
            headSha: 'c'.repeat(40),
        };

        for (const [key, value] of Object.entries(replacements)) {
            const changed = structuredClone(result);
            changed.provenance[key] = value;

            assertRejected(() => {
                validateResultProvenance(changed, source);
            });
        }
    });
});

describe('metrics archive extraction', () => {
    test('extracts stored and deflated result archives', () => {
        const content = Buffer.from(
                JSON.stringify(makeResult()),
                'utf8',
        );

        for (const compressionMethod of [0, 8]) {
            assert.deepEqual(
                    extractMetricsResultArchive(
                            makeZip(content, {
                                compressionMethod,
                            }),
                    ),
                    content,
            );
        }
    });

    test('rejects an unexpected archive entry name', () => {
        assert.throws(
                () => {
                    extractMetricsResultArchive(
                            makeZip('{}', {
                                fileName: 'other.json',
                            }),
                    );
                },
                /must contain only/,
        );
    });

    test('rejects an entry that expands beyond the result limit', () => {
        const archive = makeZip(
                Buffer.alloc(MAX_RESULT_BYTES + 1, 0x78),
        );

        assert.ok(archive.length < MAX_ARTIFACT_BYTES);

        assert.throws(
                () => extractMetricsResultArchive(archive),
                /outside the size limit/,
        );
    });

    test('rejects an archive larger than the artifact limit before parsing', () => {
        assert.throws(
                () => extractMetricsResultArchive(
                        Buffer.alloc(MAX_ARTIFACT_BYTES + 1),
                ),
                /is not a ZIP archive/,
        );
    });
});

describe('metrics artifact storage', () => {
    test('downloads through the GitHub redirect and writes the validated result', async (t) => {
        const directory = makeTempDirectory(
                t,
                'pr-metrics-download-',
        );

        const archive = makeZip(
                Buffer.from(
                        JSON.stringify(makeResult()),
                        'utf8',
                ),
        );

        const artifactFetch = makeArtifactFetch({
            ok: true,
            headers: {
                'content-length': String(archive.length),
            },
            arrayBuffer: async () => archive,
        });

        const filePath = await downloadMetricsArtifact(
                makeDownloadOptions(directory, artifactFetch.fetchImpl),
        );

        assertArtifactRequests(artifactFetch.requests);

        assert.equal(
                filePath,
                path.join(directory, RESULT_FILE_NAME),
        );

        assert.equal(
                readMetricsArtifact(directory).metrics.headJars[0].module,
                'core',
        );
    });

    test('downloads through a bounded streaming response', async (t) => {
        const directory = makeTempDirectory(
                t,
                'pr-metrics-stream-',
        );

        const archive = makeZip(
                Buffer.from(
                        JSON.stringify(makeResult()),
                        'utf8',
                ),
        );
        const readable = makeReadableBody([
            archive.subarray(0, 1),
            archive.subarray(1),
        ]);
        const artifactFetch = makeArtifactFetch({
            ok: true,
            headers: {},
            body: readable.body,
        });

        await downloadMetricsArtifact(
                makeDownloadOptions(directory, artifactFetch.fetchImpl),
        );

        assert.equal(
                readMetricsArtifact(directory).metrics.headJars[0].module,
                'core',
        );
        assertArtifactRequests(artifactFetch.requests);
        assert.equal(readable.wasCancelled(), false);
    });

    test('cancels an oversized streaming response', async (t) => {
        const directory = makeTempDirectory(
                t,
                'pr-metrics-oversized-stream-',
        );
        const readable = makeReadableBody([
            Buffer.alloc(MAX_ARTIFACT_BYTES + 1),
        ]);
        const artifactFetch = makeArtifactFetch({
            ok: true,
            headers: {},
            body: readable.body,
        });

        await assert.rejects(
                () => downloadMetricsArtifact(
                        makeDownloadOptions(directory, artifactFetch.fetchImpl),
                ),
                new RegExp(`exceeds ${MAX_ARTIFACT_BYTES} bytes`),
        );

        assertArtifactRequests(artifactFetch.requests);
        assert.equal(readable.wasCancelled(), true);
    });

    test('cancels when a streaming response returns an invalid chunk', async (t) => {
        const directory = makeTempDirectory(
                t,
                'pr-metrics-invalid-stream-',
        );
        const readable = makeReadableBody([null]);
        const artifactFetch = makeArtifactFetch({
            ok: true,
            headers: {},
            body: readable.body,
        });

        await assert.rejects(
                () => downloadMetricsArtifact(
                        makeDownloadOptions(directory, artifactFetch.fetchImpl),
                ),
                /returned an invalid body/,
        );

        assertArtifactRequests(artifactFetch.requests);
        assert.equal(readable.wasCancelled(), true);
    });

    test('reads one regular, bounded, valid result file', (t) => {
        const directory = makeTempDirectory(
                t,
                'pr-metrics-result-',
        );

        fs.writeFileSync(
                path.join(directory, RESULT_FILE_NAME),
                JSON.stringify(makeResult()),
        );

        assert.equal(
                readMetricsArtifact(directory).metrics.headJars[0].module,
                'core',
        );
    });

    test('rejects invalid artifact directory contents', async (t) => {
        const cases = [
            [
                'missing result',
                () => {},
            ],
            [
                'unexpected file',
                (directory) => {
                    fs.writeFileSync(
                            path.join(directory, 'other.json'),
                            '{}',
                    );
                },
            ],
            [
                'malformed JSON',
                (directory) => {
                    fs.writeFileSync(
                            path.join(directory, RESULT_FILE_NAME),
                            '{',
                    );
                },
            ],
            [
                'oversized result',
                (directory) => {
                    fs.writeFileSync(
                            path.join(directory, RESULT_FILE_NAME),
                            'x'.repeat(MAX_RESULT_BYTES + 1),
                    );
                },
            ],
            [
                'symbolic link',
                (directory) => {
                    const target = path.join(
                            directory,
                            'target.json',
                    );

                    fs.writeFileSync(
                            target,
                            JSON.stringify(makeResult()),
                    );

                    fs.symlinkSync(
                            target,
                            path.join(directory, RESULT_FILE_NAME),
                    );
                },
            ],
        ];

        for (const [name, setup] of cases) {
            await t.test(name, (subtest) => {
                const directory = makeTempDirectory(
                        subtest,
                        'pr-metrics-invalid-',
                );

                setup(directory);

                assert.throws(
                        () => readMetricsArtifact(directory),
                );
            });
        }
    });
});

describe('publisher orchestration', () => {
    test('sets trusted source outputs when publication is allowed', async () => {
        const inputs = makeInputs();
        const outputs = new Map();

        await validateSource({
            github: makeGithub({
                run: inputs.run,
                artifacts: [
                    makeArtifact(),
                ],
            }),
            context: makeWorkflowRunContext(inputs.run),
            core: {
                warning: () => {},
                setOutput: (name, value) => {
                    outputs.set(name, value);
                },
            },
        });

        assert.equal(outputs.get('publish'), 'true');
        assert.equal(outputs.get('artifact_id'), '700');
        assert.equal(outputs.get('pull_number'), '42');
        assert.equal(outputs.get('head_sha'), HEAD_SHA);
    });

    test('sets publish=false when source validation rejects publication', async () => {
        const inputs = makeInputs();
        const warnings = [];
        const outputs = new Map();

        await validateSource({
            github: makeGithub({
                run: inputs.run,
                pullRequest: {
                    ...inputs.pullRequest,
                    state: 'closed',
                },
            }),
            context: makeWorkflowRunContext(inputs.run),
            core: {
                warning: (message) => {
                    warnings.push(message);
                },
                setOutput: (name, value) => {
                    outputs.set(name, value);
                },
            },
        });

        assert.equal(outputs.get('publish'), 'false');
        assert.equal(warnings.length, 1);
        assert.match(
                warnings[0],
                /Metrics publication skipped/,
        );
    });

    test('skips comment publication when the current PR no longer matches the source run', async () => {
        const inputs = makeInputs();
        const warnings = [];

        const published = await publishMetrics({
            github: makeGithub({
                run: inputs.run,
                pullRequest: {
                    ...inputs.pullRequest,
                    state: 'closed',
                },
            }),
            context: makeWorkflowRunContext(inputs.run),
            core: {
                warning: (message) => {
                    warnings.push(message);
                },
                info: () => {},
            },
            artifactDirectory: 'unused',
        });

        assert.equal(published, false);
        assert.equal(warnings.length, 1);
        assert.match(
                warnings[0],
                /Metrics publication skipped/,
        );
    });

    test('publishes a valid metrics result as a pull-request comment', async (t) => {
        const inputs = makeInputs();
        const source = makeSource();
        const directory = makeTempDirectory(
                t,
                'pr-metrics-publish-',
        );
        const publishedComments = [];

        fs.writeFileSync(
                path.join(directory, RESULT_FILE_NAME),
                JSON.stringify(makeResult(source)),
        );

        const github = makeGithub({
            run: inputs.run,
            pullRequest: inputs.pullRequest,
            artifacts: [
                makeArtifact(source),
            ],
        });
        const listComments = () => {};
        const paginate = github.paginate;

        github.rest.issues = {
            createComment: async (parameters) => {
                publishedComments.push(parameters);
            },
            listComments,
        };
        github.paginate = async (method, parameters) => {
            if (method === listComments) return [];

            return paginate(method, parameters);
        };

        const published = await publishMetrics({
            github,
            context: makeWorkflowRunContext(inputs.run),
            core: {
                warning: () => {},
                info: () => {},
            },
            artifactDirectory: directory,
        });

        assert.equal(published, true);
        assert.equal(publishedComments.length, 1);
        assert.equal(publishedComments[0].issue_number, 42);
        assert.match(
                publishedComments[0].body,
                /^<!-- pr-metrics -->\n## CI metrics/,
        );
    });
});
