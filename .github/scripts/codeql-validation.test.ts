import assert from 'node:assert/strict';
import test from 'node:test';
import {
  buildCodeqlArtifactName,
  parseCodeqlArtifactName,
} from './codeql-contract.js';
import {
  selectCodeqlArtifact,
  validateCodeqlSarif,
  validateCodeqlWorkflowSource,
} from './codeql-validation.js';
import type { RepositoryData, WorkflowData, WorkflowRunArtifact, WorkflowRunData } from './shared/action-context.js';
import type { WorkflowRunEventRecord } from './shared/run-context.js';
import { asApiData } from './test-support/mocks.js';

const HEAD_SHA = 'a'.repeat(40);
const RUN_ID = 456;
const RUN_ATTEMPT = 3;
const WORKFLOW_ID = 98;
const REPOSITORY = asApiData<RepositoryData>({ id: 5, full_name: 'LMLiam/Kotventure' });

function codeqlRun(overrides: object = {}): WorkflowRunData {
  return asApiData<WorkflowRunData>({
    id: RUN_ID,
    run_attempt: RUN_ATTEMPT,
    workflow_id: WORKFLOW_ID,
    head_sha: HEAD_SHA,
    head_branch: 'feature/codeql',
    head_repository: { id: 5 },
    repository: { id: 5, full_name: 'LMLiam/Kotventure' },
    event: 'pull_request',
    status: 'completed',
    conclusion: 'success',
    ...overrides,
  });
}

function codeqlArtifact(category: 'actions' | 'java-kotlin', overrides: object = {}): WorkflowRunArtifact {
  return asApiData<WorkflowRunArtifact>({
    id: 1,
    name: buildCodeqlArtifactName({
      category,
      workflowId: WORKFLOW_ID,
      runId: RUN_ID,
      runAttempt: RUN_ATTEMPT,
      headSha: HEAD_SHA,
    }),
    expired: false,
    size_in_bytes: 100,
    workflow_run: {
      id: RUN_ID,
      repository_id: 5,
      head_repository_id: 5,
      head_branch: 'feature/codeql',
      head_sha: HEAD_SHA,
    },
    ...overrides,
  });
}

function codeqlEventRun(overrides: object = {}): WorkflowRunEventRecord {
  return {
    id: RUN_ID,
    run_attempt: RUN_ATTEMPT,
    head_sha: HEAD_SHA,
    workflow_id: WORKFLOW_ID,
    event: 'pull_request',
    status: 'completed',
    ...overrides,
  };
}

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

test('selects the single CodeQL artefact bound to the trusted run', () => {
  const selection = selectCodeqlArtifact({
    artifacts: [codeqlArtifact('java-kotlin')],
    run: codeqlRun(),
    repository: REPOSITORY,
    category: 'java-kotlin',
    headSha: HEAD_SHA,
  });
  assert.equal(selection.artifact.id, 1);
  assert.equal(selection.descriptor.category, 'java-kotlin');
});

test('rejects a CodeQL artefact with a mismatched descriptor', () => {
  assert.throws(
    () => selectCodeqlArtifact({
      artifacts: [codeqlArtifact('actions')],
      run: codeqlRun(),
      repository: REPOSITORY,
      category: 'java-kotlin',
      headSha: HEAD_SHA,
    }),
    /expected exactly one CodeQL java-kotlin artefact, found 0/,
  );
  assert.throws(
    () => selectCodeqlArtifact({
      artifacts: [codeqlArtifact('java-kotlin', { name: 'codeql-sarif-java-kotlin-98-456-3-' + 'b'.repeat(40) })],
      run: codeqlRun(),
      repository: REPOSITORY,
      category: 'java-kotlin',
      headSha: HEAD_SHA,
    }),
    /expected exactly one CodeQL java-kotlin artefact, found 0/,
  );
});

test('rejects duplicate or expired CodeQL artefacts', () => {
  assert.throws(
    () => selectCodeqlArtifact({
      artifacts: [codeqlArtifact('java-kotlin'), codeqlArtifact('java-kotlin', { id: 2 })],
      run: codeqlRun(),
      repository: REPOSITORY,
      category: 'java-kotlin',
      headSha: HEAD_SHA,
    }),
    /found 2/,
  );
  assert.throws(
    () => selectCodeqlArtifact({
      artifacts: [codeqlArtifact('java-kotlin', { expired: true })],
      run: codeqlRun(),
      repository: REPOSITORY,
      category: 'java-kotlin',
      headSha: HEAD_SHA,
    }),
    /is expired/,
  );
});

test('rejects an oversized CodeQL artefact', () => {
  assert.throws(
    () => selectCodeqlArtifact({
      artifacts: [codeqlArtifact('java-kotlin', { size_in_bytes: 16 * 1024 * 1024 + 1 })],
      run: codeqlRun(),
      repository: REPOSITORY,
      category: 'java-kotlin',
      headSha: HEAD_SHA,
    }),
    /size is invalid/,
  );
});

test('validates the CodeQL workflow source and rejects a foreign path', () => {
  const workflow = asApiData<WorkflowData>({ id: WORKFLOW_ID, name: 'CodeQL', path: '.github/workflows/codeql.yml' });
  const source = validateCodeqlWorkflowSource({
    eventRun: codeqlEventRun(),
    run: codeqlRun(),
    workflow,
    repository: REPOSITORY,
  });
  assert.deepEqual(source, {
    runId: RUN_ID,
    runAttempt: RUN_ATTEMPT,
    repository: 'LMLiam/Kotventure',
    headSha: HEAD_SHA,
    event: 'pull_request',
  });

  const foreign = asApiData<WorkflowData>({ id: WORKFLOW_ID, name: 'CodeQL', path: '.github/workflows/other.yml' });
  assert.throws(
    () => validateCodeqlWorkflowSource({
      eventRun: codeqlEventRun(),
      run: codeqlRun(),
      workflow: foreign,
      repository: REPOSITORY,
    }),
    /workflow path does not match/,
  );
});

test('validates a merge-group CodeQL workflow source', () => {
  const workflow = asApiData<WorkflowData>({ id: WORKFLOW_ID, name: 'CodeQL', path: '.github/workflows/codeql.yml' });
  const source = validateCodeqlWorkflowSource({
    eventRun: codeqlEventRun({ event: 'merge_group' }),
    run: codeqlRun({
      event: 'merge_group',
      head_branch: 'gh-readonly-queue/master/pr-1',
      head_repository: { id: 5, full_name: 'LMLiam/Kotventure' },
    }),
    workflow,
    repository: REPOSITORY,
  });
  assert.equal(source.event, 'merge_group');
});
