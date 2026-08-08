'use strict';

const {
    validateMetricsResult,
} = require('../actions/pr-metrics-comment/lib/metrics-result.js');
const {
    EXPECTED_WORKFLOW_PATH,
    MAX_ARTIFACT_BYTES,
    RESULT_ARTIFACT_PREFIX,
    WORKFLOW_NAME,
} = require('./pr-metrics-publisher-contract.js');

class PublicationRejectedError extends Error {
    constructor(message) {
        super(message);
        this.name = 'PublicationRejectedError';
    }
}

function reject(message) {
    throw new PublicationRejectedError(message);
}

function requireEqual(actual, expected, label) {
    if (actual !== expected) {
        reject(`${label} does not match the trusted value`);
    }

    return actual;
}

function requireSafeInteger(
        value,
        label,
        minimum = 1,
        maximum = Number.MAX_SAFE_INTEGER
) {
    if (!Number.isSafeInteger(value) || value < minimum || value > maximum) {
        reject(`${label} is invalid`);
    }

    return value;
}

function requireObject(value, label) {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
        reject(`${label} is missing`);
    }

    return value;
}

function requireString(value, label) {
    if (typeof value !== 'string' || value.length === 0) {
        reject(`${label} is invalid`);
    }

    return value;
}

function expectedArtifactName({ runId, runAttempt }) {
    const id = requireSafeInteger(runId, 'workflow run id');
    const attempt = requireSafeInteger(runAttempt, 'workflow run attempt');

    return `${RESULT_ARTIFACT_PREFIX}${id}-${attempt}`;
}

function validateWorkflowSource({
    eventRun,
    run,
    workflow,
    repository,
    pullRequest,
    pullNumber,
}) {
    requireObject(eventRun, 'workflow_run event');
    requireObject(run, 'workflow run');
    requireObject(workflow, 'workflow');
    requireObject(repository, 'repository');
    requireObject(pullRequest, 'pull request');

    const runId = requireSafeInteger(run.id, 'workflow run id');
    const runAttempt = requireSafeInteger(run.run_attempt, 'workflow run attempt');
    const workflowId = requireSafeInteger(run.workflow_id, 'workflow id');

    const repositoryId = requireSafeInteger(repository.id, 'repository id');
    const repositoryName = requireString(repository.full_name, 'repository name');
    const defaultBranch = requireString(
            repository.default_branch,
            'repository default branch'
    );

    const pullRequestNumber = requireSafeInteger(
            pullRequest.number,
            'pull request number'
    );

    const resolvedPullNumber = requireSafeInteger(
            pullNumber,
            'resolved pull request number'
    );

    requireEqual(eventRun.id, runId, 'workflow run id');
    requireEqual(eventRun.run_attempt, runAttempt, 'workflow run attempt');
    requireEqual(eventRun.head_sha, run.head_sha, 'workflow run head SHA');

    if (eventRun.workflow_id != null) {
        requireEqual(
                eventRun.workflow_id,
                workflowId,
                'workflow run workflow id'
        );
    }

    requireEqual(eventRun.event, 'pull_request', 'workflow run event');
    requireEqual(eventRun.status, 'completed', 'workflow run event status');
    requireEqual(eventRun.conclusion, 'success', 'workflow run event conclusion');

    requireEqual(run.event, 'pull_request', 'workflow run event');
    requireEqual(run.status, 'completed', 'workflow run status');
    requireEqual(run.conclusion, 'success', 'workflow run conclusion');

    requireEqual(
            run.repository?.full_name,
            repositoryName,
            'workflow run repository',
    );

    requireEqual(
            run.repository?.id,
            repositoryId,
            'workflow run repository id',
    );

    requireEqual(workflow.id, workflowId, 'workflow identity');
    requireEqual(workflow.name, WORKFLOW_NAME, 'workflow name');
    requireEqual(workflow.path, EXPECTED_WORKFLOW_PATH, 'workflow path');

    if (!Array.isArray(run.pull_requests) || run.pull_requests.length > 1) {
        reject('workflow run must identify at most one pull request');
    }

    if (run.pull_requests.length === 1) {
        requireEqual(
                run.pull_requests[0].number,
                pullRequestNumber,
                'workflow run pull request number',
        );
    }

    requireEqual(
            resolvedPullNumber,
            pullRequestNumber,
            'pull request number',
    );

    requireEqual(pullRequest.state, 'open', 'pull request state');

    const baseRepository = requireObject(
            pullRequest.base?.repo,
            'pull request base repository',
    );

    const headRepository = requireObject(
            pullRequest.head?.repo,
            'pull request head repository',
    );

    const baseRepositoryName = requireString(
            baseRepository.full_name,
            'pull request base repository',
    );

    const baseRepositoryId = requireSafeInteger(
            baseRepository.id,
            'pull request base repository id',
    );

    const baseRef = requireString(
            pullRequest.base?.ref,
            'pull request base branch',
    );

    const baseSha = requireString(
            pullRequest.base?.sha,
            'pull request base SHA',
    );

    const headRepositoryName = requireString(
            headRepository.full_name,
            'pull request head repository',
    );

    const headRepositoryId = requireSafeInteger(
            headRepository.id,
            'pull request head repository id',
    );

    const headRef = requireString(
            pullRequest.head?.ref,
            'pull request head branch',
    );

    const headSha = requireString(
            pullRequest.head?.sha,
            'pull request head SHA',
    );

    requireEqual(
            baseRepositoryName,
            repositoryName,
            'pull request base repository',
    );

    requireEqual(
            baseRepositoryId,
            repositoryId,
            'pull request base repository id',
    );

    requireEqual(
            baseRef,
            defaultBranch,
            'pull request base branch',
    );

    requireEqual(
            run.head_repository?.full_name,
            headRepositoryName,
            'workflow head repository',
    );

    requireEqual(
            run.head_repository?.id,
            headRepositoryId,
            'workflow head repository id',
    );

    requireEqual(
            run.head_branch,
            headRef,
            'workflow head branch',
    );

    requireEqual(
            run.head_sha,
            headSha,
            'pull request head SHA',
    );

    return {
        repository: repositoryName,
        repositoryId,
        workflow: WORKFLOW_NAME,
        event: 'pull_request',
        runId,
        runAttempt,
        pullRequest: pullRequestNumber,
        baseRepository: baseRepositoryName,
        baseRepositoryId,
        baseRef,
        baseSha,
        headRepository: headRepositoryName,
        headRepositoryId,
        headRef,
        headSha,
    };
}

