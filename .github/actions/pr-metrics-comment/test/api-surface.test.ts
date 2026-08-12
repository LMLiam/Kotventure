import assert from 'node:assert/strict';
import test from 'node:test';
import { computeApiSurface } from '../lib/api-surface.js';
import type { ParsedPatch } from '../lib/patch.js';

const path = 'modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/text/Texts.kt';

function patch(filePath: string, addedTexts: string[], removedTexts: string[] = []): ParsedPatch {
  return {
    path: filePath,
    addedLines: addedTexts.map((text, i) => ({ line: i + 1, text })),
    removedText: removedTexts,
  };
}

test('counts public declarations with modifier chains', () => {
  const result = computeApiSurface([patch(path, [
    'public fun text(content: String): Component =',
    '    public inline fun <reified T> tag(): T {',
    'public data class Style(val color: Color)',
    'public companion object Factory {',
  ])]);
  assert.equal(result.added.length, 4);
});

test('ignores non-public, nested-keyword, and non-declaration lines', () => {
  const result = computeApiSurface([patch(path, [
    'internal fun helper() {}',
    'private val cache = mutableMapOf<String, Int>()',
    '    return publicValue',
    '// public fun commented(): Nothing',
    'val local = 1',
  ])]);
  assert.equal(result.added.length, 0);
  assert.equal(result.removed.length, 0);
});

test('cancels moved declarations that appear on both sides', () => {
  const sig = 'public fun moved(): Int = 1';
  const result = computeApiSurface([patch(path, [sig, 'public fun brandNew(): Int = 2'], [sig, 'public fun gone(): Int = 3'])]);
  assert.deepEqual(result.added, ['public fun brandNew(): Int = 2']);
  assert.deepEqual(result.removed, ['public fun gone(): Int = 3']);
});

test('ignores test sources and non-kotlin files', () => {
  const result = computeApiSurface([
    patch('modules/core/src/test/kotlin/FooTest.kt', ['public fun testOnly() {}']),
    patch('docs/CI.md', ['public fun docs() {}']),
  ]);
  assert.equal(result.added.length, 0);
});

test('truncates very long signatures', () => {
  const long = `public fun long(${'a: Int, '.repeat(30)})`;
  const result = computeApiSurface([patch(path, [long])]);
  const added = result.added[0];
  assert.ok(added);
  assert.equal(added.length, 118);
  assert.ok(added.endsWith('…'));
});
