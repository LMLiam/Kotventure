import assert from 'node:assert/strict';
import test from 'node:test';
import {
  buildCheckExternalId,
  completeWorkflowCheck,
  createWorkflowCheck,
  ensureWorkflowCheck,
  findWorkflowCheck,
  updateWorkflowCheck,
  workflowResultConclusion,
} from './workflow-run-check.js';
import type { WorkflowCheckAnnotation, WorkflowRunCheckContext } from './workflow-run-check.js';
import { mockOctokit } from './test-support/mocks.js';

const HEAD_SHA = 'a'.repeat(40);
const CONTEXT: WorkflowRunCheckContext = {
  repo: { owner: 'LMLiam', repo: 'Kotventure' },
  runId: 123,
  runAttempt: 2,
  serverUrl: 'https://github.com',
};

interface CheckParameters {
  owner: string;
  repo: string;
  check_run_id?: number;
  name?: string;
  head_sha?: string;
  status?: string;
  conclusion?: string;
  external_id?: string;
  details_url?: string;
  started_at?: string;
  completed_at?: string;
  output?: { title: string; summary: string; annotations?: WorkflowCheckAnnotation[] };
}

interface CreatedCheckRecord {
  id: number;
  name: string;
  head_sha: string;
  external_id: string;
  status?: string;
  conclusion?: string | null;
  app: { slug: string };
}

function makeAnnotations(count: number): WorkflowCheckAnnotation[] {
  return Array.from({ length: count }, (_, index) => ({
    path: `.github/scripts/file-${index}.ts`,
    start_line: index + 1,
    end_line: index + 1,
    annotation_level: 'notice' as const,
    title: `Annotation ${index}`,
    message: `Annotation message ${index}`,
  }));
}

function makeGithub(): {
  calls: Array<[string, CheckParameters]>;
  check: CreatedCheckRecord;
  setPaginatedChecks(checks: object[]): void;
  github: ReturnType<typeof mockOctokit>;
} {
  const calls: Array<[string, CheckParameters]> = [];
  let paginatedChecks: object[] = [];
  const check: CreatedCheckRecord = {
    id: 700,
    name: 'Qodana / trusted ref',
    head_sha: HEAD_SHA,
    external_id: 'workflow-run-check:qodana-trusted:123:2:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
    status: 'in_progress',
    conclusion: null,
    app: { slug: 'github-actions' },
  };
  const paginate = async (
    _method: (...args: never[]) => object,
    _parameters: never,
    map: (response: { data: object[] & { total_count: number } }, done: () => void) => object[],
  ): Promise<object[]> => {
    const data = Object.assign([...paginatedChecks], { total_count: paginatedChecks.length });
    return map({ data }, () => {});
  };
  return {
    calls,
    check,
    setPaginatedChecks(checks: object[]): void {
      paginatedChecks = checks;
    },
    github: mockOctokit({
      rest: {
        checks: {
          create: async (parameters: CheckParameters) => {
            calls.push(['create', parameters]);
            return { data: check };
          },
          get: async (parameters: CheckParameters) => {
            calls.push(['get', parameters]);
            return { data: check };
          },
          update: async (parameters: CheckParameters) => {
            calls.push(['update', parameters]);
            return { data: { ...check, ...parameters } };
          },
          listForRef: async () => ({ data: { check_runs: [] } }),
        },
      },
      paginate,
    }),
  };
}

test('creates an in-progress check against the validated source SHA', async () => {
  const fixture = makeGithub();
  const externalId = buildCheckExternalId({
    kind: 'qodana-trusted',
    runId: CONTEXT.runId,
    runAttempt: CONTEXT.runAttempt,
    headSha: HEAD_SHA,
  });
  const result = await createWorkflowCheck({
    github: fixture.github,
    context: CONTEXT,
    name: fixture.check.name,
    headSha: HEAD_SHA,
    externalId,
    summary: 'Qodana is analysing the validated trusted source.',
  });

  assert.deepEqual(result, {
    id: fixture.check.id,
    externalId,
    headSha: HEAD_SHA,
    name: fixture.check.name,
    status: 'in_progress',
    conclusion: null,
  });
  const first = fixture.calls[0];
  assert.ok(first);
  const [operation, parameters] = first;
  assert.equal(operation, 'create');
  assert.deepEqual({ ...parameters, started_at: '<timestamp>' }, {
    owner: 'LMLiam',
    repo: 'Kotventure',
    name: fixture.check.name,
    head_sha: HEAD_SHA,
    status: 'in_progress',
    external_id: externalId,
    details_url: 'https://github.com/LMLiam/Kotventure/actions/runs/123/attempts/2',
    started_at: '<timestamp>',
    output: {
      title: fixture.check.name,
      summary: 'Qodana is analysing the validated trusted source.',
    },
  });
  const startedAt = parameters.started_at;
  assert.ok(startedAt);
  assert.doesNotThrow(() => new Date(startedAt).toISOString());
});

