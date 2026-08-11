'use strict';

const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const AdmZip = require('adm-zip');
const { serializeMetricsResult } = require('../actions/pr-metrics-comment/lib/metrics-result.js');
const {
    RESULT_ARTIFACT_PREFIX,
    RESULT_FILE_NAME,
} = require('./pr-metrics-publisher-contract.js');

const BASE_SHA = 'a'.repeat(40);
const HEAD_SHA = 'b'.repeat(40);
const ARTIFACT_ID = 700;
const ARTIFACT_API_URL = 'https://api.github.test';
const ARTIFACT_STORAGE_URL = 'https://artifact.example/result.zip';
const TEST_TOKEN = 'test-token';

const REPOSITORY = Object.freeze({
    full_name: 'LMLiam/Kotventure',
    id: 1,
    default_branch: 'master',
});

const HEAD_REPOSITORY = Object.freeze({
    full_name: 'LMLiam/Kotventure-fix',
    id: 2,
});

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

function makeSource(overrides = {}) {
    return {
        repository: REPOSITORY.full_name,
        repositoryId: REPOSITORY.id,
        workflow: 'CI',
        event: 'pull_request',
        runId: 100,
        runAttempt: 2,
        pullRequest: 42,
        baseRepository: REPOSITORY.full_name,
        baseRepositoryId: REPOSITORY.id,
        baseRef: REPOSITORY.default_branch,
        baseSha: BASE_SHA,
        headRepository: HEAD_REPOSITORY.full_name,
        headRepositoryId: HEAD_REPOSITORY.id,
        headRef: 'fix/metrics',
        headSha: HEAD_SHA,
        ...overrides,
    };
}

function makeArtifact(source = makeSource()) {
    return {
        id: ARTIFACT_ID,
        name: `${RESULT_ARTIFACT_PREFIX}${source.runId}-${source.runAttempt}`,
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
    const zip = new AdmZip();
    const entry = zip.addFile(
            fileName,
            Buffer.isBuffer(content) ? content : Buffer.from(content, 'utf8'),
    );
    if (compressionMethod === 0) entry.header.method = 0;
    return zip.toBuffer();
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
            if (method === listAssociatedPullRequests) return associatedPullRequests;

            if (method === listWorkflowRunArtifacts) return artifacts;

            throw new Error('unexpected pagination method');
        },
    };
}

function makeResult(source = makeSource()) {
    return serializeMetricsResult({
        context: {
            repo: {
                owner: 'LMLiam',
                repo: 'Kotventure',
            },
            eventName: 'pull_request',
            payload: {
                pull_request: {
                    number: source.pullRequest,
                    base: {
                        repo: {
                            full_name: source.baseRepository,
                        },
                        ref: source.baseRef,
                        sha: source.baseSha,
                    },
                    head: {
                        repo: {
                            full_name: source.headRepository,
                        },
                        ref: source.headRef,
                        sha: source.headSha,
                    },
                },
            },
        },
        runId: String(source.runId),
        runAttempt: String(source.runAttempt),
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

function makeArtifactFetch(archiveResponse) {
    const requests = [];
    const downloadUrl = `${ARTIFACT_API_URL}/repos/LMLiam/Kotventure/actions/artifacts/${ARTIFACT_ID}/zip`;

    return {
        requests,
        fetchImpl: async (location, options) => {
            requests.push({
                location,
                options,
            });

            if (location === downloadUrl) {
                return {
                    status: 302,
                    headers: {
                        location: ARTIFACT_STORAGE_URL,
                    },
                };
            }

            if (location === ARTIFACT_STORAGE_URL) return archiveResponse;

            throw new Error(`unexpected artifact request: ${location}`);
        },
    };
}

function makeDownloadOptions(outputDirectory, fetchImpl) {
    return {
        owner: 'LMLiam',
        repo: 'Kotventure',
        artifactId: ARTIFACT_ID,
        outputDirectory,
        apiUrl: ARTIFACT_API_URL,
        token: TEST_TOKEN,
        fetchImpl,
    };
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
    ARTIFACT_API_URL,
    ARTIFACT_ID,
    ARTIFACT_STORAGE_URL,
    BASE_SHA,
    HEAD_SHA,
    HEAD_REPOSITORY,
    REPOSITORY,
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
};
