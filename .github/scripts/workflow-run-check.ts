import type { Octokit, CheckRunData } from './shared/action-context.js';
import { createValidators } from './shared/validation.js';

const KIND_PATTERN = /^[a-z0-9-]{1,48}$/;
const EXTERNAL_ID_PREFIX = 'workflow-run-check';
const CHECK_APP_SLUG = 'github-actions';
const WORKFLOW_RESULTS = new Set([
  'success',
  'failure',
  'cancelled',
  'skipped',
  'timed_out',
]);
type WorkflowResult = 'success' | 'failure' | 'cancelled' | 'skipped' | 'timed_out';

const {
  requireBoundedInteger,
  requireEqual,
  requireObject,
  requireSha,
  requireText,
} = createValidators((message: string): never => {
  throw new Error(message);
});

export interface WorkflowRunCheckContext {
  serverUrl: string;
  repo: { owner: string; repo: string };
  runId: number;
  runAttempt: number;
}

function workflowRunUrl(context: WorkflowRunCheckContext): string {
  const serverUrl = requireText(context?.serverUrl, 'GitHub server URL', 512);
  const repository = `${requireText(context?.repo?.owner, 'repository owner', 100)}/${requireText(context?.repo?.repo, 'repository name', 100)}`;
  const runId = requireBoundedInteger(Number(context?.runId), 'workflow run id');
  const runAttempt = requireBoundedInteger(Number(context?.runAttempt), 'workflow run attempt');
  return `${serverUrl}/${repository}/actions/runs/${runId}/attempts/${runAttempt}`;
}

export interface BuildCheckExternalIdOptions {
  kind: string;
  runId: number;
  runAttempt: number;
  headSha: string;
}

function buildCheckExternalId({ kind, runId, runAttempt, headSha }: BuildCheckExternalIdOptions): string {
  if (typeof kind !== 'string' || !KIND_PATTERN.test(kind)) throw new Error('check kind is invalid');
  return `${EXTERNAL_ID_PREFIX}:${kind}:${requireBoundedInteger(Number(runId), 'workflow run id')}:${requireBoundedInteger(Number(runAttempt), 'workflow run attempt')}:${requireSha(headSha, 'check head SHA')}`;
}

interface CreatedCheckExpectation {
  name: string;
  headSha: string;
  externalId: string;
}

function validateCreatedCheck(check: CheckRunData | undefined, expected: CreatedCheckExpectation): CheckRunData {
  const trustedCheck = requireObject<CheckRunData>(check, 'created check');
  requireBoundedInteger(trustedCheck.id, 'created check id');
  requireEqual(trustedCheck.name, expected.name, 'created check name');
  requireEqual(trustedCheck.head_sha, expected.headSha, 'created check head SHA');
  requireEqual(trustedCheck.external_id, expected.externalId, 'created check external id');
  requireEqual(trustedCheck.app?.slug, CHECK_APP_SLUG, 'created check application');
  return trustedCheck;
}

async function createWorkflowCheck({
  github,
  context,
  name,
  headSha,
  externalId,
  summary,
}: {
  github: Octokit;
  context: WorkflowRunCheckContext;
  name: string;
  headSha: string;
  externalId: string;
  summary: string;
}): Promise<{ id: number; externalId: string; headSha: string; name: string }> {
  const owner = requireText(context?.repo?.owner, 'repository owner', 100);
  const repo = requireText(context?.repo?.repo, 'repository name', 100);
  const trustedName = requireText(name, 'check name', 100);
  const trustedHeadSha = requireSha(headSha, 'check head SHA');
  const trustedExternalId = requireText(externalId, 'check external id', 256);
  const trustedSummary = requireText(summary, 'check summary', 65_535);
  const response = await github.rest.checks.create({
    owner,
    repo,
    name: trustedName,
    head_sha: trustedHeadSha,
    status: 'in_progress',
    external_id: trustedExternalId,
    details_url: workflowRunUrl(context),
    started_at: new Date().toISOString(),
    output: {
      title: trustedName,
      summary: trustedSummary,
    },
  });
  const check = validateCreatedCheck(response?.data, {
    name: trustedName,
    headSha: trustedHeadSha,
    externalId: trustedExternalId,
  });
  return {
    id: check.id,
    externalId: trustedExternalId,
    headSha: trustedHeadSha,
    name: trustedName,
  };
}

function workflowResultConclusion(result: string): WorkflowResult {
  if (typeof result !== 'string' || !WORKFLOW_RESULTS.has(result)) throw new Error('workflow result is invalid');
  return result as WorkflowResult;
}

async function completeWorkflowCheck({
  github,
  context,
  checkId,
  name,
  headSha,
  externalId,
  conclusion,
  summary,
}: {
  github: Octokit;
  context: WorkflowRunCheckContext;
  checkId: number;
  name: string;
  headSha: string;
  externalId: string;
  conclusion: string;
  summary: string;
}): Promise<void> {
  const owner = requireText(context?.repo?.owner, 'repository owner', 100);
  const repo = requireText(context?.repo?.repo, 'repository name', 100);
  const trustedCheckId = requireBoundedInteger(checkId, 'check id');
  const trustedName = requireText(name, 'check name', 100);
  const trustedHeadSha = requireSha(headSha, 'check head SHA');
  const trustedExternalId = requireText(externalId, 'check external id', 256);
  const trustedConclusion = workflowResultConclusion(conclusion);
  const trustedSummary = requireText(summary, 'check summary', 65_535);
  const response = await github.rest.checks.get({
    owner,
    repo,
    check_run_id: trustedCheckId,
  });
  const check = requireObject<CheckRunData>(response?.data, 'registered check');
  requireEqual(check.id, trustedCheckId, 'check id');
  requireEqual(check.name, trustedName, 'check name');
  requireEqual(check.head_sha, trustedHeadSha, 'check head SHA');
  requireEqual(check.external_id, trustedExternalId, 'check external id');
  requireEqual(check.app?.slug, CHECK_APP_SLUG, 'check application');

  await github.rest.checks.update({
    owner,
    repo,
    check_run_id: trustedCheckId,
    status: 'completed',
    conclusion: trustedConclusion,
    completed_at: new Date().toISOString(),
    details_url: workflowRunUrl(context),
    output: {
      title: trustedName,
      summary: trustedSummary,
    },
  });
}

export {
  buildCheckExternalId,
  completeWorkflowCheck,
  createWorkflowCheck,
  workflowResultConclusion,
};