test('rejects a created check from a foreign application', async () => {
  const fixture = makeGithub();
  fixture.check.app.slug = 'other-app';
  await assert.rejects(
    () => createWorkflowCheck({
      github: fixture.github,
      context: CONTEXT,
      name: fixture.check.name,
      headSha: HEAD_SHA,
      externalId: fixture.check.external_id,
      summary: 'Qodana is analysing the validated trusted source.',
    }),
    /created check application does not match/,
  );
});

test('rejects a created check bound to a foreign external id', async () => {
  const fixture = makeGithub();
  const externalId = fixture.check.external_id;
  fixture.check.external_id = `workflow-run-check:qodana-trusted:1:1:${'b'.repeat(40)}`;
  await assert.rejects(
    () => createWorkflowCheck({
      github: fixture.github,
      context: CONTEXT,
      name: fixture.check.name,
      headSha: HEAD_SHA,
      externalId,
      summary: 'Qodana is analysing the validated trusted source.',
    }),
    /created check external id does not match/,
  );
});

test('rejects a created check bound to a foreign source SHA', async () => {
  const fixture = makeGithub();
  fixture.check.head_sha = 'b'.repeat(40);
  await assert.rejects(
    () => createWorkflowCheck({
      github: fixture.github,
      context: CONTEXT,
      name: fixture.check.name,
      headSha: HEAD_SHA,
      externalId: fixture.check.external_id,
      summary: 'Qodana is analysing the validated trusted source.',
    }),
    /created check head SHA does not match/,
  );
});

test('rejects an invalid check kind', () => {
  assert.throws(
    () => buildCheckExternalId({
      kind: 'Qodana PR',
      runId: 1,
      runAttempt: 1,
      headSha: HEAD_SHA,
    }),
    /check kind is invalid/,
  );
});

test('validates and completes the registered check', async () => {
  const fixture = makeGithub();
  await completeWorkflowCheck({
    github: fixture.github,
    context: CONTEXT,
    checkId: fixture.check.id,
    name: fixture.check.name,
    headSha: HEAD_SHA,
    externalId: fixture.check.external_id,
    conclusion: 'success',
    summary: 'Qodana analysed the validated trusted source.',
  });

  assert.deepEqual(fixture.calls[0], ['get', {
    owner: 'LMLiam',
    repo: 'Kotventure',
    check_run_id: fixture.check.id,
  }]);
  const second = fixture.calls[1];
  assert.ok(second);
  const [operation, parameters] = second;
  assert.equal(operation, 'update');
  assert.deepEqual({ ...parameters, completed_at: '<timestamp>' }, {
    owner: 'LMLiam',
    repo: 'Kotventure',
    check_run_id: fixture.check.id,
    status: 'completed',
    conclusion: 'success',
    completed_at: '<timestamp>',
    details_url: 'https://github.com/LMLiam/Kotventure/actions/runs/123/attempts/2',
    output: {
      title: fixture.check.name,
      summary: 'Qodana analysed the validated trusted source.',
    },
  });
  const completedAt = parameters.completed_at;
  assert.ok(completedAt);
  assert.doesNotThrow(() => new Date(completedAt).toISOString());
});

test('rejects a check that is not bound to the expected source', async () => {
  const fixture = makeGithub();
  fixture.check.head_sha = 'b'.repeat(40);

  await assert.rejects(
    () => completeWorkflowCheck({
      github: fixture.github,
      context: CONTEXT,
      checkId: fixture.check.id,
      name: fixture.check.name,
      headSha: HEAD_SHA,
      externalId: fixture.check.external_id,
      conclusion: 'failure',
      summary: 'Qodana failed.',
    }),
    /check head SHA does not match/,
  );
  assert.equal(fixture.calls.length, 1);
});

test('reuses the one existing check for an external id', async () => {
  const fixture = makeGithub();
  const externalId = fixture.check.external_id;
  fixture.setPaginatedChecks([{
    ...fixture.check,
    status: 'queued',
    conclusion: null,
  }]);

  const result = await ensureWorkflowCheck({
    github: fixture.github,
    context: CONTEXT,
    name: fixture.check.name,
    headSha: HEAD_SHA,
    externalId,
    summary: 'Waiting for the trusted publisher.',
  });

  assert.equal(result.id, fixture.check.id);
  assert.equal(result.status, 'queued');
  assert.equal(fixture.calls.length, 0);
});

test('rejects duplicate existing checks for an external id', async () => {
  const fixture = makeGithub();
  fixture.setPaginatedChecks([fixture.check, { ...fixture.check, id: fixture.check.id + 1 }]);

  await assert.rejects(
    () => findWorkflowCheck({
      github: fixture.github,
      context: CONTEXT,
      name: fixture.check.name,
      headSha: HEAD_SHA,
      externalId: fixture.check.external_id,
    }),
    /duplicate workflow check external id/,
  );
});

