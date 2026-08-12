import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import * as fs from 'node:fs';
import * as os from 'node:os';
import * as path from 'node:path';
import test from 'node:test';
import AdmZip from 'adm-zip';

import {
  MAX_ARTIFACT_BYTES,
  buildArtifactName,
  classifyChangedFiles,
  parseArtifactName,
} from './qodana-contract.js';
import {
  createAttestation,
  type QodanaSarifDocument,
  type QodanaSarifRun,
} from './qodana-attestation.js';
import { extractQodanaSarifArchive } from './qodana-publisher-archive.js';
import {
  selectQodanaArtifact,
  selectQodanaRunArtifact,
  validateQodanaSarif,
} from './qodana-publisher-validation.js';
import { resolvePublication } from './qodana-publisher.js';
import { downloadQodanaArtifact } from './qodana-publisher-storage.js';
import {
  resolvePullRequestEventSource,
  resolvePullRequestSource,
  QodanaSourceRejectedError,
} from './qodana-source.js';
import type { PullRequestSource } from './qodana-source.js';
import type { RepositoryData, WorkflowRunArtifact, WorkflowRunData } from './shared/action-context.js';
import type { ChangedFileRecord } from './shared/path-classification.js';
import type { JsonObject } from './shared/json.js';
import {
  asApiData,
  mockContext,
  mockFetchImpl,
  mockOctokit,
  type MockFetchOptions,
} from './test-support/mocks.js';

const HEAD_SHA = 'a'.repeat(40);
const BASE_SHA = 'b'.repeat(40);
const MERGE_BASE_SHA = 'c'.repeat(40);
const QODANA_RUN_ID = 900;
const QODANA_RUN_ATTEMPT = 3;

interface RepositoryRecord {
  full_name: string;
  id: number;
  default_branch?: string;
}

const REPOSITORY: RepositoryRecord = {
  full_name: 'LMLiam/Kotventure',
  id: 1,
  default_branch: 'master',
};

const HEAD_REPOSITORY: RepositoryRecord = {
  full_name: 'LMLiam/Kotventure',
  id: 1,
};

interface PullRequestRecord {
  number: number;
  state: string;
  changed_files: number;
  base: {
    repo: RepositoryRecord;
    ref: string;
    sha: string;
  };
  head: {
    repo: RepositoryRecord;
    ref: string;
    sha: string;
  };
  user?: {
    login: string;
    type: string;
  };
}

interface MakePullRequestOptions {
  baseRef?: string;
  files?: ChangedFileRecord[];
  state?: string;
  baseSha?: string;
  headRepository?: RepositoryRecord;
  headRef?: string;
  headSha?: string;
}

function makePullRequest({
  baseRef = REPOSITORY.default_branch ?? 'master',
  files = [{ filename: 'src/Main.kt' }],
  state = 'open',
  baseSha = BASE_SHA,
  headRepository = HEAD_REPOSITORY,
  headRef = 'feature/security',
  headSha = HEAD_SHA,
}: MakePullRequestOptions = {}): PullRequestRecord {
  return {
    number: 42,
    state,
    changed_files: files.length,
    base: {
      repo: REPOSITORY,
      ref: baseRef,
      sha: baseSha,
    },
    head: {
      repo: headRepository,
      ref: headRef,
      sha: headSha,
    },
    user: {
      login: 'liam',
      type: 'User',
    },
  };
}

interface MakeGithubOptions {
  pullRequest?: PullRequestRecord;
  files?: ChangedFileRecord[];
  associatedPullRequests?: PullRequestRecord[];
  releaseProvenance?: string;
}

