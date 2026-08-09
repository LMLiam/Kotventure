'use strict';

const {
  MAX_ARTIFACT_BYTES,
  MAX_CHECK_ARTIFACT_BYTES,
  MAX_SARIF_BYTES,
  MAX_SARIF_RESULTS,
  QODANA_ARTIFACT_PREFIX,
  QODANA_CHECK_ARTIFACT_PREFIX,
  QODANA_WORKFLOW_NAME,
  QODANA_WORKFLOW_PATH,
  parseArtifactName,
  parseCheckArtifactName,
} = require('./qodana-contract.js');

class QodanaPublicationRejectedError extends Error {}

function reject(message) {
  throw new QodanaPublicationRejectedError(message);
}

function requireObject(value, label) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    reject(`${label} is missing`);
  }
  return value;
}

function requireEqual(actual, expected, label) {
  if (actual !== expected) {
    reject(`${label} does not match the trusted value`);
  }
}

function requirePositiveInteger(value, label) {
  if (!Number.isSafeInteger(value) || value < 1) {
    reject(`${label} is invalid`);
  }
  return value;
}

function requireSha(value, label) {
  if (typeof value !== 'string' || !/^[0-9a-f]{40}$/.test(value)) {
    reject(`${label} is invalid`);
  }
  return value;
}

function validateQodanaWorkflowSource({ eventRun, run, workflow, repository }) {
  requireObject(eventRun, 'workflow_run event');
  requireObject(run, 'workflow run');
  requireObject(workflow, 'workflow');
  requireObject(repository, 'repository');

  requireEqual(eventRun.id, run.id, 'workflow run id');
  requireEqual(eventRun.run_attempt, run.run_attempt, 'workflow run attempt');
  if (eventRun.workflow_id != null) {
    requireEqual(eventRun.workflow_id, run.workflow_id, 'workflow run workflow id');
  }
  requireEqual(eventRun.event, 'workflow_run', 'workflow run event');
  requireEqual(eventRun.status, 'completed', 'workflow run event status');
  requireEqual(eventRun.conclusion, run.conclusion, 'workflow run event conclusion');
  requireEqual(run.event, 'workflow_run', 'workflow run event');
  requireEqual(run.status, 'completed', 'workflow run status');
  if (!['success', 'failure', 'cancelled', 'timed_out'].includes(run.conclusion)) {
    reject('workflow run conclusion is invalid');
  }
  requireEqual(run.repository?.full_name, repository.full_name, 'workflow run repository');
  requireEqual(run.repository?.id, repository.id, 'workflow run repository id');
  requireEqual(run.head_repository?.full_name, repository.full_name, 'workflow head repository');
  requireEqual(run.head_repository?.id, repository.id, 'workflow head repository id');
  requireEqual(run.head_branch, repository.default_branch, 'workflow run default branch');
  requireSha(run.head_sha, 'workflow run head SHA');
  requireEqual(workflow.id, run.workflow_id, 'workflow identity');
  requireEqual(workflow.name, QODANA_WORKFLOW_NAME, 'workflow name');
  requireEqual(workflow.path, QODANA_WORKFLOW_PATH, 'workflow path');

  return {
    runId: requirePositiveInteger(run.id, 'Qodana workflow run id'),
    runAttempt: requirePositiveInteger(run.run_attempt, 'Qodana workflow run attempt'),
    repository: repository.full_name,
    conclusion: run.conclusion,
  };
}

