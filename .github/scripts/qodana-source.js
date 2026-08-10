'use strict';

const {
  CI_WORKFLOW_NAME,
  CI_WORKFLOW_PATH,
  buildArtifactName,
  classifyChangedFiles,
} = require('./qodana-contract.js');
const { createValidators } = require('./shared/validation.js');
const { validateEventRun } = require('./shared/run-context.js');
const { hasTrustedReleaseProvenance } = require('./shared/release-provenance.js');

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

const {
  requireEqual,
  requireInteger: requirePositiveInteger,
  requireObject,
  requireSha,
} = createValidators(reject);

function repositoryName(repository) {
  requireObject(repository, 'repository');
  if (typeof repository.full_name !== 'string' || repository.full_name.length === 0) {
    reject('repository name is invalid');
  }
  return repository.full_name;
}

async function fetchRepository({ github, owner, repo }) {
  const repositoryResponse = await github.rest.repos.get({ owner, repo });
  const repository = requireObject(repositoryResponse.data, 'repository');
  requireEqual(repository.full_name, `${owner}/${repo}`, 'repository identity');
  return repository;
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

async function resolveMergeBase({ github, owner, repo, baseSha, headSha }) {
  const response = await github.rest.repos.compareCommitsWithBasehead({
    owner,
    repo,
    basehead: `${baseSha}...${headSha}`,
  });
  const mergeBaseSha = response.data?.merge_base_commit?.sha;
  return requireSha(mergeBaseSha, 'merge-base SHA');
}

function validatePullRequestState({
  pullRequest,
  repository,
  expectedHeadSha = null,
  expectedBaseSha = null,
}) {
  requireEqual(pullRequest.state, 'open', 'pull request state', true);
  requireEqual(pullRequest.base?.repo?.full_name, repository.full_name, 'pull request base repository', true);
  requireEqual(pullRequest.base?.repo?.id, repository.id, 'pull request base repository id', true);
  requireSha(pullRequest.base?.sha, 'pull request base SHA');
  requireSha(pullRequest.head?.sha, 'pull request head SHA');
  if (expectedHeadSha != null) {
    requireEqual(pullRequest.head?.sha, expectedHeadSha, 'pull request head SHA', true);
  }
  if (expectedBaseSha != null) {
    requireEqual(pullRequest.base.sha, expectedBaseSha, 'pull request base SHA', true);
  }
}

async function completePullRequestSource({
  github,
  owner,
  repo,
  repository,
  pullRequest,
  waitForReleaseProvenance = true,
}) {
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
  const headRepository = requireObject(pullRequest.head?.repo, 'pull request head repository');
  const source = {
    repository: repository.full_name,
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

async function findPullRequestForHeadSha({ github, owner, repo, repository, headSha }) {
  const associatedPullRequests = await github.paginate(
    github.rest.repos.listPullRequestsAssociatedWithCommit,
    {
      owner,
      repo,
      commit_sha: headSha,
      per_page: 100,
    },
  );
  const matchingPullRequests = Array.isArray(associatedPullRequests)
    ? associatedPullRequests.filter((pullRequest) => pullRequest?.state === 'open'
      && pullRequest.base?.repo?.full_name === repository.full_name
      && pullRequest.base?.repo?.id === repository.id
      && pullRequest.head?.sha === headSha)
    : [];
  if (matchingPullRequests.length !== 1) {
    reject('pull request head SHA does not identify exactly one open pull request', true);
  }
  return matchingPullRequests[0].number;
}

async function resolvePullRequestSource({
  github,
  owner,
  repo,
  headSha,
  expectedBaseSha = null,
  waitForReleaseProvenance = true,
}) {
  requireSha(headSha, 'pull request head SHA');
  const repository = await fetchRepository({ github, owner, repo });
  const pullNumber = await findPullRequestForHeadSha({ github, owner, repo, repository, headSha });
  const response = await github.rest.pulls.get({ owner, repo, pull_number: pullNumber });
  const pullRequest = requireObject(response.data, 'pull request');
  validatePullRequestState({
    pullRequest,
    repository,
    expectedHeadSha: headSha,
    expectedBaseSha,
  });
  return completePullRequestSource({
    github,
    owner,
    repo,
    repository,
    pullRequest,
    waitForReleaseProvenance,
  });
}

async function resolvePullRequestEventSource({ github, context, qodanaRunId, qodanaRunAttempt }) {
  const pullRequestEvent = requireObject(context.payload?.pull_request, 'pull_request event');
  const eventHeadSha = requireSha(pullRequestEvent.head?.sha, 'pull request event head SHA');
  const owner = context.repo.owner;
  const repo = context.repo.repo;
  const repository = await fetchRepository({ github, owner, repo });
  requireEqual(pullRequestEvent.base?.repo?.full_name, repository.full_name, 'pull request base repository');
  requireEqual(pullRequestEvent.base?.repo?.id, repository.id, 'pull request base repository id');
  const pullNumber = requirePositiveInteger(pullRequestEvent.number, 'pull request number');
  const response = await github.rest.pulls.get({ owner, repo, pull_number: pullNumber });
  const pullRequest = requireObject(response.data, 'pull request');
  validatePullRequestState({ pullRequest, repository, expectedHeadSha: eventHeadSha });
  const source = await completePullRequestSource({
    github,
    owner,
    repo,
    repository,
    pullRequest,
    waitForReleaseProvenance: true,
  });
  return {
    ...source,
    artifactName: buildArtifactName({
      sourceKind: source.sourceKind,
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
    validateEventRun(reject, { eventRun, run });
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
  resolvePullRequestEventSource,
  resolvePullRequestSource,
  resolveTrustedCiRun,
};
