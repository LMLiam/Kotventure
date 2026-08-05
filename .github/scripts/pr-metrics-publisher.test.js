'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const os = require('os');
const path = require('path');
const zlib = require('zlib');
const { serializeMetricsResult } = require('../actions/pr-metrics-comment/lib/metrics-result.js');
const {
  PublicationRejectedError,
  expectedArtifactName,
  selectMetricsArtifact,
  validateResultProvenance,
  validateWorkflowSource,
} = require('./pr-metrics-publisher-validation.js');
const {
  publishMetrics,
  resolveSource,
} = require('./pr-metrics-publisher.js');
const { extractMetricsResultArchive } = require('./pr-metrics-publisher-archive.js');
const { MAX_ARTIFACT_BYTES } = require('./pr-metrics-publisher-contract.js');
const {
  downloadMetricsArtifact,
  readMetricsArtifact,
} = require('./pr-metrics-publisher-storage.js');

const BASE_SHA = 'a'.repeat(40);
const HEAD_SHA = 'b'.repeat(40);
const repository = { full_name: 'LMLiam/Kotventure', id: 1, default_branch: 'master' };
const headRepository = { full_name: 'LMLiam/Kotventure-fix', id: 2 };

function makeInputs() {
  return {
    eventRun: {
      id: 100,
      run_attempt: 2,
      event: 'pull_request',
      status: 'completed',
      conclusion: 'success',
      head_sha: HEAD_SHA,
    },
    run: {
      id: 100,
      run_attempt: 2,
      workflow_id: 55,
      event: 'pull_request',
      status: 'completed',
      conclusion: 'success',
      repository,
      pull_requests: [{ number: 42 }],
      head_repository: headRepository,
      head_branch: 'fix/metrics',
      head_sha: HEAD_SHA,
    },
    workflow: { id: 55, name: 'CI', path: '.github/workflows/ci.yml' },
    repository,
    pullRequest: {
      number: 42,
      state: 'open',
      base: { repo: repository, ref: 'master', sha: BASE_SHA },
      head: { repo: headRepository, ref: 'fix/metrics', sha: HEAD_SHA },
    },
    defaultBranch: 'master',
  };
}

function makeSource() {
  const inputs = makeInputs();
  return {
    ...validateWorkflowSource(inputs),
    repositoryId: repository.id,
  };
}

function makeArtifact(source = makeSource()) {
  return {
    id: 700,
    name: expectedArtifactName({ id: source.runId, run_attempt: source.runAttempt }),
    expired: false,
    size_in_bytes: 200,
    workflow_run: {
      id: source.runId,
      repository_id: source.repositoryId,
      head_repository_id: source.headRepositoryId,
      head_branch: source.headRef,
      head_sha: source.headSha,
    },
  };
}

function makeZip(content, compressionMethod = 8) {
  const fileName = Buffer.from('pr-metrics-result.json', 'utf8');
  const uncompressed = Buffer.isBuffer(content) ? content : Buffer.from(content, 'utf8');
  const compressed = compressionMethod === 8
    ? zlib.deflateRawSync(uncompressed)
    : uncompressed;
  const local = Buffer.alloc(30 + fileName.length);
  local.writeUInt32LE(0x04034b50, 0);
  local.writeUInt16LE(20, 4);
  local.writeUInt16LE(0, 6);
  local.writeUInt16LE(compressionMethod, 8);
  local.writeUInt32LE(compressed.length, 18);
  local.writeUInt32LE(uncompressed.length, 22);
  local.writeUInt16LE(fileName.length, 26);
  fileName.copy(local, 30);

  const central = Buffer.alloc(46 + fileName.length);
  central.writeUInt32LE(0x02014b50, 0);
  central.writeUInt16LE(20, 4);
  central.writeUInt16LE(20, 6);
  central.writeUInt16LE(0, 8);
  central.writeUInt16LE(compressionMethod, 10);
  central.writeUInt32LE(compressed.length, 20);
  central.writeUInt32LE(uncompressed.length, 24);
  central.writeUInt16LE(fileName.length, 28);
  fileName.copy(central, 46);

  const end = Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50, 0);
  end.writeUInt16LE(1, 8);
  end.writeUInt16LE(1, 10);
  end.writeUInt32LE(central.length, 12);
  end.writeUInt32LE(local.length + compressed.length, 16);
  return Buffer.concat([local, compressed, central, end]);
}