function selectQodanaCheckArtifact({ artifacts, qodanaRun, repository }) {
  if (!Array.isArray(artifacts)) {
    reject('workflow artifacts are missing');
  }
  const candidates = artifacts
    .filter((artifact) => typeof artifact?.name === 'string'
      && artifact.name.startsWith(QODANA_CHECK_ARTIFACT_PREFIX))
    .map((artifact) => ({ artifact, descriptor: parseCheckArtifactName(artifact.name) }))
    .filter(({ descriptor }) => descriptor?.qodanaRunId === qodanaRun.id
      && descriptor.qodanaRunAttempt === qodanaRun.run_attempt);
  if (candidates.length !== 1) {
    reject(`expected exactly one Qodana check artifact, found ${candidates.length}`);
  }
  const [{ artifact, descriptor }] = candidates;
  requirePositiveInteger(artifact.id, 'Qodana check artifact id');
  if (artifact.expired !== false) {
    reject('Qodana check artifact is expired');
  }
  if (!Number.isSafeInteger(artifact.size_in_bytes)
    || artifact.size_in_bytes < 1
    || artifact.size_in_bytes > MAX_CHECK_ARTIFACT_BYTES) {
    reject('Qodana check artifact size is invalid');
  }
  const artifactRun = requireObject(artifact.workflow_run, 'Qodana check artifact workflow run');
  requireEqual(artifactRun.id, qodanaRun.id, 'Qodana check artifact workflow run id');
  requireEqual(artifactRun.repository_id, repository.id, 'Qodana check artifact repository id');
  requireEqual(artifactRun.head_repository_id, qodanaRun.head_repository?.id, 'Qodana check artifact head repository id');
  requireEqual(artifactRun.head_branch, qodanaRun.head_branch, 'Qodana check artifact head branch');
  requireEqual(artifactRun.head_sha, qodanaRun.head_sha, 'Qodana check artifact head SHA');
  return { artifact, descriptor };
}

function validateQodanaCheckSource({ descriptor, source }) {
  requireEqual(descriptor.sourceKind, source.sourceKind, 'Qodana check source kind');
  requireEqual(descriptor.ciRunId, source.runId, 'Qodana check CI run id');
  requireEqual(descriptor.ciRunAttempt, source.runAttempt, 'Qodana check CI run attempt');
  requireEqual(descriptor.headSha, source.headSha, 'Qodana check head SHA');
  requireEqual(descriptor.baseSha, source.baseSha, 'Qodana check base SHA');
}

function selectQodanaRunArtifact({ artifacts, qodanaRun, repository }) {
  if (!Array.isArray(artifacts)) {
    reject('workflow artifacts are missing');
  }
  const candidates = artifacts
    .filter((artifact) => typeof artifact?.name === 'string'
      && artifact.name.startsWith(QODANA_ARTIFACT_PREFIX))
    .map((artifact) => ({ artifact, descriptor: parseArtifactName(artifact.name) }))
    .filter(({ descriptor }) => descriptor?.qodanaRunId === qodanaRun.id
      && descriptor.qodanaRunAttempt === qodanaRun.run_attempt);
  if (candidates.length !== 1) {
    reject(`expected exactly one Qodana SARIF artifact, found ${candidates.length}`);
  }
  const [{ artifact, descriptor }] = candidates;
  requireEqual(descriptor.qodanaRunId, qodanaRun.id, 'Qodana workflow run id');
  requireEqual(descriptor.qodanaRunAttempt, qodanaRun.run_attempt, 'Qodana workflow run attempt');
  requirePositiveInteger(artifact.id, 'Qodana artifact id');
  if (artifact.expired !== false) {
    reject('Qodana artifact is expired');
  }
  if (!Number.isSafeInteger(artifact.size_in_bytes)
    || artifact.size_in_bytes < 1
    || artifact.size_in_bytes > MAX_ARTIFACT_BYTES) {
    reject('Qodana artifact size is invalid');
  }
  const artifactRun = requireObject(artifact.workflow_run, 'Qodana artifact workflow run');
  requireEqual(artifactRun.id, qodanaRun.id, 'Qodana artifact workflow run id');
  requireEqual(artifactRun.repository_id, repository.id, 'Qodana artifact repository id');
  requireEqual(artifactRun.head_repository_id, qodanaRun.head_repository?.id, 'Qodana artifact head repository id');
  requireEqual(artifactRun.head_branch, qodanaRun.head_branch, 'Qodana artifact head branch');
  requireEqual(artifactRun.head_sha, qodanaRun.head_sha, 'Qodana artifact head SHA');
  return { artifact, descriptor };
}

function validateQodanaArtifactSource({ descriptor, source }) {
  requireEqual(descriptor.sourceKind, source.sourceKind, 'Qodana source kind');
  requireEqual(descriptor.ciRunId, source.runId, 'CI workflow run id');
  requireEqual(descriptor.ciRunAttempt, source.runAttempt, 'CI workflow run attempt');
  requireEqual(descriptor.headSha, source.headSha, 'Qodana artifact head SHA');
  requireEqual(descriptor.baseSha, source.baseSha, 'Qodana artifact base SHA');
}