test('finds a matching check returned after the first page', async () => {
  const fixture = makeGithub();
  const externalId = fixture.check.external_id;
  fixture.setPaginatedChecks([
    ...Array.from({ length: 100 }, (_, index) => ({
      ...fixture.check,
      id: fixture.check.id + index + 1,
      external_id: `workflow-run-check:other:${index}`,
    })),
    fixture.check,
  ]);

  const result = await findWorkflowCheck({
    github: fixture.github,
    context: CONTEXT,
    name: fixture.check.name,
    headSha: HEAD_SHA,
    externalId,
  });

  assert.equal(result?.id, fixture.check.id);
});

test('completing an already completed check with the same conclusion is idempotent', async () => {
  const fixture = makeGithub();
  fixture.check.status = 'completed';
  fixture.check.conclusion = 'success';

  await completeWorkflowCheck({
    github: fixture.github,
    context: CONTEXT,
    checkId: fixture.check.id,
    name: fixture.check.name,
    headSha: HEAD_SHA,
    externalId: fixture.check.external_id,
    conclusion: 'success',
    summary: 'The trusted publication succeeded.',
  });

  assert.equal(fixture.calls.length, 1);
  assert.equal(fixture.calls[0]?.[0], 'get');
});

test('sends at most 50 annotations per in-progress update', async () => {
  const fixture = makeGithub();

  await updateWorkflowCheck({
    github: fixture.github,
    context: CONTEXT,
    checkId: fixture.check.id,
    name: fixture.check.name,
    headSha: HEAD_SHA,
    externalId: fixture.check.external_id,
    status: 'in_progress',
    summary: 'The trusted publisher is validating the report.',
    annotations: makeAnnotations(51),
  });

  const updates = fixture.calls.filter(([operation]) => operation === 'update').map(([, parameters]) => parameters);
  assert.deepEqual(updates.map((parameters) => parameters.output?.annotations?.length), [50, 1]);
  assert.deepEqual(updates.map((parameters) => parameters.status), ['in_progress', 'in_progress']);
});

test('completes only the final annotation batch', async () => {
  const fixture = makeGithub();

  await completeWorkflowCheck({
    github: fixture.github,
    context: CONTEXT,
    checkId: fixture.check.id,
    name: fixture.check.name,
    headSha: HEAD_SHA,
    externalId: fixture.check.external_id,
    conclusion: 'success',
    summary: 'The trusted publication succeeded.',
    annotations: makeAnnotations(51),
  });

  const updates = fixture.calls.filter(([operation]) => operation === 'update').map(([, parameters]) => parameters);
  assert.deepEqual(updates.map((parameters) => parameters.output?.annotations?.length), [50, 1]);
  assert.deepEqual(updates.map((parameters) => parameters.status), ['in_progress', 'completed']);
  assert.equal(updates[0]?.conclusion, undefined);
  assert.equal(updates[1]?.conclusion, 'success');
});

test('does not split exactly 50 annotations', async () => {
  const fixture = makeGithub();

  await completeWorkflowCheck({
    github: fixture.github,
    context: CONTEXT,
    checkId: fixture.check.id,
    name: fixture.check.name,
    headSha: HEAD_SHA,
    externalId: fixture.check.external_id,
    conclusion: 'failure',
    summary: 'The trusted publication failed.',
    annotations: makeAnnotations(50),
  });

  const updates = fixture.calls.filter(([operation]) => operation === 'update').map(([, parameters]) => parameters);
  assert.equal(updates.length, 1);
  assert.equal(updates[0]?.output?.annotations?.length, 50);
  assert.equal(updates[0]?.status, 'completed');
});

test('stops annotation completion when an earlier batch fails', async () => {
  const fixture = makeGithub();
  Object.assign(fixture.github.rest.checks, {
    update: async (parameters: CheckParameters) => {
      fixture.calls.push(['update', parameters]);
      throw new Error('annotation update failed');
    },
  });

  await assert.rejects(
    () => completeWorkflowCheck({
      github: fixture.github,
      context: CONTEXT,
      checkId: fixture.check.id,
      name: fixture.check.name,
      headSha: HEAD_SHA,
      externalId: fixture.check.external_id,
      conclusion: 'success',
      summary: 'The trusted publication succeeded.',
      annotations: makeAnnotations(51),
    }),
    /annotation update failed/,
  );

  const updates = fixture.calls.filter(([operation]) => operation === 'update').map(([, parameters]) => parameters);
  assert.equal(updates.length, 1);
  assert.equal(updates[0]?.status, 'in_progress');
});

test('maps workflow results to supported check conclusions', () => {
  assert.equal(workflowResultConclusion('success'), 'success');
  assert.equal(workflowResultConclusion('failure'), 'failure');
  assert.equal(workflowResultConclusion('cancelled'), 'cancelled');
  assert.equal(workflowResultConclusion('skipped'), 'skipped');
  assert.equal(workflowResultConclusion('timed_out'), 'timed_out');
  assert.throws(() => workflowResultConclusion('pending'), /workflow result is invalid/);
});
