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

class PublicationRejectedError extends Error {}

function reject(message) {
  throw new PublicationRejectedError(message);
}

function requireEqual(actual, expected, label) {
  if (actual !== expected) {
    reject(`${label} does not match the trusted value`);
  }
}

function requireSafeInteger(value, label, minimum = 1) {
  if (!Number.isSafeInteger(value) || value < minimum) {
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

function expectedArtifactName(run) {
  const id = requireSafeInteger(run.id, 'workflow run id');
  const attempt = requireSafeInteger(run.run_attempt, 'workflow run attempt');
  return `${RESULT_ARTIFACT_PREFIX}${id}-${attempt}`;
}

function validateWorkflowSource({ eventRun, run, workflow, repository, pullRequest, defaultBranch, pullNumber }) {
  requireObject(eventRun, 'workflow_run event');
  requireObject(run, 'workflow run');
  requireObject(workflow, 'workflow');
  requireObject(repository, 'repository');
  requireObject(pullRequest, 'pull request');

  requireEqual(eventRun.id, run.id, 'workflow run id');
  requireEqual(eventRun.run_attempt, run.run_attempt, 'workflow run attempt');
  requireEqual(eventRun.head_sha, run.head_sha, 'workflow run head SHA');
  if (eventRun.workflow_id != null) {
    requireEqual(eventRun.workflow_id, run.workflow_id, 'workflow run workflow id');
  }
  requireEqual(eventRun.event, 'pull_request', 'workflow run event');
  requireEqual(eventRun.status, 'completed', 'workflow run event status');
  requireEqual(eventRun.conclusion, 'success', 'workflow run event conclusion');
  requireEqual(run.event, 'pull_request', 'workflow run event');
  requireEqual(run.status, 'completed', 'workflow run status');
  requireEqual(run.conclusion, 'success', 'workflow run conclusion');
  requireEqual(run.repository?.full_name, repository.full_name, 'workflow run repository');
  requireEqual(run.repository?.id, repository.id, 'workflow run repository id');
  requireEqual(workflow.id, run.workflow_id, 'workflow identity');
  requireEqual(workflow.name, WORKFLOW_NAME, 'workflow name');
  requireEqual(workflow.path, EXPECTED_WORKFLOW_PATH, 'workflow path');

  if (!Array.isArray(run.pull_requests) || run.pull_requests.length > 1) {
    reject('workflow run must identify at most one pull request');
  }
  if (run.pull_requests.length === 1) {
    requireEqual(run.pull_requests[0].number, pullRequest.number, 'pull request number');
  }
  requireEqual(pullNumber ?? run.pull_requests[0]?.number, pullRequest.number, 'pull request number');
  requireEqual(pullRequest.state, 'open', 'pull request state');

  requireEqual(pullRequest.base?.repo?.full_name, repository.full_name, 'pull request base repository');
  requireEqual(pullRequest.base?.repo?.id, repository.id, 'pull request base repository id');
  requireEqual(pullRequest.base?.ref, defaultBranch, 'pull request base branch');

  const headRepository = requireObject(pullRequest.head?.repo, 'pull request head repository');
  requireEqual(run.head_repository?.full_name, headRepository.full_name, 'workflow head repository');
  requireEqual(run.head_repository?.id, headRepository.id, 'workflow head repository id');
  requireEqual(run.head_branch, pullRequest.head.ref, 'workflow head branch');
  requireEqual(run.head_sha, pullRequest.head.sha, 'pull request head SHA');

  return {
    repository: repository.full_name,
    workflow: WORKFLOW_NAME,
    event: 'pull_request',
    runId: requireSafeInteger(run.id, 'workflow run id'),
    runAttempt: requireSafeInteger(run.run_attempt, 'workflow run attempt'),
    pullRequest: requireSafeInteger(pullRequest.number, 'pull request number'),
    baseRepository: pullRequest.base.repo.full_name,
    baseRepositoryId: pullRequest.base.repo.id,
    baseRef: pullRequest.base.ref,
    baseSha: pullRequest.base.sha,
    headRepository: headRepository.full_name,
    headRepositoryId: headRepository.id,
    headRef: pullRequest.head.ref,
    headSha: pullRequest.head.sha,
  };
}

function selectMetricsArtifact({ artifacts, run, source }) {
  if (!Array.isArray(artifacts)) {
    reject('workflow artifacts are missing');
  }
  const name = expectedArtifactName(run);
  const matches = artifacts.filter((artifact) => artifact?.name === name);
  if (matches.length !== 1) {
    reject(`expected exactly one metrics artifact, found ${matches.length}`);
  }
  const [artifact] = matches;
  requireSafeInteger(artifact.id, 'metrics artifact id');
  if (artifact.expired !== false) {
    reject('metrics artifact is expired');
  }
  if (!Number.isSafeInteger(artifact.size_in_bytes)
    || artifact.size_in_bytes < 1
    || artifact.size_in_bytes > MAX_ARTIFACT_BYTES) {
    reject('metrics artifact size is invalid');
  }
  const workflowRun = requireObject(artifact.workflow_run, 'metrics artifact workflow run');
  requireEqual(workflowRun.id, source.runId, 'metrics artifact workflow run id');
  requireEqual(workflowRun.repository_id, source.repositoryId, 'metrics artifact repository id');
  requireEqual(workflowRun.head_repository_id, source.headRepositoryId, 'metrics artifact head repository id');
  requireEqual(workflowRun.head_branch, source.headRef, 'metrics artifact head branch');
  requireEqual(workflowRun.head_sha, source.headSha, 'metrics artifact head SHA');
  return artifact;
}

function validateResultProvenance(result, source) {
  try {
    validateMetricsResult(result);
  } catch (error) {
    reject(`metrics result is invalid: ${error.message}`);
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
    requireEqual(result.provenance[key], value, `metrics result provenance ${key}`);
  }
  return result;
}

module.exports = {
  PublicationRejectedError,
  expectedArtifactName,
  selectMetricsArtifact,
  validateResultProvenance,
  validateWorkflowSource,
};
