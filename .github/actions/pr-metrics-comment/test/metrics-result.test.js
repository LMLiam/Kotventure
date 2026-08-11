'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const {
  MAX_REF_LENGTH,
  MAX_RESULT_BYTES,
  deserializeMetricsResult,
  serializeMetricsResult,
  validateMetricsResult,
} = require('../lib/metrics-result.js');

const BASE_SHA = 'a'.repeat(40);
const HEAD_SHA = 'b'.repeat(40);

function makeContext() {
  return {
    repo: { owner: 'LMLiam', repo: 'Kotventure' },
    eventName: 'pull_request',
    payload: {
      pull_request: {
        number: 42,
        base: {
          repo: { full_name: 'LMLiam/Kotventure' },
          ref: 'master',
          sha: BASE_SHA,
        },
        head: {
          repo: { full_name: 'LMLiam/Kotventure-fix' },
          ref: 'fix/metrics',
          sha: HEAD_SHA,
        },
      },
    },
  };
}

function makeResult(context = makeContext()) {
  return serializeMetricsResult({
    context,
    runId: '100',
    runAttempt: '2',
    headCoverage: {
      modules: new Map([['core', { missed: 1, covered: 9 }]]),
      totalMissed: 1,
      totalCovered: 9,
      files: new Map([['Foo.kt', new Map([[1, true]])]]),
    },
    baseCoverage: null,
    headJars: new Map([['core', { size: 128, classes: 3 }]]),
    baseJars: new Map(),
    headMetrics: { tests: 10, skipped: 1, durationSeconds: null },
    baseMetrics: null,
    patchCoverage: { covered: 3, missed: 1, uncovered: [{ path: 'src/Secret.kt', ranges: [[9, 9]] }] },
    apiSurface: { added: ['public fun visible(): String'], removed: [] },
  });
}

test('serialises only bounded data needed by the trusted report', () => {
  const result = makeResult();

  assert.deepEqual(Object.keys(result), ['schemaVersion', 'provenance', 'metrics']);
  assert.deepEqual(Object.keys(result.metrics.headCoverage), ['modules', 'totalMissed', 'totalCovered']);
  assert.equal('files' in result.metrics.headCoverage, false);
  assert.deepEqual(result.metrics.patchCoverage, { covered: 3, missed: 1 });
  assert.deepEqual(result.metrics.headMetrics, { tests: 10, skipped: 1, durationSeconds: null });
  assert.equal('uncovered' in result.metrics.patchCoverage, false);
  assert.equal('path' in result.metrics, false);
  assert.equal('links' in result, false);

  const decoded = deserializeMetricsResult(result);
  assert.deepEqual([...decoded.headCoverage.modules.entries()], [['core', { missed: 1, covered: 9 }]]);
  assert.deepEqual(decoded.patchCoverage, { covered: 3, missed: 1, uncovered: [] });
});

test('rejects unknown report properties', () => {
  const result = makeResult();
  result.metrics.patchCoverage.uncovered = [];
  assert.throws(() => validateMetricsResult(result), /unexpected properties/);
});

test('reports the exact bound for too-small and too-big counters', () => {
  const tooSmall = makeResult();
  tooSmall.metrics.headCoverage.totalMissed = -1;
  assert.throws(
    () => validateMetricsResult(tooSmall),
    /must be an integer from 0 to 1000000000/,
  );

  const tooBig = makeResult();
  tooBig.metrics.headCoverage.totalMissed = 1000000001;
  assert.throws(
    () => validateMetricsResult(tooBig),
    /must be an integer from 0 to 1000000000/,
  );
});

test('reports the exact bound for run attempt numbers', () => {
  const tooSmall = makeResult();
  tooSmall.provenance.runAttempt = 0;
  assert.throws(
    () => validateMetricsResult(tooSmall),
    /provenance\.runAttempt must be an integer from 1 to 1000/,
  );

  const tooBig = makeResult();
  tooBig.provenance.runAttempt = 1001;
  assert.throws(
    () => validateMetricsResult(tooBig),
    /provenance\.runAttempt must be an integer from 1 to 1000/,
  );
});

test('rejects a mismatched schema version literal', () => {
  const result = makeResult();
  result.schemaVersion = 2;
  assert.throws(
    () => validateMetricsResult(result),
    /schemaVersion has an invalid value/,
  );
});