function makeWorkflowRunContext(run = makeInputs().run) {
  return {
    repo: { owner: 'LMLiam', repo: 'Kotventure' },
    payload: {
      workflow_run: {
        id: run.id,
        run_attempt: run.run_attempt,
        workflow_id: run.workflow_id,
        event: run.event,
        status: run.status,
        conclusion: run.conclusion,
        head_sha: run.head_sha,
      },
    },
  };
}

function makeGithub({
  run,
  associatedPullRequests = [],
  artifacts,
  pullRequest = makeInputs().pullRequest,
}) {
  const listAssociatedPullRequests = () => {};
  const listWorkflowRunArtifacts = () => {};
  return {
    rest: {
      repos: {
        get: async () => ({ data: repository }),
        listPullRequestsAssociatedWithCommit: listAssociatedPullRequests,
      },
      actions: {
        getWorkflowRun: async () => ({ data: run }),
        getWorkflow: async () => ({ data: { id: 55, name: 'CI', path: '.github/workflows/ci.yml' } }),
        listWorkflowRunArtifacts,
      },
      pulls: {
        get: async () => ({ data: pullRequest }),
      },
    },
    paginate: async (method) => {
      if (method === listAssociatedPullRequests) {
        return associatedPullRequests;
      }
      if (method === listWorkflowRunArtifacts) {
        return artifacts;
      }
      throw new Error('unexpected pagination method');
    },
  };
}

function makeResult() {
  return serializeMetricsResult({
    context: {
      repo: { owner: 'LMLiam', repo: 'Kotventure' },
      eventName: 'pull_request',
      payload: {
        pull_request: {
          number: 42,
          base: { repo: { full_name: repository.full_name }, ref: 'master', sha: BASE_SHA },
          head: { repo: { full_name: headRepository.full_name }, ref: 'fix/metrics', sha: HEAD_SHA },
        },
      },
    },
    runId: '100',
    runAttempt: '2',
    headCoverage: null,
    baseCoverage: null,
    headJars: new Map([['core', { size: 1, classes: 1 }]]),
    baseJars: new Map(),
    headMetrics: null,
    baseMetrics: null,
    patchCoverage: null,
    apiSurface: null,
  });
}

function assertRejected(callback) {
  assert.throws(callback, (error) => error instanceof PublicationRejectedError);
}

test('validates the workflow, run, and current pull request identity', () => {
  const source = makeSource();
  assert.equal(source.runId, 100);
  assert.equal(source.headRepositoryId, 2);
  assert.equal(source.baseSha, BASE_SHA);
});

test('resolves a pull request through its head commit when the run omits pull requests', async () => {
  const inputs = makeInputs();
  inputs.run.pull_requests = [];
  const artifact = makeArtifact(makeSource());
  const source = await resolveSource({
    github: makeGithub({
      run: inputs.run,
      associatedPullRequests: [{ number: 42 }],
      artifacts: [artifact],
    }),
    context: makeWorkflowRunContext(inputs.run),
  });
  assert.equal(source.pullRequest, 42);
  assert.equal(source.artifactName, 'pr-metrics-result-100-2');
});

test('rejects a changed workflow or source run identity', () => {
  for (const change of [
    (inputs) => { inputs.eventRun.conclusion = 'failure'; },
    (inputs) => { inputs.run.run_attempt = 3; },
    (inputs) => { inputs.run.workflow_id = 56; },
    (inputs) => { inputs.workflow.name = 'Untrusted CI'; },
    (inputs) => { inputs.pullRequest.base.ref = 'release'; },
    (inputs) => { inputs.pullRequest.head.sha = 'c'.repeat(40); },
    (inputs) => { inputs.run.head_repository = { full_name: 'other/repo', id: 3 }; },
  ]) {
    const inputs = structuredClone(makeInputs());
    change(inputs);
    assertRejected(() => validateWorkflowSource(inputs));
  }
});

test('accepts only the exact, current metrics artifact', () => {
  const source = makeSource();
  const artifact = makeArtifact(source);
  assert.equal(selectMetricsArtifact({ artifacts: [artifact], run: makeInputs().run, source }).id, 700);

  for (const change of [
    (value) => { value.expired = true; },
    (value) => { value.size_in_bytes = MAX_ARTIFACT_BYTES + 1; },
    (value) => { value.workflow_run.id = 101; },
    (value) => { value.workflow_run.head_sha = 'c'.repeat(40); },
  ]) {
    const changed = structuredClone(artifact);
    change(changed);
    assertRejected(() => selectMetricsArtifact({ artifacts: [changed], run: makeInputs().run, source }));
  }
  assertRejected(() => selectMetricsArtifact({ artifacts: [], run: makeInputs().run, source }));
  assertRejected(() => selectMetricsArtifact({ artifacts: [artifact, structuredClone(artifact)], run: makeInputs().run, source }));
});

