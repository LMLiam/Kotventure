'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { parsePatches } = require('../lib/patch.js');

test('tracks added line numbers across context and removals', () => {
  const patch = [
    '@@ -1,4 +1,5 @@',
    ' context',
    '-old line',
    '+new line',
    '+second new',
    ' more context',
  ].join('\n');
  const [file] = parsePatches([{ filename: 'Foo.kt', status: 'modified', patch }]);
  assert.deepEqual(file.addedLines, [
    { line: 2, text: 'new line' },
    { line: 3, text: 'second new' },
  ]);
  assert.deepEqual(file.removedText, ['old line']);
});

test('handles multiple hunks with correct offsets', () => {
  const patch = [
    '@@ -1,2 +1,3 @@',
    ' a',
    '+b',
    ' c',
    '@@ -10,2 +11,3 @@',
    ' x',
    '+y',
    ' z',
  ].join('\n');
  const [file] = parsePatches([{ filename: 'Foo.kt', status: 'modified', patch }]);
  assert.deepEqual(file.addedLines.map((l) => l.line), [2, 12]);
});

test('handles single-line hunk headers without counts', () => {
  const patch = ['@@ -0,0 +1 @@', '+only line'].join('\n');
  const [file] = parsePatches([{ filename: 'New.kt', status: 'added', patch }]);
  assert.deepEqual(file.addedLines, [{ line: 1, text: 'only line' }]);
});

test('ignores no-newline markers', () => {
  const patch = ['@@ -1 +1 @@', '-old', '+new', '\\ No newline at end of file'].join('\n');
  const [file] = parsePatches([{ filename: 'Foo.kt', status: 'modified', patch }]);
  assert.deepEqual(file.addedLines, [{ line: 1, text: 'new' }]);
});

test('skips removed files and files without a patch', () => {
  const files = [
    { filename: 'Gone.kt', status: 'removed', patch: '@@ -1 +0,0 @@\n-x' },
    { filename: 'big.bin', status: 'modified' },
    { filename: 'Kept.kt', status: 'modified', patch: '@@ -1 +1 @@\n-a\n+b' },
  ];
  const parsed = parsePatches(files);
  assert.deepEqual(parsed.map((f) => f.path), ['Kept.kt']);
});
