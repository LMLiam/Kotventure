import {
  isObject,
  MAX_RESULT_BYTES,
  SCHEMA_VERSION,
  WORKFLOW_NAME,
} from './metrics-result-contract.js';
import { validateMetricsResult } from './metrics-result-validation.js';
import type { BuildMetrics, CoverageValue, JarValue, MetricsResultValue } from './metrics-result-validation.js';
import type { CoverageData } from './coverage.js';
import type { JarInfo } from './jars.js';
import type { ApiSurface } from './api-surface.js';
import type { PatchCoverage } from './patch-coverage.js';
import type { ActionContext } from '../../../scripts/shared/action-context.js';

function readRunNumber(value: string | undefined, label: string): number {
  const parsed = Number(value);
  if (!Number.isSafeInteger(parsed) || parsed < 1) throw new Error(`${label} must be a positive integer`);
  return parsed;
}

function serializeCoverage(coverage: CoverageData | null): CoverageValue | null {
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

function serializeJars(jars: Map<string, JarInfo>): JarValue[] {
  return [...jars.entries()].map(([module, jar]) => ({
    module,
    size: jar.size,
    classes: jar.classes,
  }));
}

function serializeBuildMetrics(metrics: BuildMetrics | null): BuildMetrics | null {
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

type PullRequestBase = {
  readonly repo: { readonly full_name: string };
  readonly ref: string;
  readonly sha: string;
};

type PullRequestPayload = {
  readonly number: number;
  readonly base: PullRequestBase;
  readonly head: PullRequestBase;
};

function requirePullRequest(context: ActionContext['context']): PullRequestPayload {
  const pullRequest = context.payload?.pull_request;
  if (!isObject(pullRequest)
    || !isObject(pullRequest.base)
    || !isObject(pullRequest.base.repo)
    || !isObject(pullRequest.head)
    || !isObject(pullRequest.head.repo)) {
    throw new Error('serializeMetricsResult requires a pull_request payload with base and head repositories');
  }
  return pullRequest as PullRequestPayload;
}

export interface SerializeMetricsResultOptions {
  context: ActionContext['context'];
  runId: string | undefined;
  runAttempt: string | undefined;
  headCoverage: CoverageData | null;
  baseCoverage: CoverageData | null;
  headJars: Map<string, JarInfo>;
  baseJars: Map<string, JarInfo>;
  headMetrics: BuildMetrics | null;
  baseMetrics: BuildMetrics | null;
  patchCoverage: PatchCoverage | null;
  apiSurface: ApiSurface | null;
}

export function serializeMetricsResult({
  context,
  runId,
  runAttempt,
  headCoverage,
  baseCoverage,
  headJars,
  baseJars,
  headMetrics,
  baseMetrics,
  patchCoverage,
  apiSurface,
}: SerializeMetricsResultOptions): MetricsResultValue {
  const pullRequest = requirePullRequest(context);
  const result: MetricsResultValue = {
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