function selectQodanaArtifact({ artifacts, qodanaRun, source, repository }) {
  const selection = selectQodanaRunArtifact({ artifacts, qodanaRun, repository });
  validateQodanaArtifactSource({ descriptor: selection.descriptor, source });
  const { artifact } = selection;
  return artifact;
}

function validateArtifactLocation(location) {
  if (!location || typeof location !== 'object' || Array.isArray(location)) {
    reject('SARIF artifact location is invalid');
  }
  const { uri, uriBaseId } = location;
  if (uriBaseId != null
    && (typeof uriBaseId !== 'string' || !/^[A-Za-z0-9_.-]{1,128}$/.test(uriBaseId))) {
    reject('SARIF artifact location base id is invalid');
  }
  if (uri == null) {
    return;
  }
  if (typeof uri !== 'string' || uri.length > 4096 || uri.includes('\u0000')) {
    reject('SARIF artifact location is invalid');
  }
  if (uri.startsWith('/')
    || /^[A-Za-z]:[\\/]/.test(uri)
    || /^[A-Za-z][A-Za-z0-9+.-]*:/.test(uri)
    || uri.split(/[\\/]/).includes('..')) {
    reject('SARIF artifact location escapes the project');
  }
}

function validateNestedArtifactLocations(root) {
  const pending = [{ value: root, depth: 0 }];
  let visited = 0;
  while (pending.length > 0) {
    const { value, depth } = pending.pop();
    visited += 1;
    if (visited > 250_000 || depth > 100) {
      reject('SARIF structure is too complex');
    }
    if (!value || typeof value !== 'object') {
      continue;
    }
    if (Array.isArray(value)) {
      for (const item of value) {
        pending.push({ value: item, depth: depth + 1 });
      }
      continue;
    }
    if (Object.hasOwn(value, 'uri') || Object.hasOwn(value, 'uriBaseId')) {
      validateArtifactLocation(value);
    }
    for (const [key, child] of Object.entries(value)) {
      if (key === 'artifactLocation') {
        validateArtifactLocation(child);
      }
      pending.push({ value: child, depth: depth + 1 });
    }
  }
}

function validateQodanaSarif(value) {
  const bytes = Buffer.isBuffer(value) ? value : Buffer.from(String(value), 'utf8');
  if (bytes.length < 1 || bytes.length > MAX_SARIF_BYTES) {
    reject('SARIF size is invalid');
  }
  let document;
  try {
    document = JSON.parse(bytes.toString('utf8'));
  } catch (error) {
    reject(`SARIF is not valid JSON: ${error.message}`);
  }
  requireObject(document, 'SARIF document');
  requireEqual(document.version, '2.1.0', 'SARIF version');
  if (!Array.isArray(document.runs) || document.runs.length !== 1) {
    reject('SARIF must contain exactly one run');
  }
  const [run] = document.runs;
  requireObject(run, 'SARIF run');
  const tool = requireObject(run.tool, 'SARIF tool');
  const driver = requireObject(tool.driver, 'SARIF driver');
  if (driver.name !== 'QDJVM') {
    reject('SARIF driver name is invalid');
  }
  if (!Array.isArray(run.results) || run.results.length > MAX_SARIF_RESULTS) {
    reject('SARIF results are invalid');
  }
  for (const result of run.results) {
    requireObject(result, 'SARIF result');
  }
  if (run.artifacts != null) {
    if (!Array.isArray(run.artifacts)) {
      reject('SARIF artifacts are invalid');
    }
    for (const artifact of run.artifacts) {
      const location = requireObject(artifact, 'SARIF artifact').location;
      if (location != null) {
        validateArtifactLocation(location);
      }
    }
  }
  if (run.originalUriBaseIds != null) {
    const uriBases = requireObject(run.originalUriBaseIds, 'SARIF original URI bases');
    for (const location of Object.values(uriBases)) {
      validateArtifactLocation(location);
    }
  }
  validateNestedArtifactLocations(run);
  return document;
}

module.exports = {
  QodanaPublicationRejectedError,
  selectQodanaArtifact,
  selectQodanaCheckArtifact,
  selectQodanaRunArtifact,
  validateQodanaArtifactSource,
  validateQodanaCheckSource,
  validateQodanaSarif,
  validateQodanaWorkflowSource,
};
