import assert from 'node:assert/strict';
import AdmZip from 'adm-zip';
import test from 'node:test';
import { extractArchiveEntries } from './artifact-archive.js';

function archive(entries: Array<[string, string]>): Buffer {
  const zip = new AdmZip();
  for (const [name, content] of entries) zip.addFile(name, Buffer.from(content));
  return zip.toBuffer();
}

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