test('rejects unsafe declarations and oversized declaration lists', () => {
  const unsafe = makeResult();
  unsafe.metrics.apiSurface.added[0] = 'public fun visible(): String `malicious`';
  assert.throws(() => validateMetricsResult(unsafe), /invalid value/);

  const oversized = makeResult();
  oversized.metrics.apiSurface.added = Array.from({ length: 101 }, () => 'public fun item()');
  assert.throws(() => validateMetricsResult(oversized), /too many entries/);
});

test('rejects duplicate modules and invalid counters', () => {
  const duplicate = makeResult();
  duplicate.metrics.headCoverage.modules.push({ name: 'core', missed: 0, covered: 1 });
  assert.throws(() => validateMetricsResult(duplicate), /duplicate name/);

  const invalid = makeResult();
  invalid.metrics.headCoverage.totalMissed = -1;
  assert.throws(() => validateMetricsResult(invalid), /must be an integer/);

  const invalidModules = makeResult();
  invalidModules.metrics.headCoverage.modules = {};
  assert.throws(() => validateMetricsResult(invalidModules), /modules must be an array/);

  const invalidJars = makeResult();
  invalidJars.metrics.headJars = {};
  assert.throws(() => validateMetricsResult(invalidJars), /headJars must be an array/);
});

test('rejects malformed run numbers before writing a result', () => {
  assert.throws(() => serializeMetricsResult({
    context: makeContext(),
    runId: '100x',
    runAttempt: '2',
    headCoverage: null,
    baseCoverage: null,
    headJars: new Map(),
    baseJars: new Map(),
    headMetrics: null,
    baseMetrics: null,
    patchCoverage: null,
    apiSurface: null,
  }), /runId must be a positive integer/);
});

test('accepts valid Git ref characters in provenance', () => {
  const context = makeContext();
  context.payload.pull_request.head.ref = 'fix/issue#1+build';
  assert.doesNotThrow(() => serializeMetricsResult({
    context,
    runId: '100',
    runAttempt: '2',
    headCoverage: null,
    baseCoverage: null,
    headJars: new Map([['core', { size: 1, classes: null }]]),
    baseJars: new Map(),
    headMetrics: null,
    baseMetrics: null,
    patchCoverage: null,
    apiSurface: null,
  }));
});

test('rejects unsafe and oversized Git refs', () => {
  const controlCharacter = makeContext();
  controlCharacter.payload.pull_request.head.ref = `fix/${String.fromCodePoint(0x85)}metrics`;
  assert.throws(() => makeResult(controlCharacter), /headRef has an invalid value/);

  const oversized = makeContext();
  oversized.payload.pull_request.head.ref = 'a'.repeat(MAX_REF_LENGTH + 1);
  assert.throws(() => makeResult(oversized), /headRef has an invalid value/);
});

test('rejects missing pull-request repositories before reading provenance', () => {
  const missingHeadRepository = makeContext();
  delete missingHeadRepository.payload.pull_request.head.repo;
  assert.throws(() => makeResult(missingHeadRepository), /requires a pull_request payload/);

  const missingPullRequest = makeContext();
  delete missingPullRequest.payload.pull_request;
  assert.throws(() => makeResult(missingPullRequest), /requires a pull_request payload/);
});

test('rejects invisible characters in declarations', () => {
  const result = makeResult();
  result.metrics.apiSurface.added[0] = `public fun visible(): String ${String.fromCodePoint(0x202e)}`;
  assert.throws(() => validateMetricsResult(result), /invalid value/);
});

test('rejects a serialised result over the byte limit', () => {
  const result = makeContext();
  assert.throws(() => serializeMetricsResult({
    context: result,
    runId: '100',
    runAttempt: '2',
    headCoverage: null,
    baseCoverage: null,
    headJars: new Map(),
    baseJars: new Map(),
    headMetrics: null,
    baseMetrics: null,
    patchCoverage: null,
    apiSurface: {
      added: Array.from({ length: 100 }, () => String.fromCodePoint(0x800).repeat(120)),
      removed: Array.from({ length: 100 }, () => String.fromCodePoint(0x800).repeat(120)),
    },
  }), new RegExp(`exceeds ${MAX_RESULT_BYTES} bytes`));
});
