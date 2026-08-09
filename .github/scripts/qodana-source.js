'use strict';

const {
  CI_WORKFLOW_NAME,
  CI_WORKFLOW_PATH,
  buildArtifactName,
  classifyChangedFiles,
} = require('./qodana-contract.js');

class QodanaSourceRejectedError extends Error {
  constructor(message, { stale = false } = {}) {
    super(message);
    this.name = 'QodanaSourceRejectedError';
    this.stale = stale;
  }
}

function reject(message, stale = false) {
  throw new QodanaSourceRejectedError(message, { stale });
}

function requireObject(value, label) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    reject(`${label} is missing`);
  }
  return value;
}

function requireEqual(actual, expected, label, stale = false) {
  if (actual !== expected) {
    reject(`${label} does not match the trusted value`, stale);
  }
}

function requirePositiveInteger(value, label) {
  if (!Number.isSafeInteger(value) || value < 1) {
    reject(`${label} is invalid`);
  }
  return value;
}

function requireSha(value, label) {
  if (typeof value !== 'string' || !/^[0-9a-f]{40}$/.test(value)) {
    reject(`${label} is invalid`);
  }
  return value;
}

function repositoryName(repository) {
  requireObject(repository, 'repository');
  if (typeof repository.full_name !== 'string' || repository.full_name.length === 0) {
    reject('repository name is invalid');
  }
  return repository.full_name;
}

async function getPullRequest({ github, owner, repo, repository, run }) {
  const listedPullRequests = Array.isArray(run.pull_requests) ? run.pull_requests : [];
  if (listedPullRequests.length > 1) {
    reject('workflow run identifies more than one pull request');
  }

  let pullNumber = listedPullRequests[0]?.number;
  if (pullNumber == null) {
    const associatedPullRequests = await github.paginate(
      github.rest.repos.listPullRequestsAssociatedWithCommit,
      {
        owner,
        repo,
        commit_sha: run.head_sha,
        per_page: 100,
      },
    );
    const matchingPullRequests = Array.isArray(associatedPullRequests)
      ? associatedPullRequests.filter((pullRequest) => pullRequest?.state === 'open'
        && pullRequest.base?.repo?.full_name === repository.full_name
        && pullRequest.base?.repo?.id === repository.id
        && pullRequest.base?.ref === repository.default_branch
        && pullRequest.head?.repo?.full_name === run.head_repository?.full_name
        && pullRequest.head?.repo?.id === run.head_repository?.id
        && pullRequest.head?.ref === run.head_branch
        && pullRequest.head?.sha === run.head_sha)
      : [];
    if (matchingPullRequests.length !== 1) {
      reject('workflow run does not identify exactly one pull request');
    }
    pullNumber = matchingPullRequests[0]?.number;
  }
  requirePositiveInteger(pullNumber, 'pull request number');
  const response = await github.rest.pulls.get({
    owner,
    repo,
    pull_number: pullNumber,
  });
  return requireObject(response.data, 'pull request');
}

async function getChangedFiles({ github, owner, repo, pullRequest }) {
  const files = await github.paginate(github.rest.pulls.listFiles, {
    owner,
    repo,
    pull_number: pullRequest.number,
    per_page: 100,
  });
  if (!Array.isArray(files)
    || !Number.isSafeInteger(pullRequest.changed_files)
    || pullRequest.changed_files < 1
    || files.length !== pullRequest.changed_files) {
    reject('pull-request file list is incomplete');
  }
  return files;
}

function hasTrustedReleaseMetadata({ repository, pullRequest }) {
  return pullRequest.base?.repo?.full_name === repository.full_name
    && pullRequest.base?.ref === repository.default_branch
    && pullRequest.head?.repo?.full_name === repository.full_name
    && pullRequest.head?.ref === 'release-please--branches--master'
    && pullRequest.user?.login === 'release-please-kotventure[bot]'
    && pullRequest.user?.type === 'Bot';
}

