'use strict';

const {
  MAX_RESULT_BYTES,
  SCHEMA_VERSION,
  WORKFLOW_NAME,
} = require('./metrics-result-contract.js');
const { validateMetricsResult } = require('./metrics-result-validation.js');

function readRunNumber(value, label) {
  const parsed = Number(value);
  if (!Number.isSafeInteger(parsed) || parsed < 1) {
    throw new Error(`${label} must be a positive integer`);
  }
  return parsed;
}

function serializeCoverage(coverage) {
  if (!coverage) return null;
  return {
    modules: [...coverage.modules.entries()].map(([name, counters]) => ({
      name,
      missed: counters.missed,
      covered: counters.covered,
    })),
    totalMissed: coverage.totalMissed,
    totalCovered: coverage.totalCovered,
  };
}

function serializeJars(jars) {
  return [...jars.entries()].map(([module, jar]) => ({
    module,
    size: jar.size,
    classes: jar.classes,
  }));
}

function serializeBuildMetrics(metrics) {
  if (!metrics
    || !Number.isSafeInteger(metrics.tests)
    || !Number.isSafeInteger(metrics.skipped)
    || (metrics.durationSeconds !== null && !Number.isSafeInteger(metrics.durationSeconds))) {
    return null;
  }
  return {
    tests: metrics.tests,
    skipped: metrics.skipped,
    durationSeconds: metrics.durationSeconds,
  };
}

function requirePullRequest(context) {
  const pullRequest = context?.payload?.pull_request;
  const hasRepository = (value) => value
    && typeof value === 'object'
    && !Array.isArray(value);
  if (!hasRepository(pullRequest)
    || !hasRepository(pullRequest.base)
    || !hasRepository(pullRequest.base.repo)
    || !hasRepository(pullRequest.head)
    || !hasRepository(pullRequest.head.repo)) {
    throw new Error('serializeMetricsResult requires a pull_request payload with base and head repositories');
  }
  return pullRequest;
}

function serializeMetricsResult({ context, runId, runAttempt, headCoverage, baseCoverage, headJars, baseJars,
  headMetrics, baseMetrics, patchCoverage, apiSurface }) {
  const pullRequest = requirePullRequest(context);
  const result = {
    schemaVersion: SCHEMA_VERSION,
    provenance: {
      repository: context.repo.owner + '/' + context.repo.repo,
      workflow: WORKFLOW_NAME,
      event: context.eventName,
      runId: readRunNumber(runId, 'runId'),
      runAttempt: readRunNumber(runAttempt, 'runAttempt'),
      pullRequest: pullRequest.number,
      baseRepository: pullRequest.base.repo.full_name,
      baseRef: pullRequest.base.ref,
      baseSha: pullRequest.base.sha,
      headRepository: pullRequest.head.repo.full_name,
      headRef: pullRequest.head.ref,
      headSha: pullRequest.head.sha,
    },
    metrics: {
      headCoverage: serializeCoverage(headCoverage),
      baseCoverage: serializeCoverage(baseCoverage),
      headJars: serializeJars(headJars),
      baseJars: serializeJars(baseJars),
      headMetrics: serializeBuildMetrics(headMetrics),
      baseMetrics: serializeBuildMetrics(baseMetrics),
      patchCoverage: patchCoverage
        ? { covered: patchCoverage.covered, missed: patchCoverage.missed }
        : null,
      apiSurface: apiSurface
        ? { added: apiSurface.added, removed: apiSurface.removed }
        : null,
    },
  };
  validateMetricsResult(result);
  if (Buffer.byteLength(JSON.stringify(result), 'utf8') > MAX_RESULT_BYTES) {
    throw new Error(`Metrics result exceeds ${MAX_RESULT_BYTES} bytes`);
  }
  return result;
}

module.exports = { serializeMetricsResult };
