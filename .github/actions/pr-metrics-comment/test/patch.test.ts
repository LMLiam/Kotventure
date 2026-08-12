import assert from 'node:assert/strict';
import test from 'node:test';
import { parsePatch } from 'diff';
import { parsePatches, type ParsedPatch, type PullRequestFile } from '../lib/patch.js';
import { asApiData } from '../../../scripts/test-support/mocks.js';

function makePullRequestFile(filename: string, status: string, patch?: string): PullRequestFile {
  return asApiData<PullRequestFile>({ filename, status, patch });
}

function singlePatch(files: PullRequestFile[]): ParsedPatch {
  const parsed = parsePatches(files);
  const file = parsed[0];
  assert.ok(file, 'expected exactly one parsed patch');
  return file;
}

test('tracks added line numbers across context and removals', () => {
  const patch = [
    '@@ -1,3 +1,4 @@',
    ' context',
    '-old line',
    '+new line',
    '+second new',
    ' more context',
  ].join('\n');
  const file = singlePatch([makePullRequestFile('Foo.kt', 'modified', patch)]);
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
  const file = singlePatch([makePullRequestFile('Foo.kt', 'modified', patch)]);
  assert.deepEqual(file.addedLines.map((l) => l.line), [2, 12]);
});

test('handles single-line hunk headers without counts', () => {
  const patch = ['@@ -0,0 +1 @@', '+only line'].join('\n');
  const file = singlePatch([makePullRequestFile('New.kt', 'added', patch)]);
  assert.deepEqual(file.addedLines, [{ line: 1, text: 'only line' }]);
});

test('ignores no-newline markers', () => {
  const patch = ['@@ -1 +1 @@', '-old', '+new', '\\ No newline at end of file'].join('\n');
  const file = singlePatch([makePullRequestFile('Foo.kt', 'modified', patch)]);
  assert.deepEqual(file.addedLines, [{ line: 1, text: 'new' }]);
});

test('skips removed files and files without a patch', () => {
  const files = [
    makePullRequestFile('Gone.kt', 'removed', '@@ -1 +0,0 @@\n-x'),
    makePullRequestFile('big.bin', 'modified'),
    makePullRequestFile('Kept.kt', 'modified', '@@ -1 +1 @@\n-a\n+b'),
  ];
  const parsed = parsePatches(files);
  assert.deepEqual(parsed.map((f) => f.path), ['Kept.kt']);
});

test('jsdiff parses GitHub REST hunk-only patches without file headers', () => {
  const patch = [
    '@@ -1,3 +1,4 @@',
    ' context',
    '-old line',
    '+new line',
    '+second new',
    ' more context',
  ].join('\n');
  const [file] = parsePatch(patch);
  assert.ok(file);
  assert.equal(file.oldFileName, undefined);
  assert.equal(file.newFileName, undefined);
  const hunk = file.hunks[0];
  assert.ok(hunk);
  assert.deepEqual(hunk.lines, [
    ' context',
    '-old line',
    '+new line',
    '+second new',
    ' more context',
  ]);
});

test('rejects a hunk whose declared counts do not match its body', () => {
  const patch = [
    '@@ -1,4 +1,5 @@',
    ' context',
    '-old line',
    '+new line',
    '+second new',
    ' more context',
  ].join('\n');
  assert.throws(() => parsePatches([makePullRequestFile('Foo.kt', 'modified', patch)]));
});
