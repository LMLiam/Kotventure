import * as fs from 'node:fs';
import { parseCoverage } from './coverage.js';
import { collectJars } from './jars.js';
import { parsePatches } from './patch.js';
import { computePatchCoverage } from './patch-coverage.js';
import { computeApiSurface } from './api-surface.js';
import { serializeMetricsResult } from './metrics-result.js';
import { validateBuildMetrics } from './metrics-result-validation.js';
import type { ActionContext } from '../../../scripts/shared/action-context.js';
import type { CoverageData } from './coverage.js';
import type { ParsedPatch } from './patch.js';
import type { BuildMetrics, MetricsResultValue } from './metrics-result-validation.js';

function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}

function readCoverage(reportPath: string | undefined): CoverageData | null {
  if (!reportPath || !fs.existsSync(reportPath)) return null;
  return parseCoverage(fs.readFileSync(reportPath, 'utf8'));
}

function readMetrics(metricsPath: string | undefined): BuildMetrics | null {
  if (!metricsPath || !fs.existsSync(metricsPath)) return null;
  try {
    return validateBuildMetrics(JSON.parse(fs.readFileSync(metricsPath, 'utf8')));
  } catch {
    return null;
  }
}

async function fetchPatches({ github, context, core }: ActionContext): Promise<ParsedPatch[] | null> {
  try {
    const files = await github.paginate(github.rest.pulls.listFiles, {
      owner: context.repo.owner,
      repo: context.repo.repo,
      pull_number: context.issue.number,
      per_page: 100,
    });
    return parsePatches(files);
  } catch (error) {
    core.info(`Could not list PR files: ${errorMessage(error)}`);
    return null;
  }
}

export interface CollectOptions extends ActionContext {
  env: NodeJS.ProcessEnv;
}

export async function collect({ env, context, github, core }: CollectOptions): Promise<MetricsResultValue | null> {
  const outputPath = env.OUTPUT_PATH;
  if (!outputPath) throw new Error('OUTPUT_PATH is required');

  const headCoverage = readCoverage(env.HEAD_COVERAGE_PATH);
  const baseCoverage = readCoverage(env.BASE_COVERAGE_PATH);
  const [headJars, baseJars] = await Promise.all([
    collectJars(env.HEAD_LIBS_DIR ?? ''),
    collectJars(env.BASE_LIBS_DIR ?? ''),
  ]);

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