function makeGithub({
  pullRequest = makePullRequest(),
  files,
  associatedPullRequests,
  releaseProvenance = 'success',
}: MakeGithubOptions = {}) {
  const changedFiles = files || [{ filename: 'src/Main.kt' }];
  const listFiles = async () => changedFiles;
  const listAssociatedPullRequests = async () => associatedPullRequests ?? [pullRequest];
  const listJobs = async () => [{
    name: 'Trusted release provenance',
    status: 'completed',
    conclusion: releaseProvenance,
  }];
  const listReleaseRuns = async () => [{
    id: 800,
    event: 'pull_request_target',
    status: 'completed',
    conclusion: releaseProvenance,
    repository: REPOSITORY,
    head_branch: pullRequest.head.ref,
    head_sha: pullRequest.head.sha,
    pull_requests: [{ number: pullRequest.number }],
    created_at: '2026-08-09T00:00:00Z',
  }];
  const paginate = async (method: (...args: never[]) => unknown) => {
    if (method === listFiles) return changedFiles;
    if (method === listAssociatedPullRequests) return listAssociatedPullRequests();
    if (method === listJobs) return listJobs();
    if (method === listReleaseRuns) return listReleaseRuns();
    throw new Error('unexpected pagination method');
  };
  return mockOctokit({
    rest: {
      repos: {
        get: async () => ({ data: REPOSITORY }),
        listPullRequestsAssociatedWithCommit: listAssociatedPullRequests,
        compareCommitsWithBasehead: async () => ({
          data: { merge_base_commit: { sha: MERGE_BASE_SHA } },
        }),
      },
      actions: {
        listJobsForWorkflowRun: listJobs,
        listWorkflowRuns: listReleaseRuns,
      },
      pulls: {
        get: async () => ({ data: pullRequest }),
        listFiles,
      },
    },
    paginate,
  });
}

function makeStoredZip(content: Buffer, fileName = 'qodana.sarif.json'): Buffer {
  const name = Buffer.from(fileName, 'utf8');
  const zip = new AdmZip();
  const entry = zip.addFile('x'.repeat(name.length), content);
  entry.header.method = 0;
  const archive = zip.toBuffer();
  const centralDirectoryOffset = archive.readUInt32LE(archive.length - 6);
  name.copy(archive, 30);
  name.copy(archive, centralDirectoryOffset + 46);
  return archive;
}

function makeDeflateZip(content: Buffer, declaredSize: number): Buffer {
  const zip = new AdmZip();
  zip.addFile('qodana.sarif.json', content);
  const archive = zip.toBuffer();
  const centralDirectoryOffset = archive.readUInt32LE(archive.length - 6);
  archive.writeUInt32LE(declaredSize, 22);
  archive.writeUInt32LE(declaredSize, centralDirectoryOffset + 24);
  return archive;
}

interface PublicationGithubOptions {
  delayedReleaseProvenance?: boolean;
  includeSarif?: boolean;
  qodanaConclusion?: string;
  staleSource?: boolean;
}

