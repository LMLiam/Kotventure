'use strict';

const { createValidators } = require('./shared/validation.js');
const {
  DOCUMENTATION_PATH_PATTERNS,
  RELEASE_ONLY_FILES,
  changedPathNames,
  classifyChangedFiles,
  isDocumentationPath,
  isSafeRepositoryPath,
} = require('./shared/path-classification.js');

const CI_WORKFLOW_NAME = 'CI';
const CI_WORKFLOW_PATH = '.github/workflows/ci.yml';
const QODANA_WORKFLOW_NAME = 'Qodana';
const QODANA_WORKFLOW_PATH = '.github/workflows/qodana.yml';
const QODANA_ARTIFACT_PREFIX = 'qodana-sarif-';
const QODANA_SARIF_FILE_NAME = 'qodana.sarif.json';
const MAX_ARTIFACT_BYTES = 16 * 1024 * 1024;
const MAX_SARIF_BYTES = 16 * 1024 * 1024;
const MAX_SARIF_RESULTS = 50_000;

const {
  requireInteger: requirePositiveInteger,
  requireSha,
} = createValidators((message) => {
  throw new Error(message);
});

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
  if (typeof name !== 'string') return null;
  const match = name.match(
    new RegExp(`^${QODANA_ARTIFACT_PREFIX}(code|documentation|release)-(\\d+)-(\\d+)-([0-9a-f]{40})-([0-9a-f]{40})$`),
  );
  if (!match) return null;
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
