import assert from 'node:assert/strict';
import test from 'node:test';
import { codeqlAnalysisApplies, prepareCodeql } from './codeql-publisher.js';
import type { RepositoryData, WorkflowData, WorkflowRunData } from './shared/action-context.js';
import type { WorkflowRunEventRecord } from './shared/run-context.js';
import { asApiData, mockContext, mockCore, mockOctokit } from './test-support/mocks.js';

const HEAD_SHA = 'a'.repeat(40);
const WORKFLOW_ID = 98;

const CHECK_NAMES: Record<string, string> = {
  actions: 'CodeQL publication (actions)',
  'java-kotlin': 'CodeQL publication (java-kotlin)',
};

function checkId(category: string): number {
  return category === 'actions' ? 700 : 701;
}

function mergeGroupRun(overrides: object = {}): WorkflowRunData {
  return asApiData<WorkflowRunData>({
    id: 456,
    run_attempt: 3,
    workflow_id: WORKFLOW_ID,
    head_sha: HEAD_SHA,
    head_branch: 'gh-readonly-queue/master/pr-1',
    head_repository: { id: 5, full_name: 'LMLiam/Kotventure' },
    repository: { id: 5, full_name: 'LMLiam/Kotventure' },
    event: 'merge_group',
    status: 'in_progress',
    conclusion: null,
    ...overrides,
  });
}

function mergeGroupEvent(overrides: object = {}): WorkflowRunEventRecord {
  return {
    id: 456,
    run_attempt: 3,
    head_sha: HEAD_SHA,
    workflow_id: WORKFLOW_ID,
    event: 'merge_group',
    status: 'in_progress',
    ...overrides,
  };
}

function makeAction(run: WorkflowRunData, eventRun: WorkflowRunEventRecord, associatedPullRequests: object[] = []) {
  const repository = asApiData<RepositoryData>({ id: 5, full_name: 'LMLiam/Kotventure' });
  const workflow = asApiData<WorkflowData>({ id: WORKFLOW_ID, name: 'CodeQL', path: '.github/workflows/codeql.yml' });
  const calls: Array<[string, object]> = [];
  const checks = new Map<number, object>();

  const paginate = async (
    _method: (...args: never[]) => object,
    _parameters: never,
    map?: (response: { data: object[] & { total_count: number } }, done: () => void) => object[],
  ): Promise<object[]> => {
    const params = _parameters as unknown as Record<string, unknown>;
    let items: object[];
    if (params.commit_sha !== undefined) items = associatedPullRequests;
    else if (params.pull_number !== undefined) items = [];
    else items = [];
    const data = Object.assign([...items], { total_count: items.length });
    if (map) return map({ data }, () => {});
    return data as unknown as object[];
  };

  const github = mockOctokit({
    rest: {
      repos: {
        get: async (params: object) => {
          calls.push(['repos.get', params]);
          return { data: repository };
        },
        listPullRequestsAssociatedWithCommit: async () => ({ data: [] }),
        compareCommitsWithBasehead: async (params: object) => {
          calls.push(['repos.compareCommitsWithBasehead', params]);
          return { data: { merge_base_commit: { sha: 'b'.repeat(40) } } };
        },
      },
      actions: {
        getWorkflowRun: async (params: object) => {
          calls.push(['actions.getWorkflowRun', params]);
          return { data: run };
        },
        getWorkflow: async (params: object) => {
          calls.push(['actions.getWorkflow', params]);
          return { data: workflow };
        },
      },
      pulls: {
        get: async (params: object) => {
          calls.push(['pulls.get', params]);
          return {
            data: asApiData({
              number: 1,
              state: 'open',
              base: { repo: { full_name: 'LMLiam/Kotventure', id: 5 }, ref: 'master', sha: 'b'.repeat(40) },
              head: { repo: { full_name: 'LMLiam/Kotventure', id: 5 }, ref: 'feature/codeql', sha: HEAD_SHA },
              changed_files: 1,
            }),
          };
        },
        listFiles: async () => ({ data: [] }),
      },
      checks: {
        create: async (params: Record<string, unknown>) => {
          calls.push(['checks.create', params]);
          const category = String(params.external_id).includes('actions') ? 'actions' : 'java-kotlin';
          const id = checkId(category);
          const check = {
            id,
            name: params.name,
            head_sha: params.head_sha,
            external_id: params.external_id,
            status: params.status,
            conclusion: null,
            app: { slug: 'github-actions' },
          };
          checks.set(id, check);
          return { data: check };
        },
        get: async (params: { check_run_id: number }) => {
          calls.push(['checks.get', params]);
          return { data: checks.get(params.check_run_id) };
        },
        update: async (params: { check_run_id: number; status?: string; conclusion?: string | null }) => {
          calls.push(['checks.update', params]);
          const existing = checks.get(params.check_run_id) as Record<string, unknown>;
          const updated = {
            ...existing,
            status: params.status ?? existing.status,
            conclusion: params.conclusion ?? existing.conclusion,
          };
          checks.set(params.check_run_id, updated);
          return { data: updated };
        },
        listForRef: async () => ({ data: { check_runs: [] } }),
      },
    },
    paginate,
  });

  const outputs = new Map<string, unknown>();
  const warnings: string[] = [];
  const core = mockCore({
    setOutput: (name, value) => {
      outputs.set(name, value);
    },
    warning: (message) => {
      warnings.push(String(message));
    },
  });
  const context = mockContext({
    repo: { owner: 'LMLiam', repo: 'Kotventure' },
    serverUrl: 'https://github.com',
    payload: { workflow_run: eventRun },
  });

  return { action: { github, context, core }, calls, outputs, warnings };
}

test('does not publish CodeQL for non-code pull-request classifications', () => {
  assert.equal(codeqlAnalysisApplies('documentation'), false);
  assert.equal(codeqlAnalysisApplies('release'), false);
  assert.equal(codeqlAnalysisApplies('code'), true);
  assert.equal(codeqlAnalysisApplies(null), true);
});

test('waits for an in-progress merge-group CodeQL run', async () => {
  const fixture = makeAction(mergeGroupRun(), mergeGroupEvent());
  await prepareCodeql(fixture.action);

  assert.equal(fixture.outputs.get('publish'), false);
  assert.equal(fixture.outputs.get('head_sha'), HEAD_SHA);
  assert.equal(fixture.outputs.get('upload_ref'), 'refs/heads/gh-readonly-queue/master/pr-1');
  assert.equal(fixture.outputs.get('actions_check_id'), checkId('actions'));
  assert.equal(fixture.outputs.get('java_kotlin_check_id'), checkId('java-kotlin'));
  assert.equal(fixture.outputs.get('actions_path'), undefined);
  const updates = fixture.calls.filter(([operation]) => operation === 'checks.update');
  assert.equal(updates.length, 2);
});

test('completes a non-success CodeQL run as failure', async () => {
  const fixture = makeAction(
    mergeGroupRun({ status: 'completed', conclusion: 'neutral' }),
    mergeGroupEvent({ status: 'completed' }),
  );
  await prepareCodeql(fixture.action);

  assert.equal(fixture.outputs.get('publish'), false);
  const updates = fixture.calls
    .filter(([operation]) => operation === 'checks.update')
    .map(([, parameters]) => parameters as Record<string, unknown>);
  assert.equal(updates.length, 2);
  assert.ok(updates.every((parameters) => parameters.conclusion === 'failure'));
  assert.ok(updates.every((parameters) => parameters.status === 'completed'));
});
