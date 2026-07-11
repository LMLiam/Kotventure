'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const os = require('os');
const path = require('path');
const { collectJars } = require('../lib/jars.js');
const { buildZip } = require('./helpers/zip-fixture.js');

function makeTree(files) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'jars-test-'));
  for (const [relative, content] of Object.entries(files)) {
    const full = path.join(root, relative);
    fs.mkdirSync(path.dirname(full), { recursive: true });
    fs.writeFileSync(full, typeof content === 'number' ? Buffer.alloc(content) : content);
  }
  return root;
}

test('collects module jar sizes from a nested tree', () => {
  const root = makeTree({
    'module-jars/kotventure-core-1.2.3.jar': 100,
    'other/kotventure-minimessage-1.2.3.jar': 200,
  });
  const jars = collectJars(root);
  assert.equal(jars.get('core').size, 100);
  assert.equal(jars.get('minimessage').size, 200);
});

test('prefers the highest version, comparing segments numerically', () => {
  const root = makeTree({
    'kotventure-core-1.2.3.jar': 100,
    'kotventure-core-1.10.0.jar': 300,
    'kotventure-core-0.9.9.jar': 50,
  });
  assert.equal(collectJars(root).get('core').size, 300);
});

test('counts classes for real archives and reports null otherwise', () => {
  const root = makeTree({
    'kotventure-core-1.2.3.jar': buildZip(['a/B.class', 'a/C.class', 'META-INF/MANIFEST.MF']),
    'kotventure-minimessage-1.2.3.jar': 100,
  });
  const jars = collectJars(root);
  assert.equal(jars.get('core').classes, 2);
  assert.equal(jars.get('minimessage').classes, null);
});

test('ignores sources, javadoc, and non-kotventure jars', () => {
  const root = makeTree({
    'kotventure-core-1.2.3-sources.jar': 10,
    'kotventure-core-1.2.3-javadoc.jar': 10,
    'unrelated-1.2.3.jar': 10,
    'kotventure-core-1.2.3.txt': 10,
  });
  assert.equal(collectJars(root).size, 0);
});

test('returns an empty map for a missing directory', () => {
  assert.equal(collectJars(path.join(os.tmpdir(), 'does-not-exist')).size, 0);
});