function selectMetricsArtifact({ artifacts, source }) {
    if (!Array.isArray(artifacts)) {
        reject('workflow artifacts are missing');
    }

    const name = expectedArtifactName(source);
    const matches = artifacts.filter((artifact) => artifact?.name === name);

    if (matches.length !== 1) {
        reject(`expected exactly one metrics artifact, found ${matches.length}`);
    }

    const [artifact] = matches;

    requireSafeInteger(artifact.id, 'metrics artifact id');

    if (artifact.expired !== false) {
        reject('metrics artifact is expired');
    }

    requireSafeInteger(
            artifact.size_in_bytes,
            'metrics artifact size',
            1,
            MAX_ARTIFACT_BYTES,
    );

    const workflowRun = requireObject(
            artifact.workflow_run,
            'metrics artifact workflow run',
    );

    requireEqual(
            workflowRun.id,
            source.runId,
            'metrics artifact workflow run id',
    );

    requireEqual(
            workflowRun.repository_id,
            source.repositoryId,
            'metrics artifact repository id',
    );

    requireEqual(
            workflowRun.head_repository_id,
            source.headRepositoryId,
            'metrics artifact head repository id',
    );

    requireEqual(
            workflowRun.head_branch,
            source.headRef,
            'metrics artifact head branch',
    );

    requireEqual(
            workflowRun.head_sha,
            source.headSha,
            'metrics artifact head SHA',
    );

    return artifact;
}

function validateResultProvenance(result, source) {
    let validatedResult;

    try {
        validatedResult = validateMetricsResult(result);
    } catch (error) {
        const message = error instanceof Error ? error.message : String(error);
        reject(`metrics result is invalid: ${message}`);
    }

    const expected = {
        repository: source.repository,
        workflow: source.workflow,
        event: source.event,
        runId: source.runId,
        runAttempt: source.runAttempt,
        pullRequest: source.pullRequest,
        baseRepository: source.baseRepository,
        baseRef: source.baseRef,
        baseSha: source.baseSha,
        headRepository: source.headRepository,
        headRef: source.headRef,
        headSha: source.headSha,
    };

    for (const [key, value] of Object.entries(expected)) {
        requireEqual(
                validatedResult.provenance[key],
                value,
                `metrics result provenance ${key}`,
        );
    }

    return validatedResult;
}

module.exports = {
    PublicationRejectedError,
    expectedArtifactName,
    selectMetricsArtifact,
    validateResultProvenance,
    validateWorkflowSource,
};
