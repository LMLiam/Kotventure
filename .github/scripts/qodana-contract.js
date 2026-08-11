'use strict';

const { createValidators } = require('./shared/validation.js');

const CI_WORKFLOW_NAME = 'CI';
const CI_WORKFLOW_PATH = '.github/workflows/ci.yml';
const QODANA_WORKFLOW_NAME = 'Qodana';
const QODANA_WORKFLOW_PATH = '.github/workflows/qodana.yml';
const QODANA_ARTIFACT_PREFIX = 'qodana-sarif-';
const QODANA_SARIF_FILE_NAME = 'qodana.sarif.json';
const MAX_ARTIFACT_BYTES = 16 * 1024 * 1024;
const MAX_SARIF_BYTES = 16 * 1024 * 1024;
const MAX_SARIF_RESULTS = 50_000;

const RELEASE_ONLY_FILES = new Set([
  'CHANGELOG.md',
  '.release-please-manifest.json',
  'gradle/libs.versions.toml',
]);

const DOCUMENTATION_PATH_PATTERNS = [
  /^README\.md$/,
  /^LICENSE\.md$/,
  /^AGENTS\.md$/,
  /^docs\/.+$/,
  /^\.github\/(?:CONTRIBUTING|SUPPORT)\.md$/,
  /^\.github\/pull_request_template\.md$/,
  /^\.github\/(?:PULL_REQUEST_TEMPLATE|ISSUE_TEMPLATE)\/[^/]+$/,
  /^modules\/[^/]+\/README\.md$/,
  /^assets\/.+\.(?:svg|png|jpe?g|gif|webp)$/i,
];

const {
  requireInteger: requirePositiveInteger,
  requireSha,
} = createValidators((message) => {
  throw new Error(message);
});

function isSafeRepositoryPath(name) {
  return typeof name === 'string'
    && name.length > 0
    && !name.includes('\\')
    && !name.startsWith('/')
    && !name.split('/').includes('..')
    && !name.includes('\u0000');
}

function isDocumentationPath(name) {
  return isSafeRepositoryPath(name)
    && DOCUMENTATION_PATH_PATTERNS.some((pattern) => pattern.test(name));
}

function changedPathNames(files) {
  if (!Array.isArray(files) || files.length === 0) {
    return null;
  }

  const paths = [];
  for (const file of files) {
    if (!file || typeof file.filename !== 'string' || file.filename.length === 0) {
      return null;
    }
    paths.push(file.filename);
    if (file.previous_filename != null) {
      if (typeof file.previous_filename !== 'string' || file.previous_filename.length === 0) {
        return null;
      }
      paths.push(file.previous_filename);
    }
  }
  return paths;
}

function classifyChangedFiles(files) {
  const paths = changedPathNames(files);
  if (!paths) {
    return 'code';
  }
  if (paths.every(isDocumentationPath)) {
    return 'documentation';
  }
  if (paths.every((name) => isSafeRepositoryPath(name) && RELEASE_ONLY_FILES.has(name))) {
    return 'release-candidate';
  }
  return 'code';
}

function buildArtifactName({
  sourceKind,
  qodanaRunId,
  qodanaRunAttempt,
  headSha,
  baseSha,
}) {
  if (!['code', 'documentation', 'release'].includes(sourceKind)) {
    throw new Error('Qodana source kind is invalid');
  }
  return `${QODANA_ARTIFACT_PREFIX}${sourceKind}-${requirePositiveInteger(qodanaRunId, 'Qodana workflow run id')}-${requirePositiveInteger(qodanaRunAttempt, 'Qodana workflow run attempt')}-${requireSha(headSha, 'head SHA')}-${requireSha(baseSha, 'base SHA')}`;
}

function parseArtifactName(name) {
  if (typeof name !== 'string') {
    return null;
  }
  const match = name.match(
    new RegExp(`^${QODANA_ARTIFACT_PREFIX}(code|documentation|release)-(\\d+)-(\\d+)-([0-9a-f]{40})-([0-9a-f]{40})$`),
  );
  if (!match) {
    return null;
  }
  const qodanaRunId = Number(match[2]);
  const qodanaRunAttempt = Number(match[3]);
  if (!Number.isSafeInteger(qodanaRunId) || qodanaRunId < 1
    || !Number.isSafeInteger(qodanaRunAttempt) || qodanaRunAttempt < 1) {
    return null;
  }
  return {
    sourceKind: match[1],
    qodanaRunId,
    qodanaRunAttempt,
    headSha: match[4],
    baseSha: match[5],
  };
}

module.exports = {
  CI_WORKFLOW_NAME,
  CI_WORKFLOW_PATH,
  DOCUMENTATION_PATH_PATTERNS,
  MAX_ARTIFACT_BYTES,
  MAX_SARIF_BYTES,
  MAX_SARIF_RESULTS,
  QODANA_ARTIFACT_PREFIX,
  QODANA_SARIF_FILE_NAME,
  QODANA_WORKFLOW_NAME,
  QODANA_WORKFLOW_PATH,
  RELEASE_ONLY_FILES,
  buildArtifactName,
  changedPathNames,
  classifyChangedFiles,
  isDocumentationPath,
  isSafeRepositoryPath,
  parseArtifactName,
};