async function readTrustedReleaseProvenance({ github, owner, repo, repository, pullRequest }) {
  let workflow;
  try {
    const response = await github.rest.actions.getWorkflow({
      owner,
      repo,
      workflow_id: 'release-provenance.yml',
    });
    workflow = requireObject(response.data, 'release provenance workflow');
  } catch {
    return 'missing';
  }
  if (workflow.path !== '.github/workflows/release-provenance.yml') {
    return 'missing';
  }

  let runs;
  try {
    runs = await github.paginate(github.rest.actions.listWorkflowRuns, {
      owner,
      repo,
      workflow_id: 'release-provenance.yml',
      event: 'pull_request_target',
      per_page: 100,
    });
  } catch {
    return 'missing';
  }

  const candidate = runs
    .filter((run) => {
      const associatedPullRequests = Array.isArray(run?.pull_requests) ? run.pull_requests : [];
      return run?.event === 'pull_request_target'
        && run.repository?.full_name === repository.full_name
        && run.head_sha === pullRequest.head.sha
        && run.head_branch === pullRequest.head.ref
        && (associatedPullRequests.length === 0
          || associatedPullRequests.some((item) => item.number === pullRequest.number));
    })
    .sort((left, right) => new Date(right.created_at) - new Date(left.created_at))[0];

  if (!candidate) {
    return 'missing';
  }
  if (candidate.status !== 'completed') {
    return 'pending';
  }
  if (candidate.conclusion !== 'success') {
    return 'failed';
  }

  let jobs;
  try {
    jobs = await github.paginate(github.rest.actions.listJobsForWorkflowRun, {
      owner,
      repo,
      run_id: candidate.id,
      per_page: 100,
    });
  } catch {
    return 'missing';
  }
  const trustedJob = jobs.find((job) => job?.name === 'Trusted release provenance');
  if (trustedJob?.status === 'completed' && trustedJob.conclusion === 'success') {
    return 'success';
  }
  return trustedJob?.status === 'completed' ? 'failed' : 'pending';
}

function wait(milliseconds) {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}

async function hasTrustedReleaseProvenance(options) {
  const attempts = options.waitForReleaseProvenance === false ? 1 : 6;
  for (let attempt = 0; attempt < attempts; attempt += 1) {
    const status = await readTrustedReleaseProvenance(options);
    if (status === 'success' || status === 'failed') {
      return status === 'success';
    }
    if (attempt + 1 < attempts) {
      await wait(10_000);
    }
  }
  return false;
}

async function resolveMergeBase({ github, owner, repo, baseSha, headSha }) {
  const response = await github.rest.repos.compareCommitsWithBasehead({
    owner,
    repo,
    basehead: `${baseSha}...${headSha}`,
  });
  const mergeBaseSha = response.data?.merge_base_commit?.sha;
  return requireSha(mergeBaseSha, 'merge-base SHA');
}

function validateEventRun({ eventRun, run }) {
  requireObject(eventRun, 'workflow_run event');
  requireEqual(eventRun.id, run.id, 'workflow run id');
  requireEqual(eventRun.run_attempt, run.run_attempt, 'workflow run attempt');
  requireEqual(eventRun.head_sha, run.head_sha, 'workflow run head SHA');
  if (eventRun.workflow_id != null) {
    requireEqual(eventRun.workflow_id, run.workflow_id, 'workflow run workflow id');
  }
}

async function resolveCiRun({
  github,
  owner,
  repo,
  runId,
  eventRun,
  expectedRunAttempt,
  expectedHeadSha,
  expectedBaseSha,
  waitForReleaseProvenance = true,
}) {
  requirePositiveInteger(runId, 'workflow run id');
  const repositoryResponse = await github.rest.repos.get({ owner, repo });
  const repository = requireObject(repositoryResponse.data, 'repository');
  requireEqual(repository.full_name, `${owner}/${repo}`, 'repository identity');
  const runResponse = await github.rest.actions.getWorkflowRun({ owner, repo, run_id: runId });
  const run = requireObject(runResponse.data, 'workflow run');
  const workflowResponse = await github.rest.actions.getWorkflow({
    owner,
    repo,
    workflow_id: run.workflow_id,
  });
  const workflow = requireObject(workflowResponse.data, 'workflow');

  if (eventRun) {
    validateEventRun({ eventRun, run });
  }
  requireEqual(run.event, 'pull_request', 'workflow run event');
  requireEqual(run.status, 'completed', 'workflow run status');
  requireEqual(run.conclusion, 'success', 'workflow run conclusion');
  requireEqual(run.repository?.full_name, repositoryName(repository), 'workflow run repository');
  requireEqual(run.repository?.id, repository.id, 'workflow run repository id');
  requireEqual(workflow.id, run.workflow_id, 'workflow identity');
  requireEqual(workflow.name, CI_WORKFLOW_NAME, 'workflow name');
  requireEqual(workflow.path, CI_WORKFLOW_PATH, 'workflow path');
  if (expectedRunAttempt != null) {
    requireEqual(run.run_attempt, expectedRunAttempt, 'workflow run attempt');
  }
  if (expectedHeadSha != null) {
    requireEqual(run.head_sha, expectedHeadSha, 'workflow run head SHA');
  }

  const pullRequest = await getPullRequest({ github, owner, repo, repository, run });
  requireEqual(pullRequest.state, 'open', 'pull request state', true);
  requireEqual(pullRequest.base?.repo?.full_name, repository.full_name, 'pull request base repository', true);
  requireEqual(pullRequest.base?.repo?.id, repository.id, 'pull request base repository id', true);
  requireEqual(pullRequest.base?.ref, repository.default_branch, 'pull request base branch', true);
  requireSha(pullRequest.base?.sha, 'pull request base SHA');
  const headRepository = requireObject(pullRequest.head?.repo, 'pull request head repository');
  requireEqual(run.head_repository?.full_name, headRepository.full_name, 'workflow head repository', true);
  requireEqual(run.head_repository?.id, headRepository.id, 'workflow head repository id', true);
  requireEqual(run.head_branch, pullRequest.head?.ref, 'workflow head branch', true);
  requireEqual(run.head_sha, pullRequest.head?.sha, 'pull request head SHA', true);
  if (expectedBaseSha != null) {
    requireEqual(pullRequest.base.sha, expectedBaseSha, 'pull request base SHA', true);
  }

  const files = await getChangedFiles({ github, owner, repo, pullRequest });
  const pathClassification = classifyChangedFiles(files);
  let sourceKind = pathClassification;
  if (pathClassification === 'release-candidate') {
    const trusted = hasTrustedReleaseMetadata({ repository, pullRequest })
      && await hasTrustedReleaseProvenance({
        github,
        owner,
        repo,
        repository,
        pullRequest,
        waitForReleaseProvenance,
      });
    sourceKind = trusted ? 'release' : 'code';
  }

  const source = {
    repository: repository.full_name,
    runId: requirePositiveInteger(run.id, 'workflow run id'),
    runAttempt: requirePositiveInteger(run.run_attempt, 'workflow run attempt'),
    pullRequest: requirePositiveInteger(pullRequest.number, 'pull request number'),
    pathClassification,
    sourceKind,
    baseRepository: pullRequest.base.repo.full_name,
    baseRepositoryId: pullRequest.base.repo.id,
    baseRef: pullRequest.base.ref,
    baseSha: pullRequest.base.sha,
    headRepository: headRepository.full_name,
    headRepositoryId: headRepository.id,
    headRef: pullRequest.head.ref,
    headSha: pullRequest.head.sha,
  };

  if (sourceKind === 'code') {
    source.mergeBaseSha = await resolveMergeBase({
      github,
      owner,
      repo,
      baseSha: source.baseSha,
      headSha: source.headSha,
    });
  }
  return source;
}

