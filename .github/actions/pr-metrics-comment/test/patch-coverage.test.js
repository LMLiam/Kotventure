'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { computePatchCoverage } = require('../lib/patch-coverage.js');

const pkg = 'io/github/lmliam/kotventure/core/text';

function patchFor(path, lineNumbers) {
  return { path, addedLines: lineNumbers.map((line) => ({ line, text: 'x' })), removedText: [] };
}

test('counts covered and missed added lines, skipping non-executable ones', () => {
  const coverageFiles = new Map([
    [`${pkg}/Foo.kt`, new Map([[10, true], [11, false], [12, false]])],
  ]);
  const patches = [patchFor(`modules/core/src/main/kotlin/${pkg}/Foo.kt`, [9, 10, 11, 12, 13])];
  const result = computePatchCoverage(patches, coverageFiles);
  assert.equal(result.covered, 1);
  assert.equal(result.missed, 2);
  assert.deepEqual(result.uncovered, [
    { path: `modules/core/src/main/kotlin/${pkg}/Foo.kt`, ranges: [[11, 12]] },
  ]);
});

test('groups non-consecutive missed lines into separate ranges', () => {
  const coverageFiles = new Map([
    [`${pkg}/Foo.kt`, new Map([[1, false], [2, false], [5, false]])],
  ]);
  const patches = [patchFor(`modules/core/src/main/kotlin/${pkg}/Foo.kt`, [1, 2, 5])];
  const result = computePatchCoverage(patches, coverageFiles);
  assert.deepEqual(result.uncovered[0].ranges, [[1, 2], [5, 5]]);
});

test('ignores test sources, non-kotlin files, and files absent from the report', () => {
  const coverageFiles = new Map([[`${pkg}/Foo.kt`, new Map([[1, false]])]]);
  const patches = [
    patchFor(`modules/core/src/test/kotlin/${pkg}/FooTest.kt`, [1]),
    patchFor('.github/workflows/ci.yml', [1]),
    patchFor(`modules/core/src/main/kotlin/${pkg}/Unreported.kt`, [1]),
  ];
  const result = computePatchCoverage(patches, coverageFiles);
  assert.equal(result.covered + result.missed, 0);
  assert.deepEqual(result.uncovered, []);
});
