'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const os = require('os');
const path = require('path');
const { collectJars } = require('../lib/jars.js');

function makeTree(files) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'jars-test-'));
  for (const [relative, size] of Object.entries(files)) {
    const full = path.join(root, relative);
    fs.mkdirSync(path.dirname(full), { recursive: true });
    fs.writeFileSync(full, Buffer.alloc(size));
  }
  return root;
}

test('collects module jar sizes from a nested tree', () => {
  const root = makeTree({
    'module-jars/kotventure-core-1.2.3.jar': 100,
    'other/kotventure-minimessage-1.2.3.jar': 200,
  });
  const sizes = collectJars(root);
  assert.equal(sizes.get('core'), 100);
  assert.equal(sizes.get('minimessage'), 200);
});

test('prefers the highest version, comparing segments numerically', () => {
  const root = makeTree({
    'kotventure-core-1.2.3.jar': 100,
    'kotventure-core-1.10.0.jar': 300,
    'kotventure-core-0.9.9.jar': 50,
  });
  assert.equal(collectJars(root).get('core'), 300);
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
