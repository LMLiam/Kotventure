import {
  buildArtifactName,
  classifyChangedFiles,
} from './qodana-contract.js';
import type {
  QodanaPathClassification,
  QodanaSourceKind,
} from './qodana-contract.js';
import type { ActionContext, Octokit, PullRequestData, PullRequestFile, RepositoryData } from './shared/action-context.js';
import { createValidators } from './shared/validation.js';
import { hasTrustedReleaseProvenance } from './shared/release-provenance.js';

export class QodanaSourceRejectedError extends Error {
  readonly stale: boolean;

  constructor(message: string, { stale = false }: { stale?: boolean } = {}) {
    super(message);
    this.name = 'QodanaSourceRejectedError';
    this.stale = stale;
  }
}

function reject(message: string, stale = false): never {
  throw new QodanaSourceRejectedError(message, { stale });
}

const {
  requireBoundedInteger,
  requireEqual,
  requireObject,
  requireSha,
  requireString,
} = createValidators(reject);

async function fetchRepository({ github, owner, repo }: {
  github: Octokit;
  owner: string;
  repo: string;
}): Promise<RepositoryData> {
  const repositoryResponse = await github.rest.repos.get({ owner, repo });
  const repository = requireObject<RepositoryData>(repositoryResponse.data, 'repository');
  requireEqual(repository.full_name, `${owner}/${repo}`, 'repository identity');
  return repository;
}

async function getChangedFiles({ github, owner, repo, pullRequest }: {
  github: Octokit;
  owner: string;
  repo: string;
  pullRequest: PullRequestData;
}): Promise<PullRequestFile[]> {
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

function hasTrustedReleaseMetadata({ repository, pullRequest }: {
  repository: RepositoryData;
  pullRequest: PullRequestData;
}): boolean {
  return pullRequest.base?.repo?.full_name === repository.full_name
    && pullRequest.base?.ref === repository.default_branch
    && pullRequest.head?.repo?.full_name === repository.full_name
    && pullRequest.head?.ref === 'release-please--branches--master'
    && pullRequest.user?.login === 'release-please-kotventure[bot]'
    && pullRequest.user?.type === 'Bot';
}

async function resolveMergeBase({ github, owner, repo, baseSha, headSha }: {
  github: Octokit;
  owner: string;
  repo: string;
  baseSha: string;
  headSha: string;
}): Promise<string> {
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
}: {
  pullRequest: PullRequestData;
  repository: RepositoryData;
  expectedHeadSha?: string | null;
  expectedBaseSha?: string | null;
}): void {
  requireEqual(pullRequest.state, 'open', 'pull request state', true);
  requireEqual(pullRequest.base?.repo?.full_name, repository.full_name, 'pull request base repository', true);
  requireEqual(pullRequest.base?.repo?.id, repository.id, 'pull request base repository id', true);
  requireSha(pullRequest.base?.sha, 'pull request base SHA');
  requireSha(pullRequest.head?.sha, 'pull request head SHA');
  if (expectedHeadSha != null) requireEqual(pullRequest.head?.sha, expectedHeadSha, 'pull request head SHA', true);
  if (expectedBaseSha != null) requireEqual(pullRequest.base.sha, expectedBaseSha, 'pull request base SHA', true);
}

export interface PullRequestSource {
  repository: string;
  pullRequest: number;
  pathClassification: QodanaPathClassification;
  sourceKind: QodanaSourceKind;
  baseRepository: string;
  baseRepositoryId: number;
  baseRef: string;
  baseSha: string;
  headRepository: string;
  headRepositoryId: number;
  headRef: string;
  headSha: string;
  mergeBaseSha?: string;
}

