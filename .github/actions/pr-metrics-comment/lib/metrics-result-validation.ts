import {
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
} from './metrics-result-contract.js';
import type { JsonValue } from './metrics-result-contract.js';

export type CoverageModuleValue = {
  readonly name: string;
  readonly missed: number;
  readonly covered: number;
};

export type CoverageValue = {
  readonly modules: CoverageModuleValue[];
  readonly totalMissed: number;
  readonly totalCovered: number;
};

export type JarValue = {
  readonly module: string;
  readonly size: number;
  readonly classes: number | null;
};

export type BuildMetrics = {
  readonly tests: number;
  readonly skipped: number;
  readonly durationSeconds: number | null;
};

export type PatchCoverageValue = {
  readonly covered: number;
  readonly missed: number;
};

export type ApiSurfaceValue = {
  readonly added: string[];
  readonly removed: string[];
};

export type ProvenanceValue = {
  readonly repository: string;
  readonly workflow: string;
  readonly event: string;
  readonly runId: number;
  readonly runAttempt: number;
  readonly pullRequest: number;
  readonly baseRepository: string;
  readonly baseRef: string;
  readonly baseSha: string;
  readonly headRepository: string;
  readonly headRef: string;
  readonly headSha: string;
};

export type MetricsResultValue = {
  readonly schemaVersion: number;
  readonly provenance: ProvenanceValue;
  readonly metrics: {
    readonly headCoverage: CoverageValue | null;
    readonly baseCoverage: CoverageValue | null;
    readonly headJars: JarValue[];
    readonly baseJars: JarValue[];
    readonly headMetrics: BuildMetrics | null;
    readonly baseMetrics: BuildMetrics | null;
    readonly patchCoverage: PatchCoverageValue | null;
    readonly apiSurface: ApiSurfaceValue | null;
  };
};

export function validateCoverage(value: JsonValue, label: string): CoverageValue | null {
  if (value == null) return null;
  const checked = exactKeys<CoverageValue>(value, ['modules', 'totalMissed', 'totalCovered'], label);
  if (!Array.isArray(checked.modules)) throw new Error(`${label}.modules must be an array`);
  if (checked.modules.length > MAX_MODULES) throw new Error(`${label}.modules has too many entries`);
  const names = new Set<string>();
  for (const [index, module] of checked.modules.entries()) {
    const checkedModule = exactKeys<CoverageModuleValue>(module, ['name', 'missed', 'covered'], `${label}.modules[${index}]`);
    const moduleName = boundedString(checkedModule.name, MODULE_PATTERN, `${label}.modules[${index}].name`);
    if (names.has(moduleName)) throw new Error(`${label}.modules contains a duplicate name`);
    names.add(moduleName);
    boundedInteger(checkedModule.missed, `${label}.modules[${index}].missed`);
    boundedInteger(checkedModule.covered, `${label}.modules[${index}].covered`);
  }
  boundedInteger(checked.totalMissed, `${label}.totalMissed`);
  boundedInteger(checked.totalCovered, `${label}.totalCovered`);
  return checked;
}

export function validateJars(value: JsonValue, label: string): JarValue[] {
  if (!Array.isArray(value)) throw new Error(`${label} must be an array`);
  if (value.length > MAX_MODULES) throw new Error(`${label} has too many entries`);
  const names = new Set<string>();
  const jars: JarValue[] = [];
  for (const [index, jar] of value.entries()) {
    const checkedJar = exactKeys<JarValue>(jar, ['module', 'size', 'classes'], `${label}[${index}]`);
    const jarModule = boundedString(checkedJar.module, MODULE_PATTERN, `${label}[${index}].module`);
    if (names.has(jarModule)) throw new Error(`${label} contains a duplicate module`);
    names.add(jarModule);
    boundedInteger(checkedJar.size, `${label}[${index}].size`);
    if (checkedJar.classes !== null) boundedInteger(checkedJar.classes, `${label}[${index}].classes`);
    jars.push(checkedJar);
  }
  return jars;
}

