import assert from 'node:assert/strict';
import test from 'node:test';
import { countClassEntries } from '../lib/zip.js';
import { buildZip } from './helpers/zip-fixture.js';

test('counts .class entries in the central directory', async () => {
  const zip = buildZip([
    'io/github/lmliam/kotventure/core/Texts.class',
    'io/github/lmliam/kotventure/core/Texts$Companion.class',
    'META-INF/MANIFEST.MF',
    'io/github/lmliam/kotventure/core/',
  ]);
  assert.equal(await countClassEntries(zip), 2);
});

test('returns zero for an archive without classes', async () => {
  assert.equal(await countClassEntries(buildZip(['META-INF/MANIFEST.MF'])), 0);
});

test('returns null for non-zip data', async () => {
  assert.equal(await countClassEntries(Buffer.alloc(100)), null);
  assert.equal(await countClassEntries(Buffer.alloc(4)), null);
});

test('returns null when the central directory is corrupt', async () => {
  const zip = buildZip(['a.class']);
  const centralDirectoryOffset = zip.readUInt32LE(zip.length - 6);
  zip.writeUInt32LE(0xdeadbeef, centralDirectoryOffset);
  assert.equal(await countClassEntries(zip), null);
});

test('returns null when trailing data follows the end record', async () => {
  const zip = buildZip(['a.class']);
  assert.equal(await countClassEntries(Buffer.concat([zip, Buffer.from('trailing')])), null);
});

test('returns null when a central directory record overflows the buffer', async () => {
  const zip = buildZip(['a.class']);
  const centralDirectoryOffset = zip.readUInt32LE(zip.length - 6);
  zip.writeUInt16LE(0xffff, centralDirectoryOffset + 28);
  assert.equal(await countClassEntries(zip), null);
});
