'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { describe, test } = require('node:test');
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
    BASE_SHA,
    makeArtifact,
    makeGithub,
    makeInputs,
    makeResult,
    makeSource,
    makeTempDirectory,
    makeWorkflowRunContext,
    makeZip,
} = require('./pr-metrics-publisher-test-fixtures.js');

function assertRejected(callback) {
    assert.throws(callback, PublicationRejectedError);
}

describe('workflow source validation', () => {
    test('accepts a matching workflow, run, repository, and pull request', () => {
        const source = makeSource();

        assert.equal(source.runId, 100);
        assert.equal(source.runAttempt, 2);
        assert.equal(source.repositoryId, 1);
        assert.equal(source.headRepositoryId, 2);
        assert.equal(source.baseSha, BASE_SHA);
    });

    test('rejects mismatched trusted workflow and pull-request identity', () => {
        const changes = [
            (inputs) => {
                inputs.eventRun.conclusion = 'failure';
            },
            (inputs) => {
                inputs.run.run_attempt = 3;
            },
            (inputs) => {
                inputs.run.workflow_id = 56;
            },
            (inputs) => {
                inputs.workflow.name = 'Untrusted CI';
            },
            (inputs) => {
                inputs.pullRequest.base.ref = 'release';
            },
            (inputs) => {
                inputs.pullRequest.head.sha = 'c'.repeat(40);
            },
            (inputs) => {
                inputs.run.head_repository = {
                    full_name: 'other/repo',
                    id: 3,
                };
            },
        ];

        for (const change of changes) {
            const inputs = structuredClone(makeInputs());
            change(inputs);

            assertRejected(() => validateWorkflowSource(inputs));
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
                    makeArtifact(makeSource(inputs)),
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
                Buffer.alloc(64 * 1024 + 1, 0x78),
        );

        assert.ok(archive.length < MAX_ARTIFACT_BYTES);

        assert.throws(
                () => extractMetricsResultArchive(archive),
                /outside the size limit/,
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

        const apiUrl = 'https://api.github.test';

        const filePath = await downloadMetricsArtifact({
            owner: 'LMLiam',
            repo: 'Kotventure',
            artifactId: 700,
            outputDirectory: directory,
            apiUrl,
            token: 'test-token',
            fetchImpl: async (location, options) => {
                const downloadUrl = `${apiUrl}/repos/LMLiam/Kotventure/actions/artifacts/700/zip`;

                if (location === downloadUrl) {
                    assert.equal(options.redirect, 'manual');
                    assert.equal(
                            options.headers.authorization,
                            'Bearer test-token',
                    );

                    return {
                        status: 302,
                        headers: {
                            location: 'https://artifact.example/result.zip',
                        },
                    };
                }

                assert.equal(
                        location,
                        'https://artifact.example/result.zip',
                );

                assert.equal(options.redirect, 'follow');

                return {
                    ok: true,
                    headers: {
                        'content-length': String(archive.length),
                    },
                    arrayBuffer: async () => archive,
                };
            },
        });

        assert.equal(
                filePath,
                path.join(directory, RESULT_FILE_NAME),
        );

        assert.equal(
                readMetricsArtifact(directory).metrics.headJars[0].module,
                'core',
        );
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
                            'x'.repeat(64 * 1024 + 1),
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
                    makeArtifact(makeSource(inputs)),
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

        await publishMetrics({
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

        assert.equal(warnings.length, 1);
        assert.match(
                warnings[0],
                /Metrics publication skipped/,
        );
    });
});
