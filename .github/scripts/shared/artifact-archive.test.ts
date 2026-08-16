import assert from 'node:assert/strict';
import AdmZip from 'adm-zip';
import test from 'node:test';
import { extractArchiveEntries } from './artifact-archive.js';

function archive(entries: Array<[string, string]>): Buffer {
  const zip = new AdmZip();
  for (const [name, content] of entries) zip.addFile(name, Buffer.from(content));
  return zip.toBuffer();
}

function crc32(buffer: Buffer): number {
  let crc = 0xffffffff;
  for (const byte of buffer) {
    crc ^= byte;
    for (let bit = 0; bit < 8; bit += 1) {
      crc = (crc >>> 1) ^ ((crc & 1) !== 0 ? 0xedb88320 : 0);
    }
  }
  return (crc ^ 0xffffffff) >>> 0;
}

/**
 * Builds a stored ZIP from raw bytes so fixtures can carry entry names that
 * adm-zip normalises (traversal segments) or deduplicates.
 */
function storedZip(entries: Array<[string, string]>): Buffer {
  const encoded = entries.map(([name, content]) => ({
    name: Buffer.from(name, 'utf8'),
    content: Buffer.from(content),
  }));
  const localParts: Buffer[] = [];
  const centralParts: Buffer[] = [];
  let offset = 0;
  for (const entry of encoded) {
    const crc = crc32(entry.content);
    const localHeader = Buffer.alloc(30);
    localHeader.writeUInt32LE(0x04034b50, 0);
    localHeader.writeUInt16LE(20, 4);
    localHeader.writeUInt16LE(0, 6);
    localHeader.writeUInt16LE(0, 8);
    localHeader.writeUInt16LE(0, 10);
    localHeader.writeUInt16LE(0, 12);
    localHeader.writeUInt32LE(crc, 14);
    localHeader.writeUInt32LE(entry.content.length, 18);
    localHeader.writeUInt32LE(entry.content.length, 22);
    localHeader.writeUInt16LE(entry.name.length, 26);
    localHeader.writeUInt16LE(0, 28);

    const centralHeader = Buffer.alloc(46);
    centralHeader.writeUInt32LE(0x02014b50, 0);
    centralHeader.writeUInt16LE(20, 4);
    centralHeader.writeUInt16LE(20, 6);
    centralHeader.writeUInt16LE(0, 8);
    centralHeader.writeUInt16LE(0, 10);
    centralHeader.writeUInt16LE(0, 12);
    centralHeader.writeUInt16LE(0, 14);
    centralHeader.writeUInt32LE(crc, 16);
    centralHeader.writeUInt32LE(entry.content.length, 20);
    centralHeader.writeUInt32LE(entry.content.length, 24);
    centralHeader.writeUInt16LE(entry.name.length, 28);
    centralHeader.writeUInt16LE(0, 30);
    centralHeader.writeUInt16LE(0, 32);
    centralHeader.writeUInt16LE(0, 34);
    centralHeader.writeUInt16LE(0, 36);
    centralHeader.writeUInt32LE(0, 38);
    centralHeader.writeUInt32LE(offset, 42);

    localParts.push(localHeader, entry.name, entry.content);
    centralParts.push(centralHeader, entry.name);
    offset += 30 + entry.name.length + entry.content.length;
  }
  const centralDirectory = Buffer.concat(centralParts);
  const endRecord = Buffer.alloc(22);
  endRecord.writeUInt32LE(0x06054b50, 0);
  endRecord.writeUInt16LE(0, 4);
  endRecord.writeUInt16LE(0, 6);
  endRecord.writeUInt16LE(encoded.length, 8);
  endRecord.writeUInt16LE(encoded.length, 10);
  endRecord.writeUInt32LE(centralDirectory.length, 12);
  endRecord.writeUInt32LE(offset, 16);
  endRecord.writeUInt16LE(0, 20);
  return Buffer.concat([...localParts, centralDirectory, endRecord]);
}

const bounds = {
  errorPrefix: 'test archive',
  maxArchiveBytes: 1024 * 1024,
  maxEntries: 10,
  maxEntryBytes: 1024,
  maxTotalBytes: 2048,
};

test('extracts a bounded multi-entry archive', async () => {
  const entries = await extractArchiveEntries(archive([
    ['modules/core/build/test-results/test/TEST-core.xml', '<testsuite><testcase name="core" /></testsuite>'],
    ['modules/text/build/test-results/test/TEST-text.xml', '<testsuite><testcase name="text" /></testsuite>'],
  ]), {
    errorPrefix: 'test archive',
    maxArchiveBytes: 1024 * 1024,
    maxEntries: 10,
    maxEntryBytes: 1024,
    maxTotalBytes: 2048,
  });

  assert.deepEqual(entries.map((entry) => entry.fileName), [
    'modules/core/build/test-results/test/TEST-core.xml',
    'modules/text/build/test-results/test/TEST-text.xml',
  ]);
});

test('rejects an archive over the entry bound', async () => {
  await assert.rejects(
    () => extractArchiveEntries(archive([
      ['modules/core/result.xml', 'one'],
      ['modules/text/result.xml', 'two'],
    ]), {
      errorPrefix: 'test archive',
      maxArchiveBytes: 1024 * 1024,
      maxEntries: 1,
      maxEntryBytes: 1024,
      maxTotalBytes: 2048,
    }),
    /too many entries/,
  );
});

test('rejects a parent-traversal entry path', async () => {
  await assert.rejects(
    () => extractArchiveEntries(storedZip([['../escape.xml', 'one']]), bounds),
    /unsafe entry path/,
  );
});

test('rejects an absolute or backslash entry path', async () => {
  await assert.rejects(
    () => extractArchiveEntries(storedZip([['/escape.xml', 'one']]), bounds),
    /unsafe entry path/,
  );
  await assert.rejects(
    () => extractArchiveEntries(storedZip([['a\\b.xml', 'one']]), bounds),
    /unsafe entry path/,
  );
});

test('rejects duplicate entry paths', async () => {
  await assert.rejects(
    () => extractArchiveEntries(storedZip([['a.xml', 'one'], ['a.xml', 'two']]), bounds),
    /duplicate entry paths/,
  );
});

test('rejects an entry over the entry byte bound', async () => {
  await assert.rejects(
    () => extractArchiveEntries(archive([['a.xml', 'x'.repeat(4096)]]), { ...bounds, maxEntryBytes: 16 }),
    /size limit/,
  );
});

test('rejects an archive over the total byte bound', async () => {
  await assert.rejects(
    () => extractArchiveEntries(archive([
      ['a.xml', 'x'.repeat(64)],
      ['b.xml', 'y'.repeat(64)],
    ]), { ...bounds, maxTotalBytes: 80 }),
    /total size limit/,
  );
});