function makePublicationGithub({
  delayedReleaseProvenance = false,
  includeSarif = true,
  qodanaConclusion = 'success',
  staleSource = false,
}: PublicationGithubOptions = {}) {
  const qodanaRun = {
    id: QODANA_RUN_ID,
    run_attempt: QODANA_RUN_ATTEMPT,
    workflow_id: 77,
    event: 'pull_request_target',
    status: 'completed',
    conclusion: qodanaConclusion,
    repository: REPOSITORY,
    head_repository: REPOSITORY,
    head_branch: delayedReleaseProvenance
      ? 'release-please--branches--master'
      : 'feature/security',
    head_sha: HEAD_SHA,
    pull_requests: [{ number: 42 }],
  };
  const qodanaEvent = {
    id: qodanaRun.id,
    run_attempt: qodanaRun.run_attempt,
    workflow_id: qodanaRun.workflow_id,
    head_sha: qodanaRun.head_sha,
    event: qodanaRun.event,
    status: qodanaRun.status,
    conclusion: qodanaRun.conclusion,
  };
  const files = delayedReleaseProvenance
    ? [{ filename: 'CHANGELOG.md' }]
    : [{ filename: 'src/Main.kt' }];
  const pullRequest = makePullRequest({
    files,
    headRef: delayedReleaseProvenance
      ? 'release-please--branches--master'
      : 'feature/security',
  });
  if (delayedReleaseProvenance) {
    pullRequest.user = {
      login: 'release-please-kotventure[bot]',
      type: 'Bot',
    };
  }
  const listFiles = async () => files;
  const listAssociatedPullRequests = async () => (staleSource ? [] : [pullRequest]);
  const listJobs = async () => [{
    name: 'Trusted release provenance',
    status: 'completed',
    conclusion: 'success',
  }];
  const listReleaseRuns = async () => [{
    id: 800,
    event: 'pull_request_target',
    status: 'completed',
    conclusion: 'success',
    repository: REPOSITORY,
    head_branch: pullRequest.head.ref,
    head_sha: pullRequest.head.sha,
    pull_requests: [{ number: pullRequest.number }],
    created_at: '2026-08-09T00:00:00Z',
  }];
  const artifacts: WorkflowRunArtifact[] = [];
  if (qodanaConclusion === 'success' && includeSarif) {
    artifacts.push(asApiData<WorkflowRunArtifact>({
      id: 700,
      name: buildArtifactName({
        sourceKind: 'code',
        qodanaRunId: qodanaRun.id,
        qodanaRunAttempt: qodanaRun.run_attempt,
        headSha: HEAD_SHA,
        baseSha: BASE_SHA,
      }),
      expired: false,
      size_in_bytes: 100,
      workflow_run: {
        id: qodanaRun.id,
        repository_id: REPOSITORY.id,
        head_repository_id: REPOSITORY.id,
        head_branch: qodanaRun.head_branch,
        head_sha: qodanaRun.head_sha,
      },
    }));
  }
  const listArtifacts = async () => artifacts;
  const paginate = async (method: (...args: never[]) => unknown) => {
    if (method === listArtifacts) return listArtifacts();
    if (method === listFiles) return files;
    if (method === listJobs) return listJobs();
    if (method === listReleaseRuns) return listReleaseRuns();
    if (method === listAssociatedPullRequests) return listAssociatedPullRequests();
    throw new Error('unexpected publication pagination method');
  };
  return {
    artifacts,
    context: mockContext({
      repo: { owner: 'LMLiam', repo: 'Kotventure' },
      payload: { workflow_run: qodanaEvent },
    }),
    github: mockOctokit({
      rest: {
        repos: {
          get: async () => ({ data: REPOSITORY }),
          listPullRequestsAssociatedWithCommit: listAssociatedPullRequests,
          compareCommitsWithBasehead: async () => ({
            data: { merge_base_commit: { sha: MERGE_BASE_SHA } },
          }),
        },
        actions: {
          getWorkflowRun: async () => ({ data: qodanaRun }),
          getWorkflow: async ({ workflow_id: workflowId }: { workflow_id: number | string }) => ({
            data: workflowId === qodanaRun.workflow_id
              ? { id: 77, name: 'Qodana', path: '.github/workflows/qodana.yml' }
              : workflowId === 'release-provenance.yml'
                ? { path: '.github/workflows/release-provenance.yml' }
                : { id: 55, name: 'CI', path: '.github/workflows/ci.yml' },
          }),
          listJobsForWorkflowRun: listJobs,
          listWorkflowRuns: listReleaseRuns,
          listWorkflowRunArtifacts: listArtifacts,
        },
        pulls: {
          get: async () => ({ data: pullRequest }),
          listFiles,
        },
      },
      paginate,
    }),
    qodanaRun,
  };
}

test('classifies only approved documentation paths and release files', () => {
  assert.equal(classifyChangedFiles([{ filename: 'docs/CI.md' }]), 'documentation');
  assert.equal(classifyChangedFiles([{ filename: 'README.md', previous_filename: 'docs/README.md' }]), 'documentation');
  assert.equal(classifyChangedFiles([{ filename: 'CHANGELOG.md' }]), 'release-candidate');
  assert.equal(classifyChangedFiles([{ filename: 'qodana.yaml' }]), 'code');
  assert.equal(classifyChangedFiles([{ filename: '.github/scripts/normalize-qodana-sarif.sh' }]), 'code');
  assert.equal(classifyChangedFiles([{ filename: 'docs/../.github/workflows/ci.yml' }]), 'code');
  assert.equal(classifyChangedFiles([]), 'code');
});

test('normalises Qodana metadata for a stable code-scanning category', (t) => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'qodana-normalize-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const sarifPath = path.join(directory, 'qodana.sarif.json');
  fs.writeFileSync(sarifPath, JSON.stringify({
    version: '2.1.0',
    runs: [{
      automationDetails: {
        id: 'Kotventure/qodana/2026-08-09',
        guid: 'aaeed1dc-350f-44a8-870d-62f8cb2b1dd5',
      },
      results: [{
        locations: [{
          physicalLocation: {
            region: { startLine: 0, startColumn: 0 },
          },
        }],
      }],
    }],
  }));

  execFileSync('bash', [path.join(__dirname, 'normalize-qodana-sarif.sh'), sarifPath]);

  const document = JSON.parse(fs.readFileSync(sarifPath, 'utf8'));
  assert.equal(document.runs[0].automationDetails, undefined);
  assert.deepEqual(
    document.runs[0].results[0].locations[0].physicalLocation.region,
    { startLine: 1, startColumn: 1 },
  );
});

