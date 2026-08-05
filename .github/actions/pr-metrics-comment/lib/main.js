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

module.exports = async function run({ github, context, core }) {
  const outputPath = process.env.OUTPUT_PATH;
  if (!outputPath) {
    throw new Error('OUTPUT_PATH is required');
  }

  const headCoverage = readCoverage(process.env.HEAD_COVERAGE_PATH);
  const baseCoverage = readCoverage(process.env.BASE_COVERAGE_PATH);
  const headJars = collectJars(process.env.HEAD_LIBS_DIR);
  const baseJars = collectJars(process.env.BASE_LIBS_DIR);

  if (!headCoverage && headJars.size === 0) {
    core.warning('No head coverage report or JARs found; skipping metrics result');
    return;
  }

  const patches = await fetchPatches({ github, context, core });
  const result = serializeMetricsResult({
    context,
    runId: process.env.RUN_ID,
    runAttempt: process.env.RUN_ATTEMPT,
    headCoverage,
    baseCoverage,
    headJars,
    baseJars,
    headMetrics: readMetrics(process.env.HEAD_METRICS_PATH),
    baseMetrics: readMetrics(process.env.BASE_METRICS_PATH),
    patchCoverage: patches && headCoverage ? computePatchCoverage(patches, headCoverage.files) : null,
    apiSurface: patches ? computeApiSurface(patches) : null,
  });
  fs.writeFileSync(outputPath, JSON.stringify(result), 'utf8');
  core.info(`Wrote PR metrics result to ${outputPath}`);
};
