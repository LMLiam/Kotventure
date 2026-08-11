import {
  validateMetricsResult,
} from '../actions/pr-metrics-comment/lib/metrics-result.js';
import type { JsonValue } from '../actions/pr-metrics-comment/lib/metrics-result-contract.js';
import type { MetricsResultValue } from '../actions/pr-metrics-comment/lib/metrics-result-validation.js';
import {
  EXPECTED_WORKFLOW_PATH,
  MAX_ARTIFACT_BYTES,
  RESULT_ARTIFACT_PREFIX,
  WORKFLOW_NAME,
} from './pr-metrics-publisher-contract.js';
import type {
  PullRequestData,
  RepositoryData,
  WorkflowData,
  WorkflowRunArtifact,
  WorkflowRunData,
} from './shared/action-context.js';
import { createValidators } from './shared/validation.js';
import { validateArtifactBinding } from './shared/artifact-binding.js';
import type { WorkflowRunEventRecord } from './shared/run-context.js';

export class PublicationRejectedError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'PublicationRejectedError';
  }
}

function reject(message: string): never {
  throw new PublicationRejectedError(message);
}

const {
  requireBoundedInteger,
  requireEqual,
  requireObject,
  requireString,
} = createValidators(reject);

export interface WorkflowSource {
  repository: string;
  repositoryId: number;
  workflow: string;
  event: string;
  runId: number;
  runAttempt: number;
  pullRequest: number;
  baseRepository: string;
  baseRepositoryId: number;
  baseRef: string;
  baseSha: string;
  headRepository: string;
  headRepositoryId: number;
  headRef: string;
  headSha: string;
}

function expectedArtifactName({ runId, runAttempt }: { runId: number; runAttempt: number }): string {
  const id = requireBoundedInteger(runId, 'workflow run id');
  const attempt = requireBoundedInteger(runAttempt, 'workflow run attempt');

  return `${RESULT_ARTIFACT_PREFIX}${id}-${attempt}`;
}

function validateCompletedRun(eventRun: WorkflowRunEventRecord, run: WorkflowRunData): {
  runId: number;
  runAttempt: number;
  workflowId: number;
} {
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

function validateTrustedWorkflow(
  run: WorkflowRunData,
  workflow: WorkflowData,
  repository: RepositoryData,
  workflowId: number,
): {
  repository: string;
  repositoryId: number;
  defaultBranch: string;
} {
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
}: {
  run: WorkflowRunData;
  pullRequest: PullRequestData;
  pullNumber: number;
  trustedRepository: { repository: string; repositoryId: number; defaultBranch: string };
}): WorkflowSource {
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
    const runPullRequest = run.pull_requests[0];
    if (runPullRequest == null) reject('workflow run pull request number is invalid');
    requireEqual(
      runPullRequest.number,
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

  const baseRepository = requireObject<RepositoryData>(
    pullRequest.base?.repo,
    'pull request base repository',
  );

  const headRepository = requireObject<RepositoryData>(
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
    repository: trustedRepository.repository,
    repositoryId: trustedRepository.repositoryId,
    workflow: WORKFLOW_NAME,
    event: 'pull_request',
    runId: requireBoundedInteger(run.id, 'workflow run id'),
    runAttempt: requireBoundedInteger(run.run_attempt, 'workflow run attempt'),
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
}: {
  eventRun: WorkflowRunEventRecord;
  run: WorkflowRunData;
  workflow: WorkflowData;
  repository: RepositoryData;
  pullRequest: PullRequestData;
  pullNumber: number;
}): WorkflowSource {
  requireObject<WorkflowRunEventRecord>(eventRun, 'workflow_run event');
  requireObject<WorkflowRunData>(run, 'workflow run');
  requireObject<WorkflowData>(workflow, 'workflow');
  requireObject<RepositoryData>(repository, 'repository');
  requireObject<PullRequestData>(pullRequest, 'pull request');

  const completedRun = validateCompletedRun(eventRun, run);
  const trustedWorkflow = validateTrustedWorkflow(
    run,
    workflow,
    repository,
    completedRun.workflowId,
  );

  return validateCurrentPullRequest({
    run,
    pullRequest,
    pullNumber,
    trustedRepository: trustedWorkflow,
  });
}

function selectMetricsArtifact({
  artifacts,
  source,
}: {
  artifacts: WorkflowRunArtifact[];
  source: WorkflowSource;
}): WorkflowRunArtifact {
  if (!Array.isArray(artifacts)) reject('workflow artifacts are missing');

  const name = expectedArtifactName(source);
  const matches = artifacts.filter((artifact) => artifact?.name === name);

  if (matches.length !== 1) reject(`expected exactly one metrics artifact, found ${matches.length}`);

  const artifact = matches[0];
  if (artifact == null) reject(`expected exactly one metrics artifact, found ${matches.length}`);

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

function validateResultProvenance(result: JsonValue, source: WorkflowSource): MetricsResultValue {
  let validatedResult: MetricsResultValue;

  try {
    validatedResult = validateMetricsResult(result);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    reject(`metrics result is invalid: ${message}`);
  }

  const provenanceKeys = [
    'repository',
    'workflow',
    'event',
    'runId',
    'runAttempt',
    'pullRequest',
    'baseRepository',
    'baseRef',
    'baseSha',
    'headRepository',
    'headRef',
    'headSha',
  ] as const;

  for (const key of provenanceKeys) {
    requireEqual(
      validatedResult.provenance[key],
      source[key],
      `metrics result provenance ${key}`,
    );
  }

  return validatedResult;
}

export {
  expectedArtifactName,
  selectMetricsArtifact,
  validateResultProvenance,
  validateWorkflowSource,
};
