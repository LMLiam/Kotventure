'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { buildReport } = require('../lib/report.js');

function coverage(modules, totalMissed, totalCovered) {
  return { modules: new Map(Object.entries(modules)), totalMissed, totalCovered, files: new Map() };
}

function jars(entries) {
  return new Map(Object.entries(entries).map(([m, size]) => [m, { size, classes: null }]));
}

const defaults = {
  headCoverage: null,
  baseCoverage: null,
  headJars: new Map(),
  baseJars: new Map(),
  growthThreshold: 10,
  baseLabel: 'master@abc1234',
};

test('renders coverage deltas with chart, table, and verdict', () => {
  const { body } = buildReport({
    ...defaults,
    headCoverage: coverage({ core: { missed: 10, covered: 90 } }, 10, 90),
    baseCoverage: coverage({ core: { missed: 20, covered: 80 } }, 20, 80),
    gateThreshold: 85,
  });
  assert.ok(body.includes('✅ Coverage 90.0% (+10.0pp · gate 85%)'));
  assert.ok(body.includes('vs **master@abc1234**'));
  assert.ok(body.includes('xychart-beta'));
  assert.ok(body.includes('| core | 90.0% | 80.0% | +10.0pp |'));
  assert.ok(body.includes('| **Total** | **90.0%** | **80.0%** | **+10.0pp** |'));
});

test('sorts chart bars by delta magnitude', () => {
  const { body } = buildReport({
    ...defaults,
    headCoverage: coverage({
      alpha: { missed: 9, covered: 91 },
      beta: { missed: 5, covered: 95 },
    }, 14, 186),
    baseCoverage: coverage({
      alpha: { missed: 10, covered: 90 },
      beta: { missed: 10, covered: 90 },
    }, 20, 180),
  });
  assert.ok(body.includes('x-axis ["beta", "alpha"]'));
});

test('warns on total coverage drop and near-gate coverage', () => {
  const { body, warnings } = buildReport({
    ...defaults,
    headCoverage: coverage({ core: { missed: 148, covered: 852 } }, 148, 852),
    baseCoverage: coverage({ core: { missed: 100, covered: 900 } }, 100, 900),
    gateThreshold: 85,
  });
  assert.equal(warnings.length, 2);
  assert.ok(body.startsWith('## CI metrics\n\n⚠️ '));
  assert.ok(body.includes('> [!WARNING]'));
  assert.ok(body.includes('Total coverage dropped -4.8pp'));
  assert.ok(body.includes('within 0.5pp of the 85% gate'));
});

test('renders patch coverage with uncovered ranges and verdict', () => {
  const { body } = buildReport({
    ...defaults,
    headCoverage: coverage({ core: { missed: 10, covered: 90 } }, 10, 90),
    patchCoverage: {
      covered: 41,
      missed: 4,
      uncovered: [{ path: 'modules/core/src/main/kotlin/a/Foo.kt', ranges: [[12, 15]] }],
    },
  });
  assert.ok(body.includes('### Patch coverage — 41/45 changed lines covered (91.1%)'));
  assert.ok(body.includes('`modules/core/src/main/kotlin/a/Foo.kt`: 12–15'));
  assert.ok(body.includes('patch 91.1%'));
});

test('notes when no executable lines changed', () => {
  const { body } = buildReport({
    ...defaults,
    headCoverage: coverage({ core: { missed: 10, covered: 90 } }, 10, 90),
    patchCoverage: { covered: 0, missed: 0, uncovered: [] },
  });
  assert.ok(body.includes('_No executable changed lines._'));
  assert.ok(!body.includes('patch '));
});

test('renders jar deltas with class counts and aggregate verdict', () => {
  const headJars = new Map([['core', { size: 1100, classes: 12 }]]);
  const baseJars = new Map([['core', { size: 1000, classes: 10 }]]);
  const { body } = buildReport({ ...defaults, headJars, baseJars });
  assert.ok(body.includes('| core | 1.1 KB | 1.0 KB | +10.0% | 12 (+2) |'));
  assert.ok(body.includes('📦 jars +10.0%'));
});

test('marks new and removed modules in the jar table', () => {
  const { body } = buildReport({
    ...defaults,
    headJars: jars({ core: 1024, fresh: 2048 }),
    baseJars: jars({ core: 1024, gone: 512 }),
  });
  assert.ok(body.includes('| fresh | 2.0 KB | — | new | — |'));
  assert.ok(body.includes('| gone | — | 0.5 KB | removed | — |'));
});

test('warns when jar growth exceeds the threshold', () => {
  const { body, warnings } = buildReport({
    ...defaults,
    headJars: jars({ core: 1200 }),
    baseJars: jars({ core: 1000 }),
  });
  assert.deepEqual(warnings, ['core jar +20.0% (>10% growth)']);
  assert.ok(body.includes('> [!WARNING]'));
});

test('renders API surface delta as a diff block', () => {
  const { body } = buildReport({
    ...defaults,
    headJars: jars({ core: 1024 }),
    apiSurface: { added: ['public fun a(): Int'], removed: ['public fun b(): Int'] },
  });
  assert.ok(body.includes('### Public API — +1 / −1 declarations'));
  assert.ok(body.includes('+ public fun a(): Int'));
  assert.ok(body.includes('- public fun b(): Int'));
  assert.ok(body.includes('API +1/−1'));
});

test('renders test counts and duration in build stats', () => {
  const { body } = buildReport({
    ...defaults,
    headJars: jars({ core: 1024 }),
    headMetrics: { tests: 1234, skipped: 1, durationSeconds: 242 },
    baseMetrics: { tests: 1222, skipped: 1, durationSeconds: 60 },
  });
  assert.ok(body.includes('🧪 1234 tests (+12)'));
  assert.ok(body.includes('| Tests | 1234 | 1222 |'));
  assert.ok(body.includes('| Build time (indicative) | 4m02s | 1m00s |'));
});

test('collapses to a one-line body when nothing changed', () => {
  const { body } = buildReport({
    ...defaults,
    headCoverage: coverage({ core: { missed: 10, covered: 90 } }, 10, 90),
    baseCoverage: coverage({ core: { missed: 10, covered: 90 } }, 10, 90),
    headJars: jars({ core: 1024 }),
    baseJars: jars({ core: 1024 }),
  });
  assert.ok(body.includes('_No metric changes._'));
  assert.ok(!body.includes('xychart-beta'));
  assert.ok(!body.includes('### Coverage'));
});

test('renders footer links when provided', () => {
  const { body } = buildReport({
    ...defaults,
    headJars: jars({ core: 1024 }),
    headSha: 'abc1234',
    links: { run: 'https://r', dokka: 'https://d', tests: 'https://t' },
  });
  assert.ok(body.includes('Updated for `abc1234` · [Run](https://r) · [Dokka preview](https://d) · [Test results](https://t)'));
});