test('binds Qodana artifacts to the Qodana run and the source SHAs', () => {
  const name = buildArtifactName({
    sourceKind: 'code',
    qodanaRunId: QODANA_RUN_ID,
    qodanaRunAttempt: QODANA_RUN_ATTEMPT,
    headSha: HEAD_SHA,
    baseSha: BASE_SHA,
  });
  assert.deepEqual(parseArtifactName(name), {
    sourceKind: 'code',
    qodanaRunId: QODANA_RUN_ID,
    qodanaRunAttempt: QODANA_RUN_ATTEMPT,
    headSha: HEAD_SHA,
    baseSha: BASE_SHA,
  });
  assert.equal(parseArtifactName(`${name}-old`), null);
  assert.equal(parseArtifactName(
    `qodana-sarif-code-100-2-${QODANA_RUN_ID}-${QODANA_RUN_ATTEMPT}-${HEAD_SHA}-${BASE_SHA}`,
  ), null);
});

test('creates valid zero-result attestations from trusted code', () => {
  for (const sourceKind of ['documentation', 'release'] as const) {
    const document = createAttestation({ sourceKind, headSha: HEAD_SHA });
    assert.equal(validateQodanaSarif(Buffer.from(JSON.stringify(document))).version, '2.1.0');
    const run = document.runs[0];
    assert.ok(run);
    assert.equal(run.results.length, 0);
  }
});

test('extracts exactly one bounded SARIF file from an artifact ZIP', async () => {
  const document = createAttestation({ sourceKind: 'documentation', headSha: HEAD_SHA });
  const content = Buffer.from(JSON.stringify(document));
  const archive = makeStoredZip(content);
  assert.deepEqual(await extractQodanaSarifArchive(archive), content);
  await assert.rejects(
    extractQodanaSarifArchive(makeStoredZip(content, '../qodana.sarif.json')),
    /must contain only qodana\.sarif\.json/,
  );

  const duplicateEntryMetadata = Buffer.from(archive);
  duplicateEntryMetadata.writeUInt16LE(2, duplicateEntryMetadata.length - 14);
  duplicateEntryMetadata.writeUInt16LE(2, duplicateEntryMetadata.length - 12);
  await assert.rejects(
    extractQodanaSarifArchive(duplicateEntryMetadata),
    /must contain exactly one file/,
  );
});

test('rejects archives whose entry metadata is internally inconsistent', async () => {
  const document = createAttestation({ sourceKind: 'documentation', headSha: HEAD_SHA });
  const content = Buffer.from(JSON.stringify(document));
  const archive = makeStoredZip(content);
  const centralDirectoryOffset = archive.readUInt32LE(archive.length - 6);

  const mismatchedSizes = Buffer.from(archive);
  mismatchedSizes.writeUInt32LE(content.length + 1, 18);
  await assert.rejects(
    extractQodanaSarifArchive(mismatchedSizes),
    /has inconsistent file sizes/,
  );

  const nonZeroDiskStart = Buffer.from(archive);
  nonZeroDiskStart.writeUInt16LE(1, centralDirectoryOffset + 34);
  await assert.rejects(
    extractQodanaSarifArchive(nonZeroDiskStart),
    /uses unsupported compression or metadata/,
  );

  const misplacedLocalHeader = Buffer.from(archive);
  misplacedLocalHeader.writeUInt32LE(centralDirectoryOffset, centralDirectoryOffset + 42);
  await assert.rejects(
    extractQodanaSarifArchive(misplacedLocalHeader),
    /has an invalid local file header offset/,
  );
});

test('rejects a deflate entry that expands beyond its declared size', async () => {
  const content = Buffer.alloc(64, 0x61);
  const archive = makeDeflateZip(content, 4);
  await assert.rejects(
    extractQodanaSarifArchive(archive),
    /cannot be parsed/,
  );
});

function firstResult(run: QodanaSarifRun): JsonObject {
  const result = run.results[0];
  if (result === undefined) throw new Error('SARIF test run must have a result');
  return result;
}

