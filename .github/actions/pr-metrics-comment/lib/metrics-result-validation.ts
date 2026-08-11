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

export interface CoverageModuleValue {
  readonly name: string;
  readonly missed: number;
  readonly covered: number;
}

export interface CoverageValue {
  readonly modules: CoverageModuleValue[];
  readonly totalMissed: number;
  readonly totalCovered: number;
}

export interface JarValue {
  readonly module: string;
  readonly size: number;
  readonly classes: number | null;
}

export interface BuildMetrics {
  readonly tests: number;
  readonly skipped: number;
  readonly durationSeconds: number | null;
}

export interface PatchCoverageValue {
  readonly covered: number;
  readonly missed: number;
}

export interface ApiSurfaceValue {
  readonly added: string[];
  readonly removed: string[];
}

export interface ProvenanceValue {
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
}

export interface MetricsResultValue {
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
}

export function validateCoverage(value: unknown, label: string): CoverageValue | null {
  if (value == null) return null;
  exactKeys(value, ['modules', 'totalMissed', 'totalCovered'], label);
  if (!Array.isArray(value.modules)) throw new Error(`${label}.modules must be an array`);
  if (value.modules.length > MAX_MODULES) throw new Error(`${label}.modules has too many entries`);
  const names = new Set<string>();
  for (const [index, module] of value.modules.entries()) {
    exactKeys(module, ['name', 'missed', 'covered'], `${label}.modules[${index}]`);
    const moduleName = boundedString(module.name, MODULE_PATTERN, `${label}.modules[${index}].name`);
    if (names.has(moduleName)) throw new Error(`${label}.modules contains a duplicate name`);
    names.add(moduleName);
    boundedInteger(module.missed, `${label}.modules[${index}].missed`);
    boundedInteger(module.covered, `${label}.modules[${index}].covered`);
  }
  boundedInteger(value.totalMissed, `${label}.totalMissed`);
  boundedInteger(value.totalCovered, `${label}.totalCovered`);
  return value as unknown as CoverageValue;
}

export function validateJars(value: unknown, label: string): JarValue[] {
  if (!Array.isArray(value)) throw new Error(`${label} must be an array`);
  if (value.length > MAX_MODULES) throw new Error(`${label} has too many entries`);
  const names = new Set<string>();
  for (const [index, jar] of value.entries()) {
    exactKeys(jar, ['module', 'size', 'classes'], `${label}[${index}]`);
    const jarModule = boundedString(jar.module, MODULE_PATTERN, `${label}[${index}].module`);
    if (names.has(jarModule)) throw new Error(`${label} contains a duplicate module`);
    names.add(jarModule);
    boundedInteger(jar.size, `${label}[${index}].size`);
    if (jar.classes !== null) boundedInteger(jar.classes, `${label}[${index}].classes`);
  }
  return value as unknown as JarValue[];
}

export function validateBuildMetrics(value: unknown, label: string): BuildMetrics | null {
  if (value == null) return null;
  exactKeys(value, ['tests', 'skipped', 'durationSeconds'], label);
  boundedInteger(value.tests, `${label}.tests`);
  boundedInteger(value.skipped, `${label}.skipped`);
  if (value.durationSeconds !== null) boundedInteger(value.durationSeconds, `${label}.durationSeconds`);
  return value as unknown as BuildMetrics;
}

export function validatePatchCoverage(value: unknown): PatchCoverageValue | null {
  if (value == null) return null;
  exactKeys(value, ['covered', 'missed'], 'metrics.patchCoverage');
  boundedInteger(value.covered, 'metrics.patchCoverage.covered');
  boundedInteger(value.missed, 'metrics.patchCoverage.missed');
  return value as unknown as PatchCoverageValue;
}

export function validateApiSurface(value: unknown): ApiSurfaceValue | null {
  if (value == null) return null;
  exactKeys(value, ['added', 'removed'], 'metrics.apiSurface');
  const sections: ReadonlyArray<readonly [string, unknown]> = [
    ['added', value.added],
    ['removed', value.removed],
  ];
  for (const [name, declarations] of sections) {
    if (!Array.isArray(declarations)) throw new Error(`metrics.apiSurface.${name} must be an array`);
    if (declarations.length > MAX_DECLARATIONS) throw new Error(`metrics.apiSurface.${name} has too many entries`);
    for (const [index, declaration] of declarations.entries()) {
      boundedDeclaration(declaration, `metrics.apiSurface.${name}[${index}]`);
    }
  }
  return value as unknown as ApiSurfaceValue;
}

export function validateProvenance(value: unknown): ProvenanceValue {
  exactKeys(value, [
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
  boundedString(value.repository, REPOSITORY_PATTERN, 'provenance.repository');
  if (value.workflow !== WORKFLOW_NAME) throw new Error(`provenance.workflow must be ${WORKFLOW_NAME}`);
  if (value.event !== 'pull_request') throw new Error('provenance.event must be pull_request');
  boundedInteger(value.runId, 'provenance.runId', 1, Number.MAX_SAFE_INTEGER);
  boundedInteger(value.runAttempt, 'provenance.runAttempt', 1, 1000);
  boundedInteger(value.pullRequest, 'provenance.pullRequest', 1, Number.MAX_SAFE_INTEGER);
  boundedString(value.baseRepository, REPOSITORY_PATTERN, 'provenance.baseRepository');
  boundedRef(value.baseRef, 'provenance.baseRef');
  boundedString(value.baseSha, SHA_PATTERN, 'provenance.baseSha');
  boundedString(value.headRepository, REPOSITORY_PATTERN, 'provenance.headRepository');
  boundedRef(value.headRef, 'provenance.headRef');
  boundedString(value.headSha, SHA_PATTERN, 'provenance.headSha');
  return value as unknown as ProvenanceValue;
}

export function validateMetricsResult(value: unknown): MetricsResultValue {
  exactKeys(value, ['schemaVersion', 'provenance', 'metrics'], 'metrics result');
  if (value.schemaVersion !== SCHEMA_VERSION) {
    throw new Error(`Unsupported metrics result schema: ${value.schemaVersion}`);
  }
  validateProvenance(value.provenance);
  exactKeys(value.metrics, [
    'headCoverage',
    'baseCoverage',
    'headJars',
    'baseJars',
    'headMetrics',
    'baseMetrics',
    'patchCoverage',
    'apiSurface',
  ], 'metrics');
  validateCoverage(value.metrics.headCoverage, 'metrics.headCoverage');
  validateCoverage(value.metrics.baseCoverage, 'metrics.baseCoverage');
  validateJars(value.metrics.headJars, 'metrics.headJars');
  validateJars(value.metrics.baseJars, 'metrics.baseJars');
  validateBuildMetrics(value.metrics.headMetrics, 'metrics.headMetrics');
  validateBuildMetrics(value.metrics.baseMetrics, 'metrics.baseMetrics');
  validatePatchCoverage(value.metrics.patchCoverage);
  validateApiSurface(value.metrics.apiSurface);
  return value as unknown as MetricsResultValue;
}
