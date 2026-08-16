import type { CheckRunData, CheckRunListItem, Octokit } from './shared/action-context.js';
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
const WORKFLOW_CHECK_STATUSES = new Set(['queued', 'in_progress']);
type WorkflowCheckStatus = 'queued' | 'in_progress';
const MAX_CHECK_RUNS = 1_000;
const MAX_CHECK_ANNOTATIONS = 50;

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
  workflowId?: number;
  runId: number;
  runAttempt: number;
  headSha: string;
}

function buildCheckExternalId({ kind, workflowId, runId, runAttempt, headSha }: BuildCheckExternalIdOptions): string {
  if (typeof kind !== 'string' || !KIND_PATTERN.test(kind)) throw new Error('check kind is invalid');
  const workflowPart = workflowId == null ? '' : `:workflow-${requireBoundedInteger(Number(workflowId), 'workflow id')}`;
  return `${EXTERNAL_ID_PREFIX}:${kind}${workflowPart}:${requireBoundedInteger(Number(runId), 'workflow run id')}:${requireBoundedInteger(Number(runAttempt), 'workflow run attempt')}:${requireSha(headSha, 'check head SHA')}`;
}

interface CreatedCheckExpectation {
  name: string;
  headSha: string;
  externalId: string;
  status: WorkflowCheckStatus;
}

function validateCheck(check: CheckRunData | CheckRunListItem | undefined, expected: Omit<CreatedCheckExpectation, 'status'>): CheckRunData | CheckRunListItem {
  const trustedCheck = requireObject<CheckRunData>(check, 'created check');
  requireBoundedInteger(trustedCheck.id, 'created check id');
  requireEqual(trustedCheck.name, expected.name, 'created check name');
  requireEqual(trustedCheck.head_sha, expected.headSha, 'created check head SHA');
  requireEqual(trustedCheck.external_id, expected.externalId, 'created check external id');
  requireEqual(trustedCheck.app?.slug, CHECK_APP_SLUG, 'created check application');
  return trustedCheck;
}

function validateCreatedCheck(check: CheckRunData | undefined, expected: CreatedCheckExpectation): CheckRunData {
  const trustedCheck = validateCheck(check, expected);
  requireEqual(trustedCheck.status, expected.status, 'created check status');
  return trustedCheck;
}

export interface WorkflowCheckReference {
  id: number;
  externalId: string;
  headSha: string;
  name: string;
  status: string;
  conclusion: string | null;
}

export interface WorkflowCheckAnnotation {
  path: string;
  start_line: number;
  end_line: number;
  annotation_level: 'notice' | 'warning' | 'failure';
  title: string;
  message: string;
}

function checkReference(check: CheckRunData | CheckRunListItem): WorkflowCheckReference {
  return {
    id: requireBoundedInteger(check.id, 'check id'),
    externalId: requireText(check.external_id, 'check external id', 256),
    headSha: requireSha(check.head_sha, 'check head SHA'),
    name: requireText(check.name, 'check name', 100),
    status: requireText(check.status, 'check status', 32),
    conclusion: check.conclusion ?? null,
  };
}

function validateWorkflowCheckStatus(status: string): WorkflowCheckStatus {
  if (!WORKFLOW_CHECK_STATUSES.has(status)) throw new Error('check status is invalid');
  return status as WorkflowCheckStatus;
}

async function findWorkflowCheck({
  github,
  context,
  headSha,
  name,
  externalId,
}: {
  github: Octokit;
  context: WorkflowRunCheckContext;
  headSha: string;
  name: string;
  externalId: string;
}): Promise<WorkflowCheckReference | null> {
  const owner = requireText(context?.repo?.owner, 'repository owner', 100);
  const repo = requireText(context?.repo?.repo, 'repository name', 100);
  const trustedHeadSha = requireSha(headSha, 'check head SHA');
  const trustedName = requireText(name, 'check name', 100);
  const trustedExternalId = requireText(externalId, 'check external id', 256);
  const checkRuns = await github.paginate(
    github.rest.checks.listForRef,
    {
      owner,
      repo,
      ref: trustedHeadSha,
      check_name: trustedName,
      filter: 'all',
      per_page: 100,
    },
    (response) => {
      if (response.data.total_count > MAX_CHECK_RUNS) throw new Error('workflow check list exceeds the validation bound');
      return response.data;
    },
  );
  const matches = checkRuns.filter((check) => check.external_id === trustedExternalId);
  if (matches.length > 1) throw new Error('duplicate workflow check external id');
  const match = matches[0];
  if (match == null) return null;
  const trustedCheck = validateCheck(match, {
    name: trustedName,
    headSha: trustedHeadSha,
    externalId: trustedExternalId,
  });
  requireEqual(trustedCheck.app?.slug, CHECK_APP_SLUG, 'workflow check application');
  return checkReference(trustedCheck);
}

