'use strict';

const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const zlib = require('node:zlib');
const { serializeMetricsResult } = require('../actions/pr-metrics-comment/lib/metrics-result.js');
const { RESULT_FILE_NAME } = require('./pr-metrics-publisher-contract.js');
const {
    expectedArtifactName,
    validateWorkflowSource,
} = require('./pr-metrics-publisher-validation.js');

const BASE_SHA = 'a'.repeat(40);
const HEAD_SHA = 'b'.repeat(40);

const REPOSITORY = {
    full_name: 'LMLiam/Kotventure',
    id: 1,
    default_branch: 'master',
};

const HEAD_REPOSITORY = {
    full_name: 'LMLiam/Kotventure-fix',
    id: 2,
};

function makeInputs() {
    return {
        eventRun: {
            id: 100,
            run_attempt: 2,
            workflow_id: 55,
            event: 'pull_request',
            status: 'completed',
            conclusion: 'success',
            head_sha: HEAD_SHA,
        },
        run: {
            id: 100,
            run_attempt: 2,
            workflow_id: 55,
            event: 'pull_request',
            status: 'completed',
            conclusion: 'success',
            repository: REPOSITORY,
            pull_requests: [
                {
                    number: 42,
                },
            ],
            head_repository: HEAD_REPOSITORY,
            head_branch: 'fix/metrics',
            head_sha: HEAD_SHA,
        },
        workflow: {
            id: 55,
            name: 'CI',
            path: '.github/workflows/ci.yml',
        },
        repository: REPOSITORY,
        pullRequest: {
            number: 42,
            state: 'open',
            base: {
                repo: REPOSITORY,
                ref: 'master',
                sha: BASE_SHA,
            },
            head: {
                repo: HEAD_REPOSITORY,
                ref: 'fix/metrics',
                sha: HEAD_SHA,
            },
        },
        pullNumber: 42,
    };
}

function makeSource(inputs = makeInputs()) {
    return validateWorkflowSource(inputs);
}

function makeArtifact(source = makeSource()) {
    return {
        id: 700,
        name: expectedArtifactName(source),
        expired: false,
        size_in_bytes: 200,
        workflow_run: {
            id: source.runId,
            repository_id: source.repositoryId,
            head_repository_id: source.headRepositoryId,
            head_branch: source.headRef,
            head_sha: source.headSha,
        },
    };
}

function makeZip(
        content,
        {
            compressionMethod = 8,
            fileName = RESULT_FILE_NAME,
        } = {},
) {
    const name = Buffer.from(fileName, 'utf8');

    const uncompressed = Buffer.isBuffer(content)
            ? content
            : Buffer.from(content, 'utf8');

    const compressed = compressionMethod === 8
            ? zlib.deflateRawSync(uncompressed)
            : uncompressed;

    const local = Buffer.alloc(30 + name.length);

    local.writeUInt32LE(0x04034b50, 0);
    local.writeUInt16LE(20, 4);
    local.writeUInt16LE(compressionMethod, 8);
    local.writeUInt32LE(compressed.length, 18);
    local.writeUInt32LE(uncompressed.length, 22);
    local.writeUInt16LE(name.length, 26);

    name.copy(local, 30);

    const central = Buffer.alloc(46 + name.length);

    central.writeUInt32LE(0x02014b50, 0);
    central.writeUInt16LE(20, 4);
    central.writeUInt16LE(20, 6);
    central.writeUInt16LE(compressionMethod, 10);
    central.writeUInt32LE(compressed.length, 20);
    central.writeUInt32LE(uncompressed.length, 24);
    central.writeUInt16LE(name.length, 28);

    name.copy(central, 46);

    const end = Buffer.alloc(22);

    end.writeUInt32LE(0x06054b50, 0);
    end.writeUInt16LE(1, 8);
    end.writeUInt16LE(1, 10);
    end.writeUInt32LE(central.length, 12);
    end.writeUInt32LE(local.length + compressed.length, 16);

    return Buffer.concat([
        local,
        compressed,
        central,
        end,
    ]);
}

function makeWorkflowRunContext(run = makeInputs().run) {
    return {
        repo: {
            owner: 'LMLiam',
            repo: 'Kotventure',
        },
        serverUrl: 'https://github.com',
        payload: {
            workflow_run: {
                id: run.id,
                run_attempt: run.run_attempt,
                workflow_id: run.workflow_id,
                event: run.event,
                status: run.status,
                conclusion: run.conclusion,
                head_sha: run.head_sha,
            },
        },
    };
}

function makeGithub({
                        run = makeInputs().run,
                        associatedPullRequests = [],
                        artifacts = [],
                        pullRequest = makeInputs().pullRequest,
                    } = {}) {
    const listAssociatedPullRequests = () => {};
    const listWorkflowRunArtifacts = () => {};

    return {
        rest: {
            repos: {
                get: async () => ({
                    data: REPOSITORY,
                }),
                listPullRequestsAssociatedWithCommit: listAssociatedPullRequests,
            },
            actions: {
                getWorkflowRun: async () => ({
                    data: run,
                }),
                getWorkflow: async () => ({
                    data: {
                        id: 55,
                        name: 'CI',
                        path: '.github/workflows/ci.yml',
                    },
                }),
                listWorkflowRunArtifacts,
            },
            pulls: {
                get: async () => ({
                    data: pullRequest,
                }),
            },
        },
        paginate: async (method) => {
            if (method === listAssociatedPullRequests) {
                return associatedPullRequests;
            }

            if (method === listWorkflowRunArtifacts) {
                return artifacts;
            }

            throw new Error('unexpected pagination method');
        },
    };
}

function makeResult() {
    return serializeMetricsResult({
        context: {
            repo: {
                owner: 'LMLiam',
                repo: 'Kotventure',
            },
            eventName: 'pull_request',
            payload: {
                pull_request: {
                    number: 42,
                    base: {
                        repo: {
                            full_name: REPOSITORY.full_name,
                        },
                        ref: 'master',
                        sha: BASE_SHA,
                    },
                    head: {
                        repo: {
                            full_name: HEAD_REPOSITORY.full_name,
                        },
                        ref: 'fix/metrics',
                        sha: HEAD_SHA,
                    },
                },
            },
        },
        runId: '100',
        runAttempt: '2',
        headCoverage: null,
        baseCoverage: null,
        headJars: new Map([
            [
                'core',
                {
                    size: 1,
                    classes: 1,
                },
            ],
        ]),
        baseJars: new Map(),
        headMetrics: null,
        baseMetrics: null,
        patchCoverage: null,
        apiSurface: null,
    });
}

function makeTempDirectory(t, prefix) {
    const directory = fs.mkdtempSync(
            path.join(os.tmpdir(), prefix),
    );

    t.after(() => {
        fs.rmSync(directory, {
            recursive: true,
            force: true,
        });
    });

    return directory;
}

module.exports = {
    BASE_SHA,
    HEAD_SHA,
    HEAD_REPOSITORY,
    REPOSITORY,
    makeArtifact,
    makeGithub,
    makeInputs,
    makeResult,
    makeSource,
    makeTempDirectory,
    makeWorkflowRunContext,
    makeZip,
};
