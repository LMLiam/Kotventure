'use strict';

const assert = require('node:assert/strict');
const test = require('node:test');

const {
  buildCheckExternalId,
  completeWorkflowCheck,
  createWorkflowCheck,
  workflowResultConclusion,
} = require('./workflow-run-check.js');

const HEAD_SHA = 'a'.repeat(40);
const CONTEXT = {
  repo: { owner: 'LMLiam', repo: 'Kotventure' },
  runId: 123,
  runAttempt: 2,
  serverUrl: 'https://github.com',
};

function makeGithub() {
  const calls = [];
  const check = {
    id: 700,
    name: 'Qodana / trusted ref',
    head_sha: HEAD_SHA,
    external_id: 'workflow-run-check:qodana-trusted:123:2:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
    app: { slug: 'github-actions' },
  };
  return {
    calls,
    check,
    github: {
      rest: {
        checks: {
          create: async (parameters) => {
            calls.push(['create', parameters]);
            return { data: check };
          },
          get: async (parameters) => {
            calls.push(['get', parameters]);
            return { data: check };
          },
          update: async (parameters) => {
            calls.push(['update', parameters]);
            return { data: { ...check, ...parameters } };
          },
        },
      },
    },
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
  });
  const [operation, parameters] = fixture.calls[0];
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
  assert.doesNotThrow(() => new Date(parameters.started_at).toISOString());
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
  const [operation, parameters] = fixture.calls[1];
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
  assert.doesNotThrow(() => new Date(parameters.completed_at).toISOString());
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

test('maps workflow results to supported check conclusions', () => {
  assert.equal(workflowResultConclusion('success'), 'success');
  assert.equal(workflowResultConclusion('failure'), 'failure');
  assert.equal(workflowResultConclusion('cancelled'), 'cancelled');
  assert.equal(workflowResultConclusion('skipped'), 'skipped');
  assert.equal(workflowResultConclusion('timed_out'), 'timed_out');
  assert.throws(() => workflowResultConclusion('pending'), /workflow result is invalid/);
});
