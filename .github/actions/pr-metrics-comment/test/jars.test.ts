import assert from 'node:assert/strict';
import * as fs from 'node:fs';
import * as os from 'node:os';
import * as path from 'node:path';
import test from 'node:test';
import { collectJars } from '../lib/jars.js';
import { buildZip } from './helpers/zip-fixture.js';

function makeTree(files: Record<string, Buffer | number | string>): string {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'jars-test-'));
  for (const [relative, content] of Object.entries(files)) {
    const full = path.join(root, relative);
    fs.mkdirSync(path.dirname(full), { recursive: true });
    fs.writeFileSync(full, typeof content === 'number' ? Buffer.alloc(content) : content);
  }
  return root;
}

test('collects module jar sizes from a nested tree', async () => {
  const root = makeTree({
    'module-jars/kotventure-core-1.2.3.jar': 100,
    'other/kotventure-minimessage-1.2.3.jar': 200,
  });
  const jars = await collectJars(root);
  assert.equal(jars.get('core')?.size, 100);
  assert.equal(jars.get('minimessage')?.size, 200);
});

test('prefers the highest version, comparing segments numerically', async () => {
  const root = makeTree({
    'kotventure-core-1.2.3.jar': 100,
    'kotventure-core-1.10.0.jar': 300,
    'kotventure-core-0.9.9.jar': 50,
  });
  assert.equal((await collectJars(root)).get('core')?.size, 300);
});

test('counts classes for real archives and reports null otherwise', async () => {
  const root = makeTree({
    'kotventure-core-1.2.3.jar': buildZip(['a/B.class', 'a/C.class', 'META-INF/MANIFEST.MF']),
    'kotventure-minimessage-1.2.3.jar': 100,
  });
  const jars = await collectJars(root);
  assert.equal(jars.get('core')?.classes, 2);
  assert.equal(jars.get('minimessage')?.classes, null);
});

test('ignores sources, javadoc, and non-kotventure jars', async () => {
  const root = makeTree({
    'kotventure-core-1.2.3-sources.jar': 10,
    'kotventure-core-1.2.3-javadoc.jar': 10,
    'unrelated-1.2.3.jar': 10,
    'kotventure-core-1.2.3.txt': 10,
  });
  assert.equal((await collectJars(root)).size, 0);
});

test('returns an empty map for a missing directory', async () => {
  assert.equal((await collectJars(path.join(os.tmpdir(), 'does-not-exist'))).size, 0);
});