test('rejects malformed SARIF and every standard project traversal location', () => {
  assert.throws(() => validateQodanaSarif(Buffer.from('{')), /not valid JSON/);
  const document = createAttestation({ sourceKind: 'documentation', headSha: HEAD_SHA });
  const run = document.runs[0];
  assert.ok(run);
  document.runs.push(structuredClone(run));
  assert.throws(() => validateQodanaSarif(Buffer.from(JSON.stringify(document))), /exactly one run/);

  const traversalCases: Array<(run: QodanaSarifRun) => void> = [
    (run) => { firstResult(run).locations = [{ physicalLocation: { artifactLocation: { uri: '../secret.txt' } } }]; },
    (run) => { firstResult(run).relatedLocations = [{ physicalLocation: { artifactLocation: { uri: '/etc/passwd' } } }]; },
    (run) => { firstResult(run).fixes = [{ artifactChanges: [{ artifactLocation: { uri: 'file:///etc/passwd' } }] }]; },
    (run) => {
      firstResult(run).codeFlows = [{
        threadFlows: [{
          locations: [{ location: { physicalLocation: { artifactLocation: { uri: '..\\secret.txt' } } } }],
        }],
      }];
    },
    (run) => {
      firstResult(run).stacks = [{
        frames: [{ location: { physicalLocation: { artifactLocation: { uri: 'C:\\secret.txt' } } } }],
      }];
    },
    (run) => { run.artifacts = [{ location: { uri: 'https://attacker.example/source.kt' } }]; },
    (run) => { run.originalUriBaseIds = { SRCROOT: { uri: '../../outside/' } }; },
    (run) => { (run as JsonObject).invocations = [{ workingDirectory: { uri: '/untrusted/workspace' } }]; },
    (run) => { firstResult(run).analysisTarget = { uri: 'file:///untrusted/source.kt' }; },
  ];
  for (const mutate of traversalCases) {
    const testRun = asApiData<QodanaSarifRun>({
      tool: { driver: { name: 'QDJVM' } },
      results: [{}],
    });
    const resultDocument = {
      version: '2.1.0',
      runs: [testRun],
    };
    mutate(testRun);
    assert.throws(
      () => validateQodanaSarif(Buffer.from(JSON.stringify(resultDocument))),
      /artifact location|escapes the project/,
    );
  }
});

test('selects only the artifact bound to the trusted Qodana workflow run', () => {
  const source = asApiData<PullRequestSource>({
    sourceKind: 'code',
    headSha: HEAD_SHA,
    baseSha: BASE_SHA,
  });
  const artifact = asApiData<WorkflowRunArtifact>({
    id: 700,
    name: buildArtifactName({
      ...source,
      qodanaRunId: QODANA_RUN_ID,
      qodanaRunAttempt: QODANA_RUN_ATTEMPT,
    }),
    expired: false,
    size_in_bytes: 100,
    workflow_run: {
      id: QODANA_RUN_ID,
      repository_id: REPOSITORY.id,
      head_repository_id: REPOSITORY.id,
      head_branch: 'feature/security',
      head_sha: HEAD_SHA,
    },
  });
  const qodanaRun = asApiData<WorkflowRunData>({
    id: QODANA_RUN_ID,
    run_attempt: QODANA_RUN_ATTEMPT,
    head_repository: REPOSITORY,
    head_branch: 'feature/security',
    head_sha: HEAD_SHA,
  });
  const repository = asApiData<RepositoryData>(REPOSITORY);
  assert.equal(selectQodanaArtifact({
    artifacts: [artifact],
    qodanaRun,
    source,
    repository,
  }).id, 700);
  assert.throws(
    () => selectQodanaArtifact({
      artifacts: [artifact, structuredClone(artifact)],
      qodanaRun,
      source,
      repository,
    }),
    /exactly one/,
  );

  const priorAttempt = structuredClone(artifact);
  priorAttempt.name = buildArtifactName({
    ...source,
    qodanaRunId: QODANA_RUN_ID,
    qodanaRunAttempt: QODANA_RUN_ATTEMPT - 1,
  });
  assert.equal(selectQodanaArtifact({
    artifacts: [priorAttempt, artifact],
    qodanaRun,
    source,
    repository,
  }).id, 700);
});

interface ArtifactWorkflowRunRecord {
  id: number;
  repository_id: number;
  head_repository_id?: number;
  head_branch: string;
  head_sha: string;
}

