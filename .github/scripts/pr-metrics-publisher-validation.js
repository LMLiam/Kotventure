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
const { createValidators } = require('./shared/validation.js');
const { validateArtifactBinding } = require('./shared/artifact-binding.js');

class PublicationRejectedError extends Error {
    constructor(message) {
        super(message);
        this.name = 'PublicationRejectedError';
    }
}

function reject(message) {
    throw new PublicationRejectedError(message);
}

const {
    requireBoundedInteger,
    requireEqual,
    requireObject,
    requireString,
} = createValidators(reject);

function expectedArtifactName({ runId, runAttempt }) {
    const id = requireBoundedInteger(runId, 'workflow run id');
    const attempt = requireBoundedInteger(runAttempt, 'workflow run attempt');

    return `${RESULT_ARTIFACT_PREFIX}${id}-${attempt}`;
}

function validateCompletedRun(eventRun, run) {
    const runId = requireBoundedInteger(run.id, 'workflow run id');
    const runAttempt = requireBoundedInteger(run.run_attempt, 'workflow run attempt');
    const workflowId = requireBoundedInteger(run.workflow_id, 'workflow id');

    requireEqual(eventRun.id, runId, 'workflow run id');
    requireEqual(eventRun.run_attempt, runAttempt, 'workflow run attempt');
    requireEqual(eventRun.head_sha, run.head_sha, 'workflow run head SHA');

    if (eventRun.workflow_id != null) {
        requireEqual(
                eventRun.workflow_id,
                workflowId,
                'workflow run workflow id',
        );
    }

    requireEqual(eventRun.event, 'pull_request', 'workflow run event');
    requireEqual(eventRun.status, 'completed', 'workflow run event status');
    requireEqual(eventRun.conclusion, 'success', 'workflow run event conclusion');

    requireEqual(run.event, 'pull_request', 'workflow run event');
    requireEqual(run.status, 'completed', 'workflow run status');
    requireEqual(run.conclusion, 'success', 'workflow run conclusion');

    return {
        runId,
        runAttempt,
        workflowId,
    };
}

function validateTrustedWorkflow(run, workflow, repository, workflowId) {
    const repositoryId = requireBoundedInteger(repository.id, 'repository id');
    const repositoryName = requireString(repository.full_name, 'repository name');
    const defaultBranch = requireString(
            repository.default_branch,
            'repository default branch',
    );

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

    return {
        repository: repositoryName,
        repositoryId,
        defaultBranch,
    };
}

function validateCurrentPullRequest({
    run,
    pullRequest,
    pullNumber,
    trustedRepository,
}) {
    const pullRequestNumber = requireBoundedInteger(
            pullRequest.number,
            'pull request number',
    );

    const resolvedPullNumber = requireBoundedInteger(
            pullNumber,
            'resolved pull request number',
    );

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

    const baseRepositoryId = requireBoundedInteger(
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

    const headRepositoryId = requireBoundedInteger(
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
            trustedRepository.repository,
            'pull request base repository',
    );

    requireEqual(
            baseRepositoryId,
            trustedRepository.repositoryId,
            'pull request base repository id',
    );

    requireEqual(
            baseRef,
            trustedRepository.defaultBranch,
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

    const completedRun = validateCompletedRun(eventRun, run);
    const trustedWorkflow = validateTrustedWorkflow(
            run,
            workflow,
            repository,
            completedRun.workflowId,
    );

    const currentPullRequest = validateCurrentPullRequest({
        run,
        pullRequest,
        pullNumber,
        trustedRepository: trustedWorkflow,
    });

    return {
        repository: trustedWorkflow.repository,
        repositoryId: trustedWorkflow.repositoryId,
        workflow: WORKFLOW_NAME,
        event: 'pull_request',
        runId: completedRun.runId,
        runAttempt: completedRun.runAttempt,
        ...currentPullRequest,
    };
}

function selectMetricsArtifact({ artifacts, source }) {
    if (!Array.isArray(artifacts)) reject('workflow artifacts are missing');

    const name = expectedArtifactName(source);
    const matches = artifacts.filter((artifact) => artifact?.name === name);

    if (matches.length !== 1) reject(`expected exactly one metrics artifact, found ${matches.length}`);

    const [artifact] = matches;

    validateArtifactBinding(reject, {
        artifact,
        expected: {
            runId: source.runId,
            repositoryId: source.repositoryId,
            headRepositoryId: source.headRepositoryId,
            headBranch: source.headRef,
            headSha: source.headSha,
        },
        maxBytes: MAX_ARTIFACT_BYTES,
        label: 'metrics artifact',
    });

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