async function createWorkflowCheck({
  github,
  context,
  name,
  headSha,
  externalId,
  summary,
  status = 'in_progress',
}: {
  github: Octokit;
  context: WorkflowRunCheckContext;
  name: string;
  headSha: string;
  externalId: string;
  summary: string;
  status?: WorkflowCheckStatus;
}): Promise<WorkflowCheckReference> {
  const owner = requireText(context?.repo?.owner, 'repository owner', 100);
  const repo = requireText(context?.repo?.repo, 'repository name', 100);
  const trustedName = requireText(name, 'check name', 100);
  const trustedHeadSha = requireSha(headSha, 'check head SHA');
  const trustedExternalId = requireText(externalId, 'check external id', 256);
  const trustedSummary = requireText(summary, 'check summary', 65_535);
  const trustedStatus = validateWorkflowCheckStatus(status);
  const response = await github.rest.checks.create({
    owner,
    repo,
    name: trustedName,
    head_sha: trustedHeadSha,
    status: trustedStatus,
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
    status: trustedStatus,
  });
  return checkReference(check);
}

async function ensureWorkflowCheck({
  github,
  context,
  name,
  headSha,
  externalId,
  summary,
  status = 'queued',
}: {
  github: Octokit;
  context: WorkflowRunCheckContext;
  name: string;
  headSha: string;
  externalId: string;
  summary: string;
  status?: WorkflowCheckStatus;
}): Promise<WorkflowCheckReference> {
  // GitHub has no conditional Check Run creation. The owning workflow must
  // serialise calls for one external ID before using this find-or-create path.
  const existing = await findWorkflowCheck({
    github,
    context,
    headSha,
    name,
    externalId,
  });
  if (existing) return existing;
  return createWorkflowCheck({
    github,
    context,
    name,
    headSha,
    externalId,
    summary,
    status,
  });
}

async function updateWorkflowCheck({
  github,
  context,
  checkId,
  name,
  headSha,
  externalId,
  status,
  summary,
  annotations,
}: {
  github: Octokit;
  context: WorkflowRunCheckContext;
  checkId: number;
  name: string;
  headSha: string;
  externalId: string;
  status: WorkflowCheckStatus;
  summary: string;
  annotations?: WorkflowCheckAnnotation[];
}): Promise<void> {
  const owner = requireText(context?.repo?.owner, 'repository owner', 100);
  const repo = requireText(context?.repo?.repo, 'repository name', 100);
  const trustedCheckId = requireBoundedInteger(checkId, 'check id');
  const trustedName = requireText(name, 'check name', 100);
  const trustedHeadSha = requireSha(headSha, 'check head SHA');
  const trustedExternalId = requireText(externalId, 'check external id', 256);
  const trustedStatus = validateWorkflowCheckStatus(status);
  const trustedSummary = requireText(summary, 'check summary', 65_535);
  const response = await github.rest.checks.get({
    owner,
    repo,
    check_run_id: trustedCheckId,
  });
  const check = validateCheck(response?.data, {
    name: trustedName,
    headSha: trustedHeadSha,
    externalId: trustedExternalId,
  });
  requireEqual(check.app?.slug, CHECK_APP_SLUG, 'check application');
  if (check.status === 'completed') return;
  for (const annotationBatch of annotationBatches(annotations)) {
    await github.rest.checks.update({
      owner,
      repo,
      check_run_id: trustedCheckId,
      status: trustedStatus,
      details_url: workflowRunUrl(context),
      output: {
        title: trustedName,
        summary: trustedSummary,
        ...(annotationBatch == null ? {} : { annotations: annotationBatch }),
      },
    });
  }
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
  annotations,
}: {
  github: Octokit;
  context: WorkflowRunCheckContext;
  checkId: number;
  name: string;
  headSha: string;
  externalId: string;
  conclusion: string;
  summary: string;
  annotations?: WorkflowCheckAnnotation[];
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

  if (check.status === 'completed') {
    requireEqual(check.conclusion, trustedConclusion, 'check conclusion');
    return;
  }

  const annotationBatchesForCompletion = annotationBatches(annotations);
  for (const [index, annotationBatch] of annotationBatchesForCompletion.entries()) {
    const finalBatch = index === annotationBatchesForCompletion.length - 1;
    await github.rest.checks.update({
      owner,
      repo,
      check_run_id: trustedCheckId,
      ...(finalBatch
        ? {
          status: 'completed' as const,
          conclusion: trustedConclusion,
          completed_at: new Date().toISOString(),
        }
        : { status: 'in_progress' as const }),
      details_url: workflowRunUrl(context),
      output: {
        title: trustedName,
        summary: trustedSummary,
        ...(annotationBatch == null ? {} : { annotations: annotationBatch }),
      },
    });
  }
}

function annotationBatches(
  annotations: WorkflowCheckAnnotation[] | undefined,
): Array<WorkflowCheckAnnotation[] | undefined> {
  if (annotations == null || annotations.length <= MAX_CHECK_ANNOTATIONS) return [annotations];
  const batches: WorkflowCheckAnnotation[][] = [];
  for (let index = 0; index < annotations.length; index += MAX_CHECK_ANNOTATIONS) {
    batches.push(annotations.slice(index, index + MAX_CHECK_ANNOTATIONS));
  }
  return batches;
}

export {
  buildCheckExternalId,
  completeWorkflowCheck,
  createWorkflowCheck,
  ensureWorkflowCheck,
  findWorkflowCheck,
  updateWorkflowCheck,
  workflowResultConclusion,
};