export function validateBuildMetrics(value: JsonValue, label: string): BuildMetrics | null {
  if (value == null) return null;
  const checked = exactKeys<BuildMetrics>(value, ['tests', 'skipped', 'durationSeconds'], label);
  boundedInteger(checked.tests, `${label}.tests`);
  boundedInteger(checked.skipped, `${label}.skipped`);
  if (checked.durationSeconds !== null) boundedInteger(checked.durationSeconds, `${label}.durationSeconds`);
  return checked;
}

export function validatePatchCoverage(value: JsonValue): PatchCoverageValue | null {
  if (value == null) return null;
  const checked = exactKeys<PatchCoverageValue>(value, ['covered', 'missed'], 'metrics.patchCoverage');
  boundedInteger(checked.covered, 'metrics.patchCoverage.covered');
  boundedInteger(checked.missed, 'metrics.patchCoverage.missed');
  return checked;
}

export function validateApiSurface(value: JsonValue): ApiSurfaceValue | null {
  if (value == null) return null;
  const checked = exactKeys<ApiSurfaceValue>(value, ['added', 'removed'], 'metrics.apiSurface');
  for (const name of ['added', 'removed'] as const) {
    const declarations = checked[name];
    if (!Array.isArray(declarations)) throw new Error(`metrics.apiSurface.${name} must be an array`);
    if (declarations.length > MAX_DECLARATIONS) throw new Error(`metrics.apiSurface.${name} has too many entries`);
    for (const [index, declaration] of declarations.entries()) {
      boundedDeclaration(declaration, `metrics.apiSurface.${name}[${index}]`);
    }
  }
  return checked;
}

export function validateProvenance(value: JsonValue): ProvenanceValue {
  const checked = exactKeys<ProvenanceValue>(value, [
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
  boundedString(checked.repository, REPOSITORY_PATTERN, 'provenance.repository');
  if (checked.workflow !== WORKFLOW_NAME) throw new Error(`provenance.workflow must be ${WORKFLOW_NAME}`);
  if (checked.event !== 'pull_request') throw new Error('provenance.event must be pull_request');
  boundedInteger(checked.runId, 'provenance.runId', 1, Number.MAX_SAFE_INTEGER);
  boundedInteger(checked.runAttempt, 'provenance.runAttempt', 1, 1000);
  boundedInteger(checked.pullRequest, 'provenance.pullRequest', 1, Number.MAX_SAFE_INTEGER);
  boundedString(checked.baseRepository, REPOSITORY_PATTERN, 'provenance.baseRepository');
  boundedRef(checked.baseRef, 'provenance.baseRef');
  boundedString(checked.baseSha, SHA_PATTERN, 'provenance.baseSha');
  boundedString(checked.headRepository, REPOSITORY_PATTERN, 'provenance.headRepository');
  boundedRef(checked.headRef, 'provenance.headRef');
  boundedString(checked.headSha, SHA_PATTERN, 'provenance.headSha');
  return checked;
}

export function validateMetricsResult(value: JsonValue): MetricsResultValue {
  const checked = exactKeys<MetricsResultValue>(value, ['schemaVersion', 'provenance', 'metrics'], 'metrics result');
  if (checked.schemaVersion !== SCHEMA_VERSION) {
    throw new Error(`Unsupported metrics result schema: ${checked.schemaVersion}`);
  }
  validateProvenance(checked.provenance);
  const metrics = exactKeys<MetricsResultValue['metrics']>(checked.metrics, [
    'headCoverage',
    'baseCoverage',
    'headJars',
    'baseJars',
    'headMetrics',
    'baseMetrics',
    'patchCoverage',
    'apiSurface',
  ], 'metrics');
  validateCoverage(metrics.headCoverage, 'metrics.headCoverage');
  validateCoverage(metrics.baseCoverage, 'metrics.baseCoverage');
  validateJars(metrics.headJars, 'metrics.headJars');
  validateJars(metrics.baseJars, 'metrics.baseJars');
  validateBuildMetrics(metrics.headMetrics, 'metrics.headMetrics');
  validateBuildMetrics(metrics.baseMetrics, 'metrics.baseMetrics');
  validatePatchCoverage(metrics.patchCoverage);
  validateApiSurface(metrics.apiSurface);
  return checked;
}