async function resolveSource({ github, context, qodanaRunId, qodanaRunAttempt }) {
  const eventRun = requireObject(context.payload?.workflow_run, 'workflow_run event');
  const source = await resolveCiRun({
    github,
    owner: context.repo.owner,
    repo: context.repo.repo,
    runId: requirePositiveInteger(eventRun.id, 'workflow run id'),
    eventRun,
  });
  return {
    ...source,
    artifactName: buildArtifactName({
      sourceKind: source.sourceKind,
      runId: source.runId,
      runAttempt: source.runAttempt,
      qodanaRunId: requirePositiveInteger(qodanaRunId, 'Qodana workflow run id'),
      qodanaRunAttempt: requirePositiveInteger(qodanaRunAttempt, 'Qodana workflow run attempt'),
      headSha: source.headSha,
      baseSha: source.baseSha,
    }),
  };
}

async function resolveTrustedCiRun({ github, owner, repo, runId, eventRun }) {
  requirePositiveInteger(runId, 'workflow run id');
  const repositoryResponse = await github.rest.repos.get({ owner, repo });
  const repository = requireObject(repositoryResponse.data, 'repository');
  requireEqual(repository.full_name, `${owner}/${repo}`, 'repository identity');
  const runResponse = await github.rest.actions.getWorkflowRun({ owner, repo, run_id: runId });
  const run = requireObject(runResponse.data, 'workflow run');
  const workflowResponse = await github.rest.actions.getWorkflow({
    owner,
    repo,
    workflow_id: run.workflow_id,
  });
  const workflow = requireObject(workflowResponse.data, 'workflow');

  if (eventRun) {
    validateEventRun({ eventRun, run });
  }
  if (!['push', 'schedule', 'workflow_dispatch'].includes(run.event)) {
    reject('workflow run event is not trusted');
  }
  requireEqual(run.status, 'completed', 'workflow run status');
  requireEqual(run.conclusion, 'success', 'workflow run conclusion');
  requireEqual(run.repository?.full_name, repositoryName(repository), 'workflow run repository');
  requireEqual(run.repository?.id, repository.id, 'workflow run repository id');
  requireEqual(run.head_repository?.full_name, repository.full_name, 'workflow head repository');
  requireEqual(run.head_repository?.id, repository.id, 'workflow head repository id');
  requireEqual(run.head_branch, repository.default_branch, 'workflow run default branch');
  requireEqual(workflow.id, run.workflow_id, 'workflow identity');
  requireEqual(workflow.name, CI_WORKFLOW_NAME, 'workflow name');
  requireEqual(workflow.path, CI_WORKFLOW_PATH, 'workflow path');

  return {
    event: run.event,
    headSha: requireSha(run.head_sha, 'workflow run head SHA'),
  };
}

module.exports = {
  QodanaSourceRejectedError,
  hasTrustedReleaseMetadata,
  hasTrustedReleaseProvenance,
  resolveCiRun,
  resolveSource,
  resolveTrustedCiRun,
};
