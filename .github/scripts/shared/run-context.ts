import type { ActionContext } from './action-context.js';
import { createValidators, type ValidatorReject } from './validation.js';

type Octokit = ActionContext['github'];

type RepositoryData = Awaited<ReturnType<Octokit['rest']['repos']['get']>>['data'];
type WorkflowRunData = Awaited<ReturnType<Octokit['rest']['actions']['getWorkflowRun']>>['data'];
type WorkflowData = Awaited<ReturnType<Octokit['rest']['actions']['getWorkflow']>>['data'];

export interface WorkflowRunEventRecord {
  id: number;
  run_attempt: number;
  head_sha: string;
  workflow_id: number | null;
}

export interface WorkflowRunContext {
  repository: RepositoryData;
  run: WorkflowRunData;
  workflow: WorkflowData;
}

export interface ValidateEventRunOptions {
  eventRun: WorkflowRunEventRecord;
  run: WorkflowRunData;
}

export function validateEventRun(reject: ValidatorReject, { eventRun, run }: ValidateEventRunOptions): void {
  const { requireEqual, requireObject } = createValidators(reject);
  requireObject(eventRun, 'workflow_run event');
  requireEqual(eventRun.id, run.id, 'workflow run id');
  requireEqual(eventRun.run_attempt, run.run_attempt, 'workflow run attempt');
  requireEqual(eventRun.head_sha, run.head_sha, 'workflow run head SHA');
  if (eventRun.workflow_id != null) {
    requireEqual(eventRun.workflow_id, run.workflow_id, 'workflow run workflow id');
  }
}

export interface FetchWorkflowRunContextOptions {
  github: Octokit;
  owner: string;
  repo: string;
  eventRun: WorkflowRunEventRecord;
}

export async function fetchWorkflowRunContext(
  reject: ValidatorReject,
  { github, owner, repo, eventRun }: FetchWorkflowRunContextOptions,
): Promise<WorkflowRunContext> {
  const { requireEqual, requireInteger, requireObject } = createValidators(reject);
  requireObject(eventRun, 'workflow_run event');
  requireInteger(eventRun.id, 'workflow run id');

  const [{ data: repository }, { data: run }] = await Promise.all([
    github.rest.repos.get({ owner, repo }),
    github.rest.actions.getWorkflowRun({ owner, repo, run_id: eventRun.id }),
  ]);

  requireObject(repository, 'repository');
  requireEqual(repository.full_name, `${owner}/${repo}`, 'repository identity');
  requireObject(run, 'workflow run');
  validateEventRun(reject, { eventRun, run });

  const { data: workflow } = await github.rest.actions.getWorkflow({
    owner,
    repo,
    workflow_id: run.workflow_id,
  });
  requireObject(workflow, 'workflow');

  return { repository, run, workflow };
}
