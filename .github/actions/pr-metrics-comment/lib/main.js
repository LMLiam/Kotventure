'use strict';

const fs = require('fs');
const { parseCoverage } = require('./coverage.js');
const { collectJars } = require('./jars.js');
const { buildReport } = require('./report.js');
const { upsertComment } = require('./comment.js');

function readCoverage(reportPath) {
  if (!reportPath || !fs.existsSync(reportPath)) {
    return null;
  }
  return parseCoverage(fs.readFileSync(reportPath, 'utf8'));
}

function resolveThreshold(raw) {
  const parsed = Number.parseInt(raw, 10);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 10;
}

module.exports = async function run({ github, context, core }) {
  const headCoverage = readCoverage(process.env.HEAD_COVERAGE_PATH);
  const baseCoverage = readCoverage(process.env.BASE_COVERAGE_PATH);
  const headJars = collectJars(process.env.HEAD_LIBS_DIR);
  const baseJars = collectJars(process.env.BASE_LIBS_DIR);

  if (!headCoverage && headJars.size === 0) {
    core.warning('No head coverage report or JARs found; skipping metrics comment');
    return;
  }

  const growthThreshold = resolveThreshold(process.env.THRESHOLD);
  const baseLabel = (process.env.BASE_LABEL || 'PR base').replace(/[\r\n|`]/g, '');

  const { body, warnings } = buildReport({
    headCoverage,
    baseCoverage,
    headJars,
    baseJars,
    growthThreshold,
    baseLabel,
  });

  if (warnings.length > 0) {
    core.warning(`Artifact size growth exceeds ${growthThreshold}%: ${warnings.join('; ')}`);
  }

  await upsertComment({ github, context, body });
  core.info('PR metrics comment posted');
};
