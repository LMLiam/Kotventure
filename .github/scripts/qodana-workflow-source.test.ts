import assert from 'node:assert/strict';
import test from 'node:test';
import {
  validateQodanaWorkflowSource,
  type TrustedQodanaRun,
} from './qodana-publisher-validation.js';
import { asApiData } from './test-support/mocks.js';
import type { RepositoryData, WorkflowData, WorkflowRunData } from './shared/action-context.js';
import type { WorkflowRunEventRecord } from './shared/run-context.js';

const HEAD_SHA = 'a'.repeat(40);
const RUN_ID = 900;
const RUN_ATTEMPT = 3;

const repositoryRecord = {
  full_name: 'LMLiam/Kotventure',
  id: 1,
  default_branch: 'master',
};

const repository = asApiData<RepositoryData>(repositoryRecord);

const workflowRecord = {
  id: 77,
  name: 'Qodana',
  path: '.github/workflows/qodana.yml',
};

const workflow = asApiData<WorkflowData>(workflowRecord);

function makeEventRun(overrides: Partial<WorkflowRunEventRecord> = {}): WorkflowRunEventRecord {
  return {
    id: RUN_ID,
    run_attempt: RUN_ATTEMPT,
    workflow_id: workflow.id,
    event: 'pull_request_target',
    status: 'completed',
    conclusion: 'success',
    head_sha: HEAD_SHA,
    ...overrides,
  };
}

function makeRun(overrides: Partial<WorkflowRunData> = {}): WorkflowRunData {
  return asApiData<WorkflowRunData>({
    id: RUN_ID,
    run_attempt: RUN_ATTEMPT,
    workflow_id: workflow.id,
    event: 'pull_request_target',
    status: 'completed',
    conclusion: 'success',
    repository,
    head_repository: repository,
    head_branch: 'feature/security',
    head_sha: HEAD_SHA,
    ...overrides,
  });
}

interface WorkflowSourceOverrides {
  eventRun?: Partial<WorkflowRunEventRecord>;
  run?: Partial<WorkflowRunData>;
  workflow?: Partial<WorkflowData>;
  repository?: Partial<RepositoryData>;
}

function makeInputs(overrides: WorkflowSourceOverrides = {}): {
  eventRun: WorkflowRunEventRecord;
  run: WorkflowRunData;
  workflow: WorkflowData;
  repository: RepositoryData;
} {
  return {
    eventRun: makeEventRun(overrides.eventRun),
    run: makeRun(overrides.run),
    workflow: overrides.workflow ? asApiData<WorkflowData>({ ...workflowRecord, ...overrides.workflow }) : workflow,
    repository: overrides.repository ? asApiData<RepositoryData>({ ...repositoryRecord, ...overrides.repository }) : repository,
  };
}

function rejections(overrides: WorkflowSourceOverrides): () => TrustedQodanaRun {
  return () => validateQodanaWorkflowSource(makeInputs(overrides));
}

test('accepts a trusted pull-request-target Qodana run', () => {
  assert.deepEqual(validateQodanaWorkflowSource(makeInputs()), {
    runId: RUN_ID,
    runAttempt: RUN_ATTEMPT,
    repository: repository.full_name,
    conclusion: 'success',
  });
});

test('rejects event metadata that does not match the trusted run', () => {
  assert.throws(
    rejections({ eventRun: { id: RUN_ID + 1 } }),
    /workflow run id does not match/,
  );
  assert.throws(
    rejections({ eventRun: { run_attempt: RUN_ATTEMPT + 1 } }),
    /workflow run attempt does not match/,
  );
  assert.throws(
    rejections({ eventRun: { workflow_id: 78 } }),
    /workflow run workflow id does not match/,
  );
  assert.throws(
    rejections({ eventRun: { event: 'push' } }),
    /workflow run event does not match/,
  );
  assert.throws(
    rejections({ eventRun: { status: 'in_progress' } }),
    /workflow run event status does not match/,
  );
  assert.throws(
    rejections({
      eventRun: { conclusion: 'failure' },
      run: { conclusion: 'success' },
    }),
    /workflow run event conclusion does not match/,
  );
});

test('accepts every documented workflow-run conclusion', () => {
  for (const conclusion of ['action_required', 'cancelled', 'failure', 'neutral', 'skipped', 'stale', 'success', 'timed_out']) {
    const trusted = validateQodanaWorkflowSource(makeInputs({
      eventRun: { conclusion },
      run: { conclusion },
    }));
    assert.equal(trusted.conclusion, conclusion);
  }
});

test('rejects an unknown workflow-run conclusion', () => {
  assert.throws(
    rejections({
      eventRun: { conclusion: 'pending' },
      run: { conclusion: 'pending' },
    }),
    /workflow run conclusion is invalid/,
  );
});

test('rejects a run whose event is not pull-request-target', () => {
  assert.throws(
    rejections({ run: { event: 'push' } }),
    /workflow run event does not match/,
  );
  assert.throws(
    rejections({ run: { status: 'in_progress' } }),
    /workflow run status does not match/,
  );
});

test('rejects a run bound to a foreign repository or head repository', () => {
  assert.throws(
    rejections({
      run: { repository: asApiData<WorkflowRunData['repository']>({ ...repositoryRecord, full_name: 'other/repo' }) },
    }),
    /workflow run repository does not match/,
  );
  assert.throws(
    rejections({
      run: { repository: asApiData<WorkflowRunData['repository']>({ ...repositoryRecord, id: 2 }) },
    }),
    /workflow run repository id does not match/,
  );
  assert.throws(
    rejections({
      run: { head_repository: asApiData<WorkflowRunData['repository']>({ ...repositoryRecord, full_name: 'other/repo' }) },
    }),
    /workflow head repository does not match/,
  );
  assert.throws(
    rejections({
      run: { head_repository: asApiData<WorkflowRunData['repository']>({ ...repositoryRecord, id: 2 }) },
    }),
    /workflow head repository id does not match/,
  );
});

test('rejects a run with an invalid head branch or head SHA', () => {
  assert.throws(
    rejections({ run: { head_branch: '' } }),
    /workflow run head branch is invalid/,
  );
  assert.throws(
    rejections({ run: { head_sha: 'not-a-sha' } }),
    /workflow run head SHA is invalid/,
  );
});

test('rejects a trusted workflow whose identity does not match the run', () => {
  assert.throws(
    rejections({ workflow: { id: 78 } }),
    /workflow identity does not match/,
  );
  assert.throws(
    rejections({ workflow: { name: 'Untrusted Qodana' } }),
    /workflow name does not match/,
  );
  assert.throws(
    rejections({ workflow: { path: '.github/workflows/untrusted.yml' } }),
    /workflow path does not match/,
  );
});

test('rejects a repository that does not match the run binding', () => {
  assert.throws(
    rejections({ repository: { full_name: 'other/repo' } }),
    /workflow run repository does not match/,
  );
});
