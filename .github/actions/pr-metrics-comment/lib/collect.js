'use strict';

const fs = require('fs');
const { parseCoverage } = require('./coverage.js');
const { collectJars } = require('./jars.js');
const { parsePatches } = require('./patch.js');
const { computePatchCoverage } = require('./patch-coverage.js');
const { computeApiSurface } = require('./api-surface.js');
const { serializeMetricsResult } = require('./metrics-result.js');

function readCoverage(reportPath) {
  if (!reportPath || !fs.existsSync(reportPath)) {
    return null;
  }
  return parseCoverage(fs.readFileSync(reportPath, 'utf8'));
}

function readMetrics(metricsPath) {
  if (!metricsPath || !fs.existsSync(metricsPath)) {
    return null;
  }
  try {
    const parsed = JSON.parse(fs.readFileSync(metricsPath, 'utf8'));
    return Number.isFinite(parsed.tests) ? parsed : null;
  } catch {
    return null;
  }
}

async function fetchPatches({ github, context, core }) {
  try {
    const files = await github.paginate(github.rest.pulls.listFiles, {
      owner: context.repo.owner,
      repo: context.repo.repo,
      pull_number: context.issue.number,
      per_page: 100,
    });
    return parsePatches(files);
  } catch (error) {
    core.info(`Could not list PR files: ${error.message}`);
    return null;
  }
}

async function collect({ env, context, github, core }) {
  const outputPath = env.OUTPUT_PATH;
  if (!outputPath) {
    throw new Error('OUTPUT_PATH is required');
  }

  const headCoverage = readCoverage(env.HEAD_COVERAGE_PATH);
  const baseCoverage = readCoverage(env.BASE_COVERAGE_PATH);
  const headJars = collectJars(env.HEAD_LIBS_DIR);
  const baseJars = collectJars(env.BASE_LIBS_DIR);

  if (!headCoverage && headJars.size === 0) {
    core.warning('No head coverage report or JARs found; skipping metrics result');
    return null;
  }

  const patches = await fetchPatches({ github, context, core });
  return serializeMetricsResult({
    context,
    runId: env.RUN_ID,
    runAttempt: env.RUN_ATTEMPT,
    headCoverage,
    baseCoverage,
    headJars,
    baseJars,
    headMetrics: readMetrics(env.HEAD_METRICS_PATH),
    baseMetrics: readMetrics(env.BASE_METRICS_PATH),
    patchCoverage: patches && headCoverage ? computePatchCoverage(patches, headCoverage.files) : null,
    apiSurface: patches ? computeApiSurface(patches) : null,
  });
}

module.exports = { collect };
