'use strict';

const {
  MAX_DECLARATIONS,
  MAX_MODULES,
  MODULE_PATTERN,
  REPOSITORY_PATTERN,
  SCHEMA_VERSION,
  SHA_PATTERN,
  WORKFLOW_NAME,
  boundedDeclaration,
  boundedInteger,
  boundedRef,
  boundedString,
  exactKeys,
} = require('./metrics-result-contract.js');

function validateCoverage(value, label) {
  if (value == null) {
    return null;
  }
  exactKeys(value, ['modules', 'totalMissed', 'totalCovered'], label);
  if (!Array.isArray(value.modules)) {
    throw new Error(`${label}.modules must be an array`);
  }
  if (value.modules.length > MAX_MODULES) {
    throw new Error(`${label}.modules has too many entries`);
  }
  const names = new Set();
  for (const [index, module] of value.modules.entries()) {
    exactKeys(module, ['name', 'missed', 'covered'], `${label}.modules[${index}]`);
    boundedString(module.name, MODULE_PATTERN, `${label}.modules[${index}].name`);
    if (names.has(module.name)) {
      throw new Error(`${label}.modules contains a duplicate name`);
    }
    names.add(module.name);
    boundedInteger(module.missed, `${label}.modules[${index}].missed`);
    boundedInteger(module.covered, `${label}.modules[${index}].covered`);
  }
  boundedInteger(value.totalMissed, `${label}.totalMissed`);
  boundedInteger(value.totalCovered, `${label}.totalCovered`);
  return value;
}

function validateJars(value, label) {
  if (!Array.isArray(value)) {
    throw new Error(`${label} must be an array`);
  }
  if (value.length > MAX_MODULES) {
    throw new Error(`${label} has too many entries`);
  }
  const names = new Set();
  for (const [index, jar] of value.entries()) {
    exactKeys(jar, ['module', 'size', 'classes'], `${label}[${index}]`);
    boundedString(jar.module, MODULE_PATTERN, `${label}[${index}].module`);
    if (names.has(jar.module)) {
      throw new Error(`${label} contains a duplicate module`);
    }
    names.add(jar.module);
    boundedInteger(jar.size, `${label}[${index}].size`);
    if (jar.classes !== null) {
      boundedInteger(jar.classes, `${label}[${index}].classes`);
    }
  }
  return value;
}

function validateBuildMetrics(value, label) {
  if (value == null) {
    return null;
  }
  exactKeys(value, ['tests', 'skipped', 'durationSeconds'], label);
  boundedInteger(value.tests, `${label}.tests`);
  boundedInteger(value.skipped, `${label}.skipped`);
  if (value.durationSeconds !== null) {
    boundedInteger(value.durationSeconds, `${label}.durationSeconds`);
  }
  return value;
}

function validatePatchCoverage(value) {
  if (value == null) {
    return null;
  }
  exactKeys(value, ['covered', 'missed'], 'metrics.patchCoverage');
  boundedInteger(value.covered, 'metrics.patchCoverage.covered');
  boundedInteger(value.missed, 'metrics.patchCoverage.missed');
  return value;
}

function validateApiSurface(value) {
  if (value == null) {
    return null;
  }
  exactKeys(value, ['added', 'removed'], 'metrics.apiSurface');
  for (const [name, declarations] of [['added', value.added], ['removed', value.removed]]) {
    if (!Array.isArray(declarations)) {
      throw new Error(`metrics.apiSurface.${name} must be an array`);
    }
    if (declarations.length > MAX_DECLARATIONS) {
      throw new Error(`metrics.apiSurface.${name} has too many entries`);
    }
    for (const [index, declaration] of declarations.entries()) {
      boundedDeclaration(declaration, `metrics.apiSurface.${name}[${index}]`);
    }
  }
  return value;
}

function validateProvenance(value) {
  exactKeys(value, [
    'repository',
    'workflow',
    'event',
    'runId',
    'runAttempt',
    'pullRequest',
    'baseRepository',
    'baseRef',
    'baseSha',
    'headRepository',
    'headRef',
    'headSha',
  ], 'provenance');
  boundedString(value.repository, REPOSITORY_PATTERN, 'provenance.repository');
  if (value.workflow !== WORKFLOW_NAME) {
    throw new Error(`provenance.workflow must be ${WORKFLOW_NAME}`);
  }
  if (value.event !== 'pull_request') {
    throw new Error('provenance.event must be pull_request');
  }
  boundedInteger(value.runId, 'provenance.runId', 1, Number.MAX_SAFE_INTEGER);
  boundedInteger(value.runAttempt, 'provenance.runAttempt', 1, 1000);
  boundedInteger(value.pullRequest, 'provenance.pullRequest', 1, Number.MAX_SAFE_INTEGER);
  boundedString(value.baseRepository, REPOSITORY_PATTERN, 'provenance.baseRepository');
  boundedRef(value.baseRef, 'provenance.baseRef');
  boundedString(value.baseSha, SHA_PATTERN, 'provenance.baseSha');
  boundedString(value.headRepository, REPOSITORY_PATTERN, 'provenance.headRepository');
  boundedRef(value.headRef, 'provenance.headRef');
  boundedString(value.headSha, SHA_PATTERN, 'provenance.headSha');
  return value;
}

function validateMetricsResult(value) {
  exactKeys(value, ['schemaVersion', 'provenance', 'metrics'], 'metrics result');
  if (value.schemaVersion !== SCHEMA_VERSION) {
    throw new Error(`Unsupported metrics result schema: ${value.schemaVersion}`);
  }
  validateProvenance(value.provenance);
  exactKeys(value.metrics, [
    'headCoverage',
    'baseCoverage',
    'headJars',
    'baseJars',
    'headMetrics',
    'baseMetrics',
    'patchCoverage',
    'apiSurface',
  ], 'metrics');
  validateCoverage(value.metrics.headCoverage, 'metrics.headCoverage');
  validateCoverage(value.metrics.baseCoverage, 'metrics.baseCoverage');
  validateJars(value.metrics.headJars, 'metrics.headJars');
  validateJars(value.metrics.baseJars, 'metrics.baseJars');
  validateBuildMetrics(value.metrics.headMetrics, 'metrics.headMetrics');
  validateBuildMetrics(value.metrics.baseMetrics, 'metrics.baseMetrics');
  validatePatchCoverage(value.metrics.patchCoverage);
  validateApiSurface(value.metrics.apiSurface);
  return value;
}

module.exports = { validateMetricsResult, validateProvenance };