interface ArtifactRecord {
  id: number;
  name: string;
  expired: boolean;
  size_in_bytes: number;
  workflow_run?: ArtifactWorkflowRunRecord | null;
}

function makeArtifact(overrides: Partial<ArtifactRecord> = {}): WorkflowRunArtifact {
  return asApiData<WorkflowRunArtifact>({
    id: 700,
    name: buildArtifactName({
      sourceKind: 'code',
      qodanaRunId: QODANA_RUN_ID,
      qodanaRunAttempt: QODANA_RUN_ATTEMPT,
      headSha: HEAD_SHA,
      baseSha: BASE_SHA,
    }),
    expired: false,
    size_in_bytes: 100,
    workflow_run: {
      id: QODANA_RUN_ID,
      repository_id: REPOSITORY.id,
      head_repository_id: REPOSITORY.id,
      head_branch: 'feature/security',
      head_sha: HEAD_SHA,
    },
    ...overrides,
  });
}

test('rejects missing, duplicate, expired, and oversized SARIF artifacts', () => {
  const qodanaRun = asApiData<WorkflowRunData>({
    id: QODANA_RUN_ID,
    run_attempt: QODANA_RUN_ATTEMPT,
    head_repository: REPOSITORY,
    head_branch: 'feature/security',
    head_sha: HEAD_SHA,
  });
  const repository = asApiData<RepositoryData>(REPOSITORY);
  const cases: Array<[WorkflowRunArtifact[], RegExp]> = [
    [[], /exactly one Qodana SARIF artifact, found 0/],
    [[makeArtifact(), makeArtifact()], /exactly one Qodana SARIF artifact, found 2/],
    [[makeArtifact({ expired: true })], /Qodana SARIF artifact is expired/],
    [[makeArtifact({ size_in_bytes: MAX_ARTIFACT_BYTES + 1 })], /Qodana SARIF artifact size is invalid/],
    [[makeArtifact({
      workflow_run: {
        id: QODANA_RUN_ID,
        repository_id: REPOSITORY.id,
        head_branch: 'feature/security',
        head_sha: HEAD_SHA,
      },
    })], /Qodana SARIF artifact head repository id is invalid/],
  ];
  for (const [artifacts, expected] of cases) {
    assert.throws(
      () => selectQodanaRunArtifact({
        artifacts,
        qodanaRun,
        repository,
      }),
      expected,
    );
  }
});

test('publishes only a successful Qodana run for the current PR head', async () => {
  const inputs = makePublicationGithub();
  const publication = await resolvePublication({
    github: inputs.github,
    context: inputs.context,
  });
  assert.deepEqual(publication, {
    artifactId: 700,
    headSha: HEAD_SHA,
    pullNumber: 42,
    publish: true,
    rejection: null,
    sourceKind: 'code',
  });
});

test('publishes a source whose release provenance arrived after registration', async () => {
  const inputs = makePublicationGithub({ delayedReleaseProvenance: true });
  const publication = await resolvePublication({
    github: inputs.github,
    context: inputs.context,
  });
  assert.equal(publication.publish, true);
  assert.equal(publication.sourceKind, 'release');
  assert.equal(publication.artifactId, 700);
});

test('does not publish a failed Qodana analysis', async () => {
  const inputs = makePublicationGithub({ qodanaConclusion: 'failure' });
  const publication = await resolvePublication({
    github: inputs.github,
    context: inputs.context,
  });
  assert.equal(publication.publish, false);
  assert.equal(publication.artifactId, null);
  assert.equal(publication.rejection, null);
});

test('does not publish cancelled or other non-success Qodana analyses', async () => {
  for (const conclusion of ['action_required', 'cancelled', 'neutral', 'skipped', 'stale', 'timed_out']) {
    const inputs = makePublicationGithub({ qodanaConclusion: conclusion });
    const publication = await resolvePublication({
      github: inputs.github,
      context: inputs.context,
    });
    assert.equal(publication.publish, false);
    assert.equal(publication.rejection, null);
  }
});

test('skips publication when the pull-request source becomes stale', async () => {
  const inputs = makePublicationGithub({ staleSource: true });
  const publication = await resolvePublication({
    github: inputs.github,
    context: inputs.context,
  });
  assert.equal(publication.publish, false);
  assert.equal(publication.rejection, null);
});