test('extracts one bounded metrics result from a ZIP archive', () => {
  const content = Buffer.from(JSON.stringify(makeResult()), 'utf8');
  const extracted = extractMetricsResultArchive(makeZip(content));
  assert.deepEqual(extracted, content);
});

test('rejects a compressed archive whose entry expands beyond the result limit', () => {
  const archive = makeZip(Buffer.alloc(64 * 1024 + 1, 0x78));
  assert.ok(archive.length < MAX_ARTIFACT_BYTES);
  assert.throws(() => extractMetricsResultArchive(archive), /outside the size limit/);
});

test('downloads the archive through a redirect and writes only the validated result', async (t) => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'pr-metrics-download-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  const archive = makeZip(Buffer.from(JSON.stringify(makeResult()), 'utf8'));
  const apiUrl = 'https://api.github.test';
  const filePath = await downloadMetricsArtifact({
    owner: 'LMLiam',
    repo: 'Kotventure',
    artifactId: 700,
    outputDirectory: directory,
    apiUrl,
    token: 'test-token',
    fetchImpl: async (location, options) => {
      if (location === `${apiUrl}/repos/LMLiam/Kotventure/actions/artifacts/700/zip`) {
        assert.equal(options.redirect, 'manual');
        assert.equal(options.headers.authorization, 'Bearer test-token');
        assert.equal(typeof options.signal?.aborted, 'boolean');
        return {
          status: 302,
          headers: { location: 'https://artifact.example/result.zip' },
        };
      }
      assert.equal(location, 'https://artifact.example/result.zip');
      assert.equal(options.redirect, 'follow');
      assert.equal(typeof options.signal?.aborted, 'boolean');
      return {
        ok: true,
        headers: { 'content-length': String(archive.length) },
        arrayBuffer: async () => archive,
      };
    },
  });
  assert.equal(filePath, path.join(directory, 'pr-metrics-result.json'));
  assert.equal(readMetricsArtifact(directory).metrics.headJars[0].module, 'core');
});

test('rejects a result whose provenance is stale or points to another PR', () => {
  const source = makeSource();
  const result = makeResult();
  assert.equal(validateResultProvenance(result, source), result);

  const replacements = {
    runId: 101,
    runAttempt: 3,
    pullRequest: 43,
    baseSha: 'c'.repeat(40),
    headSha: 'c'.repeat(40),
  };
  for (const [key, value] of Object.entries(replacements)) {
    const changed = structuredClone(result);
    changed.provenance[key] = value;
    assertRejected(() => validateResultProvenance(changed, source));
  }
});

test('skips publication when the current pull request no longer matches the source run', async () => {
  const inputs = makeInputs();
  const warnings = [];
  await publishMetrics({
    github: makeGithub({
      run: inputs.run,
      artifacts: [],
      pullRequest: { ...inputs.pullRequest, state: 'closed' },
    }),
    context: makeWorkflowRunContext(inputs.run),
    core: {
      warning: (message) => warnings.push(message),
      info: () => {},
    },
    artifactDirectory: 'unused',
  });
  assert.equal(warnings.length, 1);
  assert.match(warnings[0], /Metrics publication skipped/);
});

test('reads one regular, bounded, valid result file from the downloaded artifact', (t) => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'pr-metrics-result-'));
  t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
  fs.writeFileSync(path.join(directory, 'pr-metrics-result.json'), JSON.stringify(makeResult()));
  assert.equal(readMetricsArtifact(directory).metrics.headJars[0].module, 'core');
});

test('rejects missing, extra, malformed, oversized, and linked artifact files', (t) => {
  const cases = [
    (directory) => {},
    (directory) => { fs.writeFileSync(path.join(directory, 'other.json'), '{}'); },
    (directory) => { fs.writeFileSync(path.join(directory, 'pr-metrics-result.json'), '{'); },
    (directory) => { fs.writeFileSync(path.join(directory, 'pr-metrics-result.json'), 'x'.repeat(64 * 1024 + 1)); },
    (directory) => {
      const target = path.join(directory, 'target.json');
      fs.writeFileSync(target, JSON.stringify(makeResult()));
      fs.symlinkSync(target, path.join(directory, 'pr-metrics-result.json'));
    },
  ];
  for (const createCase of cases) {
    const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'pr-metrics-invalid-'));
    t.after(() => fs.rmSync(directory, { recursive: true, force: true }));
    createCase(directory);
    assert.throws(() => readMetricsArtifact(directory));
  }
});
