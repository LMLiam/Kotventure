import { validateMetricsResult } from './metrics-result-validation.js';
import type { JsonValue } from './metrics-result-contract.js';
import type { BuildMetrics, CoverageValue, JarValue, MetricsResultValue } from './metrics-result-validation.js';
import type { CoverageData, ModuleCounters } from './coverage.js';
import type { JarInfo } from './jars.js';
import type { ApiSurface } from './api-surface.js';
import type { PatchCoverage } from './patch-coverage.js';

export interface DeserializedMetricsResult {
  readonly headCoverage: CoverageData | null;
  readonly baseCoverage: CoverageData | null;
  readonly headJars: Map<string, JarInfo>;
  readonly baseJars: Map<string, JarInfo>;
  readonly headMetrics: BuildMetrics | null;
  readonly baseMetrics: BuildMetrics | null;
  readonly patchCoverage: PatchCoverage | null;
  readonly apiSurface: ApiSurface | null;
}

function deserializeCoverage(coverage: CoverageValue | null): CoverageData | null {
  if (!coverage) return null;
  return {
    modules: new Map<string, ModuleCounters>(
      coverage.modules.map(({ name, missed, covered }) => [name, { missed, covered }]),
    ),
    totalMissed: coverage.totalMissed,
    totalCovered: coverage.totalCovered,
    files: new Map(),
  };
}

function deserializeJars(jars: JarValue[]): Map<string, JarInfo> {
  return new Map(jars.map(({ module, size, classes }) => [module, { size, classes }]));
}

export function deserializeMetricsResult(result: JsonValue): DeserializedMetricsResult {
  const validated = validateMetricsResult(result);
  return {
    headCoverage: deserializeCoverage(validated.metrics.headCoverage),
    baseCoverage: deserializeCoverage(validated.metrics.baseCoverage),
    headJars: deserializeJars(validated.metrics.headJars),
    baseJars: deserializeJars(validated.metrics.baseJars),
    headMetrics: validated.metrics.headMetrics,
    baseMetrics: validated.metrics.baseMetrics,
    patchCoverage: validated.metrics.patchCoverage
      ? { ...validated.metrics.patchCoverage, uncovered: [] }
      : null,
    apiSurface: validated.metrics.apiSurface,
  };
}
