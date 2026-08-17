import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { mkdtempSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, resolve } from 'node:path';
import { test } from 'node:test';

const script = resolve(dirname(__filename), 'vanilla-fixture-cache-key.sh');

test('prints the version and both fixture digests', () => {
  withPins(
    "final String targetMinecraftVersion = '26.2'\n" +
      "final String serverBundleSha1 = '823e2250d24b3ddac457a60c92a6a941943fcd6a'\n" +
      "final String serverBundleSha256 = 'cdacdfb25898de5e4b4b0e5ddcc2722f77067e46605709c2d886c000ebb63ec5'\n",
    (pins) => {
      const output = run(pins);

      assert.equal(
        output,
        'version=26.2\n' +
          'sha1=823e2250d24b3ddac457a60c92a6a941943fcd6a\n' +
          'sha256=cdacdfb25898de5e4b4b0e5ddcc2722f77067e46605709c2d886c000ebb63ec5\n' +
          'key=vanilla-mc-26.2-823e2250d24b3ddac457a60c92a6a941943fcd6a-cdacdfb25898de5e4b4b0e5ddcc2722f77067e46605709c2d886c000ebb63ec5\n',
      );
    },
  );
});

test('accepts a reviewed fixture update without changing the parser contract', () => {
  withPins(
    "final String targetMinecraftVersion = '27.0'\n" +
      "final String serverBundleSha1 = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'\n" +
      "final String serverBundleSha256 = 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb'\n",
    (pins) => {
      assert.match(run(pins), /version=27\.0/);
      assert.match(run(pins), /key=vanilla-mc-27\.0-aaaa/);
    },
  );
});

test('rejects missing, duplicate, malformed, upper-case, and padded digests', () => {
  const invalidPins = [
    "final String targetMinecraftVersion = '26.2'\n" +
      "final String serverBundleSha1 = '823e2250d24b3ddac457a60c92a6a941943fcd6a'\n",
    "final String targetMinecraftVersion = '26.2'\n" +
      "final String serverBundleSha1 = '823e2250d24b3ddac457a60c92a6a941943fcd6a'\n" +
      "final String serverBundleSha256 = 'cdacdfb25898de5e4b4b0e5ddcc2722f77067e46605709c2d886c000ebb63ec5'\n" +
      "final String serverBundleSha256 = 'cdacdfb25898de5e4b4b0e5ddcc2722f77067e46605709c2d886c000ebb63ec5'\n",
    "final String targetMinecraftVersion = '26.2'\n" +
      "final String serverBundleSha1 = '823e2250d24b3ddac457a60c92a6a941943fcd6a'\n" +
      "final String serverBundleSha256 = 'abcd'\n",
    "final String targetMinecraftVersion = '26.2'\n" +
      "final String serverBundleSha1 = '823e2250d24b3ddac457a60c92a6a941943fcd6a'\n" +
      "final String serverBundleSha256 = 'CDACDFB25898DE5E4B4B0E5DDCC2722F77067E46605709C2D886C000EBB63EC5'\n",
    "final String targetMinecraftVersion = '26.2'\n" +
      "final String serverBundleSha1 = '823e2250d24b3ddac457a60c92a6a941943fcd6a'\n" +
      "final String serverBundleSha256 = ' cdacdfb25898de5e4b4b0e5ddcc2722f77067e46605709c2d886c000ebb63ec5'\n",
  ];

  for (const pinsContent of invalidPins) {
    withPins(pinsContent, (pins) => {
      assert.throws(() => run(pins));
    });
  }
});

function run(pins: string): string {
  return execFileSync('bash', [script, pins], { encoding: 'utf8' });
}

function withPins(content: string, block: (pins: string) => void): void {
  const directory = mkdtempSync(resolve(tmpdir(), 'kotventure-fixture-pins-'));
  const pins = resolve(directory, 'vanilla-conformance.gradle');
  try {
    writeFileSync(pins, content);
    block(pins);
  } finally {
    rmSync(directory, { recursive: true, force: true });
  }
}
