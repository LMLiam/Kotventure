import assert from 'node:assert/strict';
import test from 'node:test';
import {
  buildCheckExternalId,
  completeWorkflowCheck,
  createWorkflowCheck,
  ensureWorkflowCheck,
  findWorkflowCheck,
  workflowResultConclusion,
} from './workflow-run-check.js';
import type { WorkflowRunCheckContext } from './workflow-run-check.js';
import { asApiData, mockOctokit } from './test-support/mocks.js';

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
  output?: { title: string; summary: string };
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

function makeGithub(): {
  calls: Array<[string, CheckParameters]>;
  check: CreatedCheckRecord;
  github: ReturnType<typeof mockOctokit>;
} {
  const calls: Array<[string, CheckParameters]> = [];
  const check: CreatedCheckRecord = {
    id: 700,
    name: 'Qodana / trusted ref',
    head_sha: HEAD_SHA,
    external_id: 'workflow-run-check:qodana-trusted:123:2:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
    status: 'in_progress',
    conclusion: null,
    app: { slug: 'github-actions' },
  };
  return {
    calls,
    check,
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
  Object.assign(fixture.github.rest.checks, {
    listForRef: async () => asApiData<Awaited<ReturnType<typeof fixture.github.rest.checks.listForRef>>>({
      data: {
        total_count: 1,
        check_runs: [{
          ...fixture.check,
          status: 'queued',
          conclusion: null,
        }],
      },
    }),
  });

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
  Object.assign(fixture.github.rest.checks, {
    listForRef: async () => asApiData<Awaited<ReturnType<typeof fixture.github.rest.checks.listForRef>>>({
      data: {
        total_count: 2,
        check_runs: [fixture.check, { ...fixture.check, id: fixture.check.id + 1 }],
      },
    }),
  });

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

test('maps workflow results to supported check conclusions', () => {
  assert.equal(workflowResultConclusion('success'), 'success');
  assert.equal(workflowResultConclusion('failure'), 'failure');
  assert.equal(workflowResultConclusion('cancelled'), 'cancelled');
  assert.equal(workflowResultConclusion('skipped'), 'skipped');
  assert.equal(workflowResultConclusion('timed_out'), 'timed_out');
  assert.throws(() => workflowResultConclusion('pending'), /workflow result is invalid/);
});
