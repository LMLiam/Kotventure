'use strict';

const { validateMetricsResult } = require('./metrics-result-validation.js');

function deserializeCoverage(coverage) {
  if (!coverage) return null;
  return {
    modules: new Map(coverage.modules.map(({ name, missed, covered }) => [name, { missed, covered }])),
    totalMissed: coverage.totalMissed,
    totalCovered: coverage.totalCovered,
    files: new Map(),
  };
}

function deserializeJars(jars) {
  return new Map(jars.map(({ module, size, classes }) => [module, { size, classes }]));
}

function deserializeMetricsResult(result) {
  validateMetricsResult(result);
  return {
    headCoverage: deserializeCoverage(result.metrics.headCoverage),
    baseCoverage: deserializeCoverage(result.metrics.baseCoverage),
    headJars: deserializeJars(result.metrics.headJars),
    baseJars: deserializeJars(result.metrics.baseJars),
    headMetrics: result.metrics.headMetrics,
    baseMetrics: result.metrics.baseMetrics,
    patchCoverage: result.metrics.patchCoverage
      ? { ...result.metrics.patchCoverage, uncovered: [] }
      : null,
    apiSurface: result.metrics.apiSurface,
  };
}

module.exports = { deserializeMetricsResult };