async function completePullRequestSource({
  github,
  owner,
  repo,
  repository,
  pullRequest,
  waitForReleaseProvenance = true,
}: {
  github: Octokit;
  owner: string;
  repo: string;
  repository: RepositoryData;
  pullRequest: PullRequestData;
  waitForReleaseProvenance?: boolean;
}): Promise<PullRequestSource> {
  const files = await getChangedFiles({ github, owner, repo, pullRequest });
  const pathClassification = classifyChangedFiles(files);
  let sourceKind: QodanaSourceKind;
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
  } else {
    sourceKind = pathClassification;
  }
  const baseRepository = requireObject<RepositoryData>(pullRequest.base?.repo, 'pull request base repository');
  const headRepository = requireObject<RepositoryData>(pullRequest.head?.repo, 'pull request head repository');
  const source: PullRequestSource = {
    repository: repository.full_name,
    pullRequest: requireBoundedInteger(pullRequest.number, 'pull request number'),
    pathClassification,
    sourceKind,
    baseRepository: baseRepository.full_name,
    baseRepositoryId: requireBoundedInteger(baseRepository.id, 'pull request base repository id'),
    baseRef: requireString(pullRequest.base?.ref, 'pull request base branch'),
    baseSha: requireSha(pullRequest.base?.sha, 'pull request base SHA'),
    headRepository: headRepository.full_name,
    headRepositoryId: requireBoundedInteger(headRepository.id, 'pull request head repository id'),
    headRef: requireString(pullRequest.head?.ref, 'pull request head branch'),
    headSha: requireSha(pullRequest.head?.sha, 'pull request head SHA'),
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

async function findPullRequestForHeadSha({ github, owner, repo, repository, headSha }: {
  github: Octokit;
  owner: string;
  repo: string;
  repository: RepositoryData;
  headSha: string;
}): Promise<number> {
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
  const pullRequest = matchingPullRequests[0];
  if (pullRequest == null) {
    reject('pull request head SHA does not identify exactly one open pull request', true);
  }
  return pullRequest.number;
}

async function resolvePullRequestSource({
  github,
  owner,
  repo,
  headSha,
  expectedBaseSha = null,
  waitForReleaseProvenance = true,
}: {
  github: Octokit;
  owner: string;
  repo: string;
  headSha: string;
  expectedBaseSha?: string | null;
  waitForReleaseProvenance?: boolean;
}): Promise<PullRequestSource> {
  requireSha(headSha, 'pull request head SHA');
  const repository = await fetchRepository({ github, owner, repo });
  const pullNumber = await findPullRequestForHeadSha({ github, owner, repo, repository, headSha });
  const response = await github.rest.pulls.get({ owner, repo, pull_number: pullNumber });
  const pullRequest = requireObject<PullRequestData>(response.data, 'pull request');
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

interface PullRequestEventRecord {
  number: number;
  head: { sha: string };
  base: { repo: { full_name: string; id: number } | null } | null;
}

export interface PullRequestEventSource extends PullRequestSource {
  artifactName: string;
}

async function resolvePullRequestEventSource({
  github,
  context,
  qodanaRunId,
  qodanaRunAttempt,
}: {
  github: Octokit;
  context: ActionContext['context'];
  qodanaRunId: number;
  qodanaRunAttempt: number;
}): Promise<PullRequestEventSource> {
  const pullRequestEvent = requireObject<PullRequestEventRecord>(context.payload?.pull_request, 'pull_request event');
  const eventHeadSha = requireSha(pullRequestEvent.head?.sha, 'pull request event head SHA');
  const owner = context.repo.owner;
  const repo = context.repo.repo;
  const repository = await fetchRepository({ github, owner, repo });
  requireEqual(pullRequestEvent.base?.repo?.full_name, repository.full_name, 'pull request base repository');
  requireEqual(pullRequestEvent.base?.repo?.id, repository.id, 'pull request base repository id');
  const pullNumber = requireBoundedInteger(pullRequestEvent.number, 'pull request number');
  const response = await github.rest.pulls.get({ owner, repo, pull_number: pullNumber });
  const pullRequest = requireObject<PullRequestData>(response.data, 'pull request');
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
      qodanaRunId: requireBoundedInteger(qodanaRunId, 'Qodana workflow run id'),
      qodanaRunAttempt: requireBoundedInteger(qodanaRunAttempt, 'Qodana workflow run attempt'),
      headSha: source.headSha,
      baseSha: source.baseSha,
    }),
  };
}

export {
  hasTrustedReleaseMetadata,
  resolvePullRequestEventSource,
  resolvePullRequestSource,
};
