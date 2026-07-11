'use strict';

const fs = require('fs');
const { parseCoverage } = require('./coverage.js');
const { collectJars } = require('./jars.js');
const { parsePatches } = require('./patch.js');
const { computePatchCoverage } = require('./patch-coverage.js');
const { computeApiSurface } = require('./api-surface.js');
const { buildReport } = require('./report.js');
const { upsertComment } = require('./comment.js');

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

function readGateThreshold(gateFile) {
  if (!gateFile || !fs.existsSync(gateFile)) {
    return null;
  }
  const match = fs.readFileSync(gateFile, 'utf8').match(/coverageLineThreshold\s*=\s*(\d+)/);
  return match ? parseInt(match[1], 10) : null;
}

function resolveThreshold(raw) {
  const parsed = Number.parseInt(raw, 10);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 10;
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

async function resolveLinks({ github, context, core }) {
  const runId = Number.parseInt(process.env.RUN_ID || '', 10);
  if (!Number.isFinite(runId)) {
    return {};
  }
  const runUrl = `${context.serverUrl}/${context.repo.owner}/${context.repo.repo}/actions/runs/${runId}`;
  const links = { run: runUrl };
  try {
    const artifacts = await github.paginate(github.rest.actions.listWorkflowRunArtifacts, {
      owner: context.repo.owner,
      repo: context.repo.repo,
      run_id: runId,
    });
    const byName = (name) => artifacts.find((artifact) => artifact.name === name);
    const dokka = byName('dokka-preview');
    if (dokka) {
      links.dokka = `${runUrl}/artifacts/${dokka.id}`;
    }
    const tests = byName('gradle-test-results');
    if (tests) {
      links.tests = `${runUrl}/artifacts/${tests.id}`;
    }
  } catch (error) {
    core.info(`Could not list run artifacts: ${error.message}`);
  }
  return links;
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

  const patches = await fetchPatches({ github, context, core });
  const { body, warnings } = buildReport({
    headCoverage,
    baseCoverage,
    headJars,
    baseJars,
    headMetrics: readMetrics(process.env.HEAD_METRICS_PATH),
    baseMetrics: readMetrics(process.env.BASE_METRICS_PATH),
    patchCoverage: patches && headCoverage ? computePatchCoverage(patches, headCoverage.files) : null,
    apiSurface: patches ? computeApiSurface(patches) : null,
    gateThreshold: readGateThreshold(process.env.GATE_FILE),
    growthThreshold: resolveThreshold(process.env.THRESHOLD),
    baseLabel: (process.env.BASE_LABEL || 'PR base').replace(/[\r\n|`]/g, ''),
    headSha: (process.env.HEAD_SHA || '').slice(0, 7) || null,
    links: await resolveLinks({ github, context, core }),
  });

  for (const warning of warnings) {
    core.warning(warning);
  }
  await upsertComment({ github, context, body });
  core.info('PR metrics comment posted');
};