test('reports a missing successful Qodana artifact as publication failure', async () => {
  const inputs = makePublicationGithub({ includeSarif: false });
  const publication = await resolvePublication({
    github: inputs.github,
    context: inputs.context,
  });
  assert.equal(publication.publish, false);
  assert.ok(publication.rejection);
  assert.match(publication.rejection.message, /exactly one Qodana SARIF artifact/);
});

test('resolves a pull-request source by head SHA and computes its merge base', async () => {
  const source = await resolvePullRequestSource({
    github: makeGithub(),
    owner: 'LMLiam',
    repo: 'Kotventure',
    headSha: HEAD_SHA,
  });
  assert.equal(source.sourceKind, 'code');
  assert.equal(source.mergeBaseSha, MERGE_BASE_SHA);
  assert.equal(source.pullRequest, 42);
  assert.equal('runId' in source, false);
});

test('resolves same-repository and fork pull-request heads', async () => {
  const forkRepository = {
    full_name: 'contributor/Kotventure',
    id: 2,
  };
  const pullRequest = makePullRequest({ headRepository: forkRepository });
  const source = await resolvePullRequestSource({
    github: makeGithub({ pullRequest }),
    owner: 'LMLiam',
    repo: 'Kotventure',
    headSha: HEAD_SHA,
  });
  assert.equal(source.headRepository, forkRepository.full_name);
  assert.equal(source.headSha, HEAD_SHA);
});

test('accepts stacked pull requests whose base is another feature branch', async () => {
  const stackedBase = 'chore/ci-shared-validation';
  const pullRequest = makePullRequest({ baseRef: stackedBase });
  const source = await resolvePullRequestSource({
    github: makeGithub({ pullRequest }),
    owner: 'LMLiam',
    repo: 'Kotventure',
    headSha: HEAD_SHA,
  });
  assert.equal(source.sourceKind, 'code');
  assert.equal(source.baseRef, stackedBase);
  assert.equal(source.baseSha, BASE_SHA);
  assert.equal(source.pullRequest, 42);
});

test('registers a stacked pull request from the pull_request_target event', async () => {
  const stackedBase = 'chore/ci-shared-validation';
  const pullRequest = makePullRequest({ baseRef: stackedBase });
  const source = await resolvePullRequestEventSource({
    github: makeGithub({ pullRequest }),
    context: mockContext({
      repo: { owner: 'LMLiam', repo: 'Kotventure' },
      payload: { pull_request: pullRequest },
    }),
    qodanaRunId: QODANA_RUN_ID,
    qodanaRunAttempt: QODANA_RUN_ATTEMPT,
  });
  assert.equal(source.sourceKind, 'code');
  assert.equal(source.baseRef, stackedBase);
  assert.equal(source.pullRequest, 42);
  assert.equal(source.headSha, HEAD_SHA);
});

test('filters commit-associated pull requests before selecting the source', async () => {
  const pullRequest = makePullRequest();
  const unrelatedPullRequest = makePullRequest({
    headRef: 'unrelated',
    headSha: 'e'.repeat(40),
  });
  unrelatedPullRequest.number = 99;
  const source = await resolvePullRequestSource({
    github: makeGithub({
      pullRequest,
      associatedPullRequests: [unrelatedPullRequest, pullRequest],
    }),
    owner: 'LMLiam',
    repo: 'Kotventure',
    headSha: HEAD_SHA,
  });
  assert.equal(source.pullRequest, 42);
});

test('resolves the pull_request_target event source for the register job', async () => {
  const pullRequest = makePullRequest();
  const source = await resolvePullRequestEventSource({
    github: makeGithub({ pullRequest }),
    context: mockContext({
      repo: { owner: 'LMLiam', repo: 'Kotventure' },
      payload: { pull_request: pullRequest },
    }),
    qodanaRunId: QODANA_RUN_ID,
    qodanaRunAttempt: QODANA_RUN_ATTEMPT,
  });
  assert.equal(source.sourceKind, 'code');
  assert.equal(source.pullRequest, 42);
  assert.equal(source.headSha, HEAD_SHA);
  assert.equal(source.artifactName, buildArtifactName({
    sourceKind: 'code',
    qodanaRunId: QODANA_RUN_ID,
    qodanaRunAttempt: QODANA_RUN_ATTEMPT,
    headSha: HEAD_SHA,
    baseSha: BASE_SHA,
  }));
});

