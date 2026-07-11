'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { buildReport } = require('../lib/report.js');

function coverage(modules, totalMissed, totalCovered) {
  return { modules: new Map(Object.entries(modules)), totalMissed, totalCovered };
}

const defaults = {
  headCoverage: null,
  baseCoverage: null,
  headJars: new Map(),
  baseJars: new Map(),
  growthThreshold: 10,
  baseLabel: 'master@abc1234',
};

test('renders coverage deltas with chart and table', () => {
  const { body } = buildReport({
    ...defaults,
    headCoverage: coverage({ core: { missed: 10, covered: 90 } }, 10, 90),
    baseCoverage: coverage({ core: { missed: 20, covered: 80 } }, 20, 80),
  });
  assert.ok(body.includes('vs **master@abc1234**'));
  assert.ok(body.includes('xychart-beta'));
  assert.ok(body.includes('| core | 90.0% | 80.0% | +10.0pp |'));
  assert.ok(body.includes('| **Total** | **90.0%** | **80.0%** | **+10.0pp** |'));
});

test('omits the coverage chart when base coverage is missing', () => {
  const { body } = buildReport({
    ...defaults,
    headCoverage: coverage({ core: { missed: 10, covered: 90 } }, 10, 90),
  });
  assert.ok(body.includes('Base coverage unavailable'));
  assert.ok(!body.includes('xychart-beta'));
});

test('omits the coverage chart when nothing changed', () => {
  const { body } = buildReport({
    ...defaults,
    headCoverage: coverage({ core: { missed: 10, covered: 90 } }, 10, 90),
    baseCoverage: coverage({ core: { missed: 10, covered: 90 } }, 10, 90),
  });
  assert.ok(body.includes('No per-module coverage delta'));
  assert.ok(!body.includes('xychart-beta'));
});

test('marks new and removed modules in the jar table', () => {
  const { body } = buildReport({
    ...defaults,
    headJars: new Map([['core', 1024], ['fresh', 2048]]),
    baseJars: new Map([['core', 1024], ['gone', 512]]),
  });
  assert.ok(body.includes('| fresh | 2.0 KB | — | new |'));
  assert.ok(body.includes('| gone | — | 0.5 KB | removed |'));
});

test('warns when jar growth exceeds the threshold', () => {
  const { body, warnings } = buildReport({
    ...defaults,
    headJars: new Map([['core', 1200]]),
    baseJars: new Map([['core', 1000]]),
  });
  assert.deepEqual(warnings, ['core: +20.0%']);
  assert.ok(body.includes('> [!WARNING]'));
  assert.ok(body.includes('Modules exceeding 10% JAR growth'));
});

test('stays quiet below the threshold', () => {
  const { body, warnings } = buildReport({
    ...defaults,
    headJars: new Map([['core', 1050]]),
    baseJars: new Map([['core', 1000]]),
  });
  assert.deepEqual(warnings, []);
  assert.ok(!body.includes('[!WARNING]'));
});
