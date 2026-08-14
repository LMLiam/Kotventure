import assert from 'node:assert/strict';
import test from 'node:test';
import {
  buildCodeqlArtifactName,
  parseCodeqlArtifactName,
} from './codeql-contract.js';
import { validateCodeqlSarif } from './codeql-validation.js';

const HEAD_SHA = 'a'.repeat(40);

test('validates a CodeQL SARIF document and relative locations', () => {
  const document = validateCodeqlSarif(JSON.stringify({
    version: '2.1.0',
    runs: [{
      tool: { driver: { name: 'CodeQL' } },
      originalUriBaseIds: {
        '%SRCROOT%': { uri: 'file:///home/runner/work/Kotventure/Kotventure/' },
      },
      results: [{
        ruleId: 'example',
        locations: [{
          physicalLocation: {
            artifactLocation: { uri: 'modules/core/src/main/kotlin/Example.kt', uriBaseId: '%SRCROOT%' },
          },
        }],
      }],
    }],
  }));

  assert.equal(document.version, '2.1.0');
});

test('rejects malformed, foreign-driver, and escaping CodeQL SARIF', () => {
  assert.throws(
    () => validateCodeqlSarif('{'),
    /SARIF is not valid JSON/,
  );
  assert.throws(
    () => validateCodeqlSarif(JSON.stringify({ version: '2.1.0', runs: [{ tool: { driver: { name: 'QDJVM' } }, results: [] }] })),
    /SARIF driver name is invalid/,
  );
  assert.throws(
    () => validateCodeqlSarif(JSON.stringify({
      version: '2.1.0',
      runs: [{
        tool: { driver: { name: 'CodeQL' } },
        results: [{ locations: [{ physicalLocation: { artifactLocation: { uri: '../outside.kt' } } }] }],
      }],
    })),
    /SARIF location escapes the project/,
  );
  assert.throws(
    () => validateCodeqlSarif(JSON.stringify({
      version: '2.1.0',
      runs: [{
        tool: { driver: { name: 'CodeQL' } },
        results: [],
        originalUriBaseIds: { SRCROOT: { uri: '/outside' } },
      }],
    })),
    /SARIF base URI escapes the project/,
  );
  assert.throws(
    () => validateCodeqlSarif(JSON.stringify({
      version: '2.1.0',
      runs: [{
        tool: { driver: { name: 'CodeQL' } },
        results: [],
        originalUriBaseIds: { '%SRCROOT%': { uri: 'https://example.com/' } },
      }],
    })),
    /SARIF base URI escapes the project/,
  );
});

test('builds and parses a CodeQL artefact name', () => {
  const name = buildCodeqlArtifactName({
    category: 'java-kotlin',
    workflowId: 98,
    runId: 456,
    runAttempt: 3,
    headSha: HEAD_SHA,
  });
  assert.deepEqual(parseCodeqlArtifactName(name), {
    category: 'java-kotlin',
    workflowId: 98,
    runId: 456,
    runAttempt: 3,
    headSha: HEAD_SHA,
  });
});
