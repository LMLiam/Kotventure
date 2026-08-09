'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const test = require('node:test');

const {
  buildArtifactName,
  classifyChangedFiles,
  parseArtifactName,
} = require('./qodana-contract.js');
const { createAttestation } = require('./qodana-attestation.js');
const { extractQodanaSarifArchive } = require('./qodana-publisher-archive.js');
const {
  selectQodanaArtifact,
  validateQodanaSarif,
} = require('./qodana-publisher-validation.js');
const { resolvePublication } = require('./qodana-publisher.js');
const { downloadQodanaArtifact } = require('./qodana-publisher-storage.js');
const {
  resolveCiRun,
  resolveTrustedCiRun,
  QodanaSourceRejectedError,
} = require('./qodana-source.js');

const HEAD_SHA = 'a'.repeat(40);
const BASE_SHA = 'b'.repeat(40);
const MERGE_BASE_SHA = 'c'.repeat(40);
const QODANA_RUN_ID = 900;
const QODANA_RUN_ATTEMPT = 3;
const REPOSITORY = {
  full_name: 'LMLiam/Kotventure',
  id: 1,
  default_branch: 'master',
};
const HEAD_REPOSITORY = {
  full_name: 'LMLiam/Kotventure',
  id: 1,
};

function makePullRequest({
  files = [{ filename: 'src/Main.kt' }],
  state = 'open',
  baseSha = BASE_SHA,
  headRepository = HEAD_REPOSITORY,
  headRef = 'feature/security',
  headSha = HEAD_SHA,
} = {}) {
  return {
    number: 42,
    state,
    changed_files: files.length,
    base: {
      repo: REPOSITORY,
      ref: 'master',
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

function makeCiRun({
  event = 'pull_request',
  headRepository = HEAD_REPOSITORY,
  headBranch = 'feature/security',
  headSha = HEAD_SHA,
  pullRequests = [{ number: 42 }],
} = {}) {
  return {
    id: 100,
    run_attempt: 2,
    workflow_id: 55,
    event,
    status: 'completed',
    conclusion: 'success',
    repository: REPOSITORY,
    head_repository: headRepository,
    head_branch: headBranch,
    head_sha: headSha,
    pull_requests: pullRequests,
  };
}

function makeGithub({
  run = makeCiRun(),
  pullRequest = makePullRequest(),
  files,
  associatedPullRequests = [],
} = {}) {
  const changedFiles = files || [{ filename: 'src/Main.kt' }];
  const listFiles = async () => changedFiles;
  const listAssociatedPullRequests = async () => associatedPullRequests;
  return {
    rest: {
      repos: {
        get: async () => ({ data: REPOSITORY }),
        listPullRequestsAssociatedWithCommit: listAssociatedPullRequests,
        compareCommitsWithBasehead: async () => ({ data: { merge_base_commit: { sha: MERGE_BASE_SHA } } }),
      },
      actions: {
        getWorkflowRun: async () => ({ data: run }),
        getWorkflow: async () => ({
          data: {
            id: 55,
            name: 'CI',
            path: '.github/workflows/ci.yml',
          },
        }),
      },
      pulls: {
        get: async () => ({ data: pullRequest }),
        listFiles,
      },
    },
    paginate: async (method) => {
      if (method === listFiles) {
        return changedFiles;
      }
      if (method === listAssociatedPullRequests) {
        return associatedPullRequests;
      }
      throw new Error('unexpected pagination method');
    },
  };
}

function makeStoredZip(content, fileName = 'qodana.sarif.json') {
  const name = Buffer.from(fileName, 'utf8');
  const localHeader = Buffer.alloc(30 + name.length);
  localHeader.writeUInt32LE(0x04034b50, 0);
  localHeader.writeUInt16LE(20, 4);
  localHeader.writeUInt16LE(0, 6);
  localHeader.writeUInt16LE(0, 8);
  localHeader.writeUInt32LE(content.length, 18);
  localHeader.writeUInt32LE(content.length, 22);
  localHeader.writeUInt16LE(name.length, 26);
  name.copy(localHeader, 30);

  const centralDirectory = Buffer.alloc(46 + name.length);
  centralDirectory.writeUInt32LE(0x02014b50, 0);
  centralDirectory.writeUInt16LE(20, 4);
  centralDirectory.writeUInt16LE(20, 6);
  centralDirectory.writeUInt16LE(0, 8);
  centralDirectory.writeUInt16LE(0, 10);
  centralDirectory.writeUInt32LE(content.length, 20);
  centralDirectory.writeUInt32LE(content.length, 24);
  centralDirectory.writeUInt16LE(name.length, 28);
  name.copy(centralDirectory, 46);

  const end = Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50, 0);
  end.writeUInt16LE(1, 8);
  end.writeUInt16LE(1, 10);
  end.writeUInt32LE(centralDirectory.length, 12);
  end.writeUInt32LE(localHeader.length + content.length, 16);
  return Buffer.concat([localHeader, content, centralDirectory, end]);
}

function makePublicationGithub() {
  const qodanaRun = {
    id: QODANA_RUN_ID,
    run_attempt: QODANA_RUN_ATTEMPT,
    workflow_id: 77,
    event: 'workflow_run',
    status: 'completed',
    conclusion: 'success',
    repository: REPOSITORY,
    head_repository: REPOSITORY,
    head_branch: REPOSITORY.default_branch,
    head_sha: 'e'.repeat(40),
  };
  const qodanaEvent = {
    id: qodanaRun.id,
    run_attempt: qodanaRun.run_attempt,
    workflow_id: qodanaRun.workflow_id,
    event: qodanaRun.event,
    status: qodanaRun.status,
    conclusion: qodanaRun.conclusion,
  };
  const ciRun = makeCiRun();
  const pullRequest = makePullRequest();
  const files = [{ filename: 'src/Main.kt' }];
  const listFiles = async () => files;
  const listArtifacts = async () => [
    {
      id: 700,
      name: buildArtifactName({
        sourceKind: 'code',
        runId: ciRun.id,
        runAttempt: ciRun.run_attempt,
        qodanaRunId: qodanaRun.id,
        qodanaRunAttempt: qodanaRun.run_attempt,
        headSha: ciRun.head_sha,
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
    },
  ];
  return {
    context: {
      repo: { owner: 'LMLiam', repo: 'Kotventure' },
      payload: { workflow_run: qodanaEvent },
    },
    github: {
      rest: {
        repos: {
          get: async () => ({ data: REPOSITORY }),
          compareCommitsWithBasehead: async () => ({
            data: { merge_base_commit: { sha: MERGE_BASE_SHA } },
          }),
        },
        actions: {
          getWorkflowRun: async ({ run_id: runId }) => ({
            data: runId === qodanaRun.id ? qodanaRun : ciRun,
          }),
          getWorkflow: async ({ workflow_id: workflowId }) => ({
            data: workflowId === qodanaRun.workflow_id
              ? { id: 77, name: 'Qodana', path: '.github/workflows/qodana.yml' }
              : { id: 55, name: 'CI', path: '.github/workflows/ci.yml' },
          }),
          listWorkflowRunArtifacts: listArtifacts,
        },
        pulls: {
          get: async () => ({ data: pullRequest }),
          listFiles,
        },
      },
      paginate: async (method) => {
        if (method === listArtifacts) {
          return listArtifacts();
        }
        if (method === listFiles) {
          return files;
        }
        throw new Error('unexpected publication pagination method');
      },
    },
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

test('binds Qodana artifacts to both run attempts and the source SHAs', () => {
  const name = buildArtifactName({
    sourceKind: 'code',
    runId: 100,
    runAttempt: 2,
    qodanaRunId: QODANA_RUN_ID,
    qodanaRunAttempt: QODANA_RUN_ATTEMPT,
    headSha: HEAD_SHA,
    baseSha: BASE_SHA,
  });
  assert.deepEqual(parseArtifactName(name), {
    sourceKind: 'code',
    ciRunId: 100,
    ciRunAttempt: 2,
    qodanaRunId: QODANA_RUN_ID,
    qodanaRunAttempt: QODANA_RUN_ATTEMPT,
    headSha: HEAD_SHA,
    baseSha: BASE_SHA,
  });
  assert.equal(parseArtifactName(`${name}-old`), null);
});

test('creates valid zero-result attestations from trusted code', () => {
  for (const sourceKind of ['documentation', 'release']) {
    const document = createAttestation({ sourceKind, headSha: HEAD_SHA });
    assert.equal(validateQodanaSarif(Buffer.from(JSON.stringify(document))).version, '2.1.0');
    assert.equal(document.runs[0].results.length, 0);
  }
});

test('extracts exactly one bounded SARIF file from an artifact ZIP', () => {
  const document = createAttestation({ sourceKind: 'documentation', headSha: HEAD_SHA });
  const content = Buffer.from(JSON.stringify(document));
  const archive = makeStoredZip(content);
  assert.deepEqual(extractQodanaSarifArchive(archive), content);
  assert.throws(
    () => extractQodanaSarifArchive(makeStoredZip(content, '../qodana.sarif.json')),
    /must contain only qodana\.sarif\.json/,
  );

  const duplicateEntryMetadata = Buffer.from(archive);
  duplicateEntryMetadata.writeUInt16LE(2, duplicateEntryMetadata.length - 14);
  duplicateEntryMetadata.writeUInt16LE(2, duplicateEntryMetadata.length - 12);
  assert.throws(
    () => extractQodanaSarifArchive(duplicateEntryMetadata),
    /must contain exactly one file/,
  );
});

test('rejects malformed SARIF and every standard project traversal location', () => {
  assert.throws(() => validateQodanaSarif(Buffer.from('{')), /not valid JSON/);
  const document = createAttestation({ sourceKind: 'documentation', headSha: HEAD_SHA });
  document.runs.push(structuredClone(document.runs[0]));
  assert.throws(() => validateQodanaSarif(Buffer.from(JSON.stringify(document))), /exactly one run/);

  const traversalCases = [
    (run) => { run.results[0].locations = [{ physicalLocation: { artifactLocation: { uri: '../secret.txt' } } }]; },
    (run) => { run.results[0].relatedLocations = [{ physicalLocation: { artifactLocation: { uri: '/etc/passwd' } } }]; },
    (run) => { run.results[0].fixes = [{ artifactChanges: [{ artifactLocation: { uri: 'file:///etc/passwd' } }] }]; },
    (run) => {
      run.results[0].codeFlows = [{
        threadFlows: [{
          locations: [{ location: { physicalLocation: { artifactLocation: { uri: '..\\secret.txt' } } } }],
        }],
      }];
    },
    (run) => {
      run.results[0].stacks = [{
        frames: [{ location: { physicalLocation: { artifactLocation: { uri: 'C:\\secret.txt' } } } }],
      }];
    },
    (run) => { run.artifacts = [{ location: { uri: 'https://attacker.example/source.kt' } }]; },
    (run) => { run.originalUriBaseIds = { SRCROOT: { uri: '../../outside/' } }; },
    (run) => { run.invocations = [{ workingDirectory: { uri: '/untrusted/workspace' } }]; },
    (run) => { run.results[0].analysisTarget = { uri: 'file:///untrusted/source.kt' }; },
  ];
  for (const mutate of traversalCases) {
    const resultDocument = {
      version: '2.1.0',
      runs: [{
        tool: { driver: { name: 'QDJVM' } },
        results: [{}],
      }],
    };
    mutate(resultDocument.runs[0]);
    assert.throws(
      () => validateQodanaSarif(Buffer.from(JSON.stringify(resultDocument))),
      /artifact location|escapes the project/,
    );
  }
});

test('selects only the artifact bound to the trusted Qodana workflow run', () => {
  const source = {
    sourceKind: 'code',
    runId: 100,
    runAttempt: 2,
    headSha: HEAD_SHA,
    baseSha: BASE_SHA,
  };
  const artifact = {
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
      head_branch: REPOSITORY.default_branch,
      head_sha: 'e'.repeat(40),
    },
  };
  assert.equal(selectQodanaArtifact({
    artifacts: [artifact],
    qodanaRun: {
      id: QODANA_RUN_ID,
      run_attempt: QODANA_RUN_ATTEMPT,
      head_repository: REPOSITORY,
      head_branch: REPOSITORY.default_branch,
      head_sha: 'e'.repeat(40),
    },
    source,
    repository: REPOSITORY,
  }).id, 700);
  assert.throws(
    () => selectQodanaArtifact({
      artifacts: [artifact, structuredClone(artifact)],
      qodanaRun: {
        id: QODANA_RUN_ID,
        run_attempt: QODANA_RUN_ATTEMPT,
        head_repository: REPOSITORY,
        head_branch: REPOSITORY.default_branch,
        head_sha: 'e'.repeat(40),
      },
      source,
      repository: REPOSITORY,
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
    qodanaRun: {
      id: QODANA_RUN_ID,
      run_attempt: QODANA_RUN_ATTEMPT,
      head_repository: REPOSITORY,
      head_branch: REPOSITORY.default_branch,
      head_sha: 'e'.repeat(40),
    },
    source,
    repository: REPOSITORY,
  }).id, 700);
});

test('publishes only a successful Qodana run for the current CI head', async () => {
  const inputs = makePublicationGithub();
  const publication = await resolvePublication({
    github: inputs.github,
    context: inputs.context,
  });
  assert.deepEqual(publication, {
    artifactId: 700,
    headSha: HEAD_SHA,
    pullNumber: 42,
    sourceKind: 'code',
  });
});

test('resolves a successful CI source and computes its merge base', async () => {
  const source = await resolveCiRun({
    github: makeGithub(),
    owner: 'LMLiam',
    repo: 'Kotventure',
    runId: 100,
  });
  assert.equal(source.sourceKind, 'code');
  assert.equal(source.mergeBaseSha, MERGE_BASE_SHA);
  assert.equal(source.pullRequest, 42);
});

test('resolves same-repository and fork pull-request heads', async () => {
  const forkRepository = {
    full_name: 'contributor/Kotventure',
    id: 2,
  };
  const pullRequest = makePullRequest({ headRepository: forkRepository });
  const run = makeCiRun({ headRepository: forkRepository });
  const source = await resolveCiRun({
    github: makeGithub({ run, pullRequest }),
    owner: 'LMLiam',
    repo: 'Kotventure',
    runId: 100,
  });
  assert.equal(source.headRepository, forkRepository.full_name);
  assert.equal(source.headSha, HEAD_SHA);
});

test('filters commit-associated pull requests before selecting the fallback', async () => {
  const run = makeCiRun({ pullRequests: [] });
  const pullRequest = makePullRequest();
  const unrelatedPullRequest = makePullRequest({ headRef: 'unrelated' });
  unrelatedPullRequest.number = 99;
  const source = await resolveCiRun({
    github: makeGithub({
      run,
      pullRequest,
      associatedPullRequests: [unrelatedPullRequest, pullRequest],
    }),
    owner: 'LMLiam',
    repo: 'Kotventure',
    runId: 100,
  });
  assert.equal(source.pullRequest, 42);
});

test('accepts trusted CI only from the current repository default branch', async () => {
  const run = makeCiRun({
    event: 'workflow_dispatch',
    headBranch: REPOSITORY.default_branch,
    pullRequests: [],
  });
  const source = await resolveTrustedCiRun({
    github: makeGithub({ run }),
    owner: 'LMLiam',
    repo: 'Kotventure',
    runId: run.id,
  });
  assert.deepEqual(source, {
    event: 'workflow_dispatch',
    headSha: HEAD_SHA,
  });

  run.head_branch = 'feature/untrusted-dispatch';
  await assert.rejects(
    resolveTrustedCiRun({
      github: makeGithub({ run }),
      owner: 'LMLiam',
      repo: 'Kotventure',
      runId: run.id,
    }),
    /default branch/,
  );
});

test('uses the documentation attestation path without computing a merge base', async () => {
  const files = [{ filename: 'docs/CI.md' }];
  const pullRequest = makePullRequest({ files });
  const source = await resolveCiRun({
    github: makeGithub({ pullRequest, files }),
    owner: 'LMLiam',
    repo: 'Kotventure',
    runId: 100,
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
      fetchImpl: async (location, options) => {
        if (location === 'https://api.github.test/repos/LMLiam/Kotventure/actions/artifacts/700/zip') {
          assert.equal(options.redirect, 'manual');
          assert.equal(options.headers.authorization, 'Bearer test-token');
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
      },
    });
    assert.equal(filePath, path.join(directory, 'qodana.sarif.json'));
    assert.deepEqual(JSON.parse(fs.readFileSync(filePath, 'utf8')), document);
  } finally {
    fs.rmSync(directory, { recursive: true, force: true });
  }
});

test('marks a pull request that advanced after CI as stale', async () => {
  const pullRequest = makePullRequest();
  pullRequest.head.sha = 'd'.repeat(40);
  await assert.rejects(
    resolveCiRun({
      github: makeGithub({ pullRequest }),
      owner: 'LMLiam',
      repo: 'Kotventure',
      runId: 100,
    }),
    (error) => error instanceof QodanaSourceRejectedError && error.stale,
  );
});

test('marks a changed base or closed pull request as stale', async () => {
  const changedBase = makePullRequest({ baseSha: 'd'.repeat(40) });
  await assert.rejects(
    resolveCiRun({
      github: makeGithub({ pullRequest: changedBase }),
      owner: 'LMLiam',
      repo: 'Kotventure',
      runId: 100,
      expectedBaseSha: BASE_SHA,
    }),
    (error) => error instanceof QodanaSourceRejectedError && error.stale,
  );

  const closedPullRequest = makePullRequest({ state: 'closed' });
  await assert.rejects(
    resolveCiRun({
      github: makeGithub({ pullRequest: closedPullRequest }),
      owner: 'LMLiam',
      repo: 'Kotventure',
      runId: 100,
    }),
    (error) => error instanceof QodanaSourceRejectedError && error.stale,
  );
});