test('uses the documentation attestation path without computing a merge base', async () => {
  const files = [{ filename: 'docs/CI.md' }];
  const pullRequest = makePullRequest({ files });
  const source = await resolvePullRequestSource({
    github: makeGithub({ pullRequest, files }),
    owner: 'LMLiam',
    repo: 'Kotventure',
    headSha: HEAD_SHA,
  });
  assert.equal(source.sourceKind, 'documentation');
  assert.equal(source.mergeBaseSha, undefined);
});

test('downloads, bounds, and validates the single SARIF artifact', async () => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'qodana-download-'));
  const document = createAttestation({ sourceKind: 'documentation', headSha: HEAD_SHA });
  const content = Buffer.from(JSON.stringify(document));
  const archive = makeStoredZip(content);
  try {
    const filePath = await downloadQodanaArtifact({
      owner: 'LMLiam',
      repo: 'Kotventure',
      artifactId: 700,
      outputDirectory: directory,
      apiUrl: 'https://api.github.test',
      token: 'test-token',
      fetchImpl: mockFetchImpl(async (location, options: MockFetchOptions) => {
        if (location === 'https://api.github.test/repos/LMLiam/Kotventure/actions/artifacts/700/zip') {
          assert.equal(options.redirect, 'manual');
          assert.equal(options.headers?.authorization, 'Bearer test-token');
          return {
            status: 302,
            headers: { location: 'https://artifact.example/qodana.zip' },
          };
        }
        assert.equal(location, 'https://artifact.example/qodana.zip');
        assert.equal(options.redirect, 'follow');
        return {
          ok: true,
          headers: { 'content-length': String(archive.length) },
          arrayBuffer: async () => archive,
        };
      }),
    });
    assert.equal(filePath, path.join(directory, 'qodana.sarif.json'));
    assert.deepEqual(JSON.parse(fs.readFileSync(filePath, 'utf8')), document);
  } finally {
    fs.rmSync(directory, { recursive: true, force: true });
  }
});

test('marks a pull request that advanced after registration as stale', async () => {
  const advanced = makePullRequest({ headSha: 'd'.repeat(40) });
  await assert.rejects(
    resolvePullRequestSource({
      github: makeGithub({ pullRequest: advanced }),
      owner: 'LMLiam',
      repo: 'Kotventure',
      headSha: HEAD_SHA,
    }),
    (error) => error instanceof QodanaSourceRejectedError && error.stale,
  );
});

test('marks a missing head-SHA association or closed pull request as stale', async () => {
  await assert.rejects(
    resolvePullRequestSource({
      github: makeGithub({ associatedPullRequests: [] }),
      owner: 'LMLiam',
      repo: 'Kotventure',
      headSha: HEAD_SHA,
    }),
    (error) => error instanceof QodanaSourceRejectedError && error.stale,
  );

  const closedPullRequest = makePullRequest({ state: 'closed' });
  await assert.rejects(
    resolvePullRequestSource({
      github: makeGithub({ pullRequest: closedPullRequest }),
      owner: 'LMLiam',
      repo: 'Kotventure',
      headSha: HEAD_SHA,
    }),
    (error) => error instanceof QodanaSourceRejectedError && error.stale,
  );
});

test('marks a changed base or advanced event head as stale', async () => {
  const changedBase = makePullRequest({ baseSha: 'd'.repeat(40) });
  await assert.rejects(
    resolvePullRequestSource({
      github: makeGithub({ pullRequest: changedBase }),
      owner: 'LMLiam',
      repo: 'Kotventure',
      headSha: HEAD_SHA,
      expectedBaseSha: BASE_SHA,
    }),
    (error) => error instanceof QodanaSourceRejectedError && error.stale,
  );

  const current = makePullRequest({ headSha: 'd'.repeat(40) });
  await assert.rejects(
    resolvePullRequestEventSource({
      github: makeGithub({ pullRequest: current }),
      context: mockContext({
        repo: { owner: 'LMLiam', repo: 'Kotventure' },
        payload: { pull_request: makePullRequest() },
      }),
      qodanaRunId: QODANA_RUN_ID,
      qodanaRunAttempt: QODANA_RUN_ATTEMPT,
    }),
    (error) => error instanceof QodanaSourceRejectedError && error.stale,
  );
});
