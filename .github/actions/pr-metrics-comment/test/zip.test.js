'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { countClassEntries } = require('../lib/zip.js');
const { buildZip } = require('./helpers/zip-fixture.js');

test('counts .class entries in the central directory', () => {
  const zip = buildZip([
    'io/github/lmliam/kotventure/core/Texts.class',
    'io/github/lmliam/kotventure/core/Texts$Companion.class',
    'META-INF/MANIFEST.MF',
    'io/github/lmliam/kotventure/core/',
  ]);
  assert.equal(countClassEntries(zip), 2);
});

test('returns zero for an archive without classes', () => {
  assert.equal(countClassEntries(buildZip(['META-INF/MANIFEST.MF'])), 0);
});

test('returns null for non-zip data', () => {
  assert.equal(countClassEntries(Buffer.alloc(100)), null);
  assert.equal(countClassEntries(Buffer.alloc(4)), null);
});

test('returns null when the central directory is corrupt', () => {
  const zip = buildZip(['a.class']);
  zip.writeUInt32LE(0xdeadbeef, 10);
  assert.equal(countClassEntries(zip), null);
});
