import assert from 'node:assert/strict';
import test from 'node:test';
import type { JobItem, WorkflowRunData } from './shared/action-context.js';
import { asApiData } from './test-support/mocks.js';
import {
  buildResult,
  junitPublicationApplies,
  observationForBuild,
  observationForVanilla,
} from './junit-publisher.js';

const HEAD_SHA = 'a'.repeat(40);

function run(status: 'in_progress' | 'completed', conclusion: string | null = null): WorkflowRunData {
  return asApiData<WorkflowRunData>({
    id: 123,
    run_attempt: 1,
    status,
    conclusion,
    head_sha: HEAD_SHA,
  });
}

function job(name: string, status: 'queued' | 'in_progress' | 'completed', conclusion: string | null): JobItem {
  return asApiData<JobItem>({ name, status, conclusion });
}

test('waits for every Build shard before publishing an aggregate result', () => {
  const jobs = [
    job('Build (core)', 'completed', 'success'),
    job('Build (text)', 'in_progress', null),
    job('Build (runtime)', 'completed', 'success'),
  ];
  const pending = observationForBuild(jobs, run('in_progress'));
  assert.equal(pending.ready, false);
  assert.equal(buildResult(jobs, run('in_progress')), null);

  const complete = observationForBuild([
    jobs[0]!,
    job('Build (text)', 'completed', 'success'),
    jobs[2]!,
  ], run('completed', 'success'));
  assert.equal(complete.ready, true);
  assert.equal(complete.result, 'success');
});

test('maps Build shard failures, cancellation, and timeout', () => {
  const names = ['Build (core)', 'Build (text)', 'Build (runtime)'];
  const failures = names.map((name) => job(name, 'completed', name === 'Build (text)' ? 'failure' : 'success'));
  assert.equal(observationForBuild(failures, run('completed', 'failure')).result, 'failure');
  const cancelled = names.map((name) => job(name, 'completed', name === 'Build (core)' ? 'cancelled' : 'success'));
  assert.equal(observationForBuild(cancelled, run('completed', 'cancelled')).result, 'cancelled');
  const timedOut = names.map((name) => job(name, 'completed', name === 'Build (runtime)' ? 'timed_out' : 'success'));
  assert.equal(observationForBuild(timedOut, run('completed', 'timed_out')).result, 'timed_out');
});

test('observes Vanilla independently from Build', () => {
  const pending = observationForVanilla([job('Vanilla conformance', 'in_progress', null)], run('in_progress'));
  assert.equal(pending.ready, false);
  const skipped = observationForVanilla([job('Vanilla conformance', 'completed', 'skipped')], run('completed', 'success'));
  assert.equal(skipped.result, 'skipped');
});

test('does not publish JUnit for non-code pull-request classifications', () => {
  assert.equal(junitPublicationApplies('documentation'), false);
  assert.equal(junitPublicationApplies('release'), false);
  assert.equal(junitPublicationApplies('code'), true);
});
