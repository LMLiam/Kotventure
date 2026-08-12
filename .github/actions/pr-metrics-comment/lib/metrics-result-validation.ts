import { z } from 'zod';
import {
  MAX_COUNT,
  MAX_DECLARATIONS,
  MAX_DECLARATION_LENGTH,
  MAX_MODULES,
  MAX_REF_LENGTH,
  MODULE_PATTERN,
  REPOSITORY_PATTERN,
  SCHEMA_VERSION,
  SHA_PATTERN,
  WORKFLOW_NAME,
  hasUnsafeTextCharacter,
} from './metrics-result-contract.js';
import type { JsonValue } from './metrics-result-contract.js';

/**
 * Parses `value` against `schema` and returns the original input reference on
 * success, never the parsed output. Reference identity is a contract: the
 * publisher asserts `validateMetricsResult(result) === result` in
 * `pr-metrics-publisher.test.js`. This is sound while every schema is a plain
 * shape and bounds check with no `.transform()`, `.default()`, `.prefault()`,
 * or `.catch()` — the parsed output is structurally identical to the input. A
 * schema that converts values must switch this function to `result.data`.
 */
function parseOrThrow<T>(schema: z.ZodType<T>, value: unknown): T {
  const result = schema.safeParse(value, {
    error: (issue) => {
      switch (issue.code) {
        case 'unrecognized_keys':
          return 'has unexpected properties';
        case 'invalid_type':
          if (issue.expected === 'array') return 'must be an array';
          if (issue.expected === 'int') return 'must be an integer';
          if (issue.expected === 'object') return 'must be an object';
          if (issue.expected === 'string') return 'must be a string';
          return issue.message;
        case 'too_small':
        case 'too_big':
          if (issue.origin === 'array') return 'has too many entries';
          if (issue.origin === 'string') return 'has an invalid value';
          return issue.message;
        case 'invalid_value':
        case 'invalid_format':
          return 'has an invalid value';
        default:
          return issue.message;
      }
    },
  });
  if (!result.success) {
    throw new Error(result.error.issues
      .map((issue) => {
        const path = issue.path.join('.');
        return path.length > 0 ? `${path} ${issue.message}` : issue.message;
      })
      .join('; '));
  }
  return value as T;
}

/**
 * Builds an integer schema whose bound failures report the exact range in the
 * message. Schema-level error messages take precedence over the parse-level
 * map, so both `too_small` and `too_big` report the same literal.
 */
function boundedInt(minimum: number, maximum: number): z.ZodNumber {
  const message = `must be an integer from ${minimum} to ${maximum}`;
  return z.number().int()
    .min(minimum, { error: () => message })
    .max(maximum, { error: () => message });
}

const boundedCountSchema = boundedInt(0, MAX_COUNT);

const moduleNameSchema = z.string().regex(MODULE_PATTERN);

const coverageModuleSchema = z.strictObject({
  name: moduleNameSchema,
  missed: boundedCountSchema,
  covered: boundedCountSchema,
});

const coverageSchema = z.strictObject({
  modules: z.array(coverageModuleSchema).max(MAX_MODULES).refine(
    (modules): boolean => new Set(modules.map((module) => module.name)).size === modules.length,
    { message: 'contains a duplicate name' },
  ),
  totalMissed: boundedCountSchema,
  totalCovered: boundedCountSchema,
});

const jarSchema = z.strictObject({
  module: moduleNameSchema,
  size: boundedCountSchema,
  classes: boundedInt(0, MAX_COUNT).nullable(),
});

const jarsSchema = z.array(jarSchema).max(MAX_MODULES).refine(
  (jars): boolean => new Set(jars.map((jar) => jar.module)).size === jars.length,
  { message: 'contains a duplicate module' },
);

const buildMetricsSchema = z.strictObject({
  tests: boundedCountSchema,
  skipped: boundedCountSchema,
  durationSeconds: boundedInt(0, MAX_COUNT).nullable(),
});

const patchCoverageSchema = z.strictObject({
  covered: boundedCountSchema,
  missed: boundedCountSchema,
});

const declarationSchema = z.string().min(1).max(MAX_DECLARATION_LENGTH).refine(
  (value): boolean => !hasUnsafeTextCharacter(value, false),
  { message: 'has an invalid value' },
);

const apiSurfaceSchema = z.strictObject({
  added: z.array(declarationSchema).max(MAX_DECLARATIONS),
  removed: z.array(declarationSchema).max(MAX_DECLARATIONS),
});

const refSchema = z.string().min(1).max(MAX_REF_LENGTH).refine(
  (value): boolean => !hasUnsafeTextCharacter(value, true),
  { message: 'has an invalid value' },
);

const provenanceSchema = z.strictObject({
  repository: z.string().regex(REPOSITORY_PATTERN),
  workflow: z.string().refine((value): boolean => value === WORKFLOW_NAME, { message: `must be ${WORKFLOW_NAME}` }),
  event: z.string().refine((value): boolean => value === 'pull_request', { message: 'must be pull_request' }),
  runId: boundedInt(1, Number.MAX_SAFE_INTEGER),
  runAttempt: boundedInt(1, 1000),
  pullRequest: boundedInt(1, Number.MAX_SAFE_INTEGER),
  baseRepository: z.string().regex(REPOSITORY_PATTERN),
  baseRef: refSchema,
  baseSha: z.string().regex(SHA_PATTERN),
  headRepository: z.string().regex(REPOSITORY_PATTERN),
  headRef: refSchema,
  headSha: z.string().regex(SHA_PATTERN),
});

const metricsResultSchema = z.strictObject({
  schemaVersion: z.literal(SCHEMA_VERSION),
  provenance: provenanceSchema,
  metrics: z.strictObject({
    headCoverage: coverageSchema.nullable(),
    baseCoverage: coverageSchema.nullable(),
    headJars: jarsSchema,
    baseJars: jarsSchema,
    headMetrics: buildMetricsSchema.nullable(),
    baseMetrics: buildMetricsSchema.nullable(),
    patchCoverage: patchCoverageSchema.nullable(),
    apiSurface: apiSurfaceSchema.nullable(),
  }),
});

export type CoverageValue = z.infer<typeof coverageSchema>;
export type JarValue = z.infer<typeof jarSchema>;
export type BuildMetrics = z.infer<typeof buildMetricsSchema>;
export type MetricsResultValue = z.infer<typeof metricsResultSchema>;

export function validateBuildMetrics(value: JsonValue): BuildMetrics | null {
  if (value == null) return null;
  return parseOrThrow(buildMetricsSchema, value);
}

export function validateMetricsResult(value: JsonValue): MetricsResultValue {
  return parseOrThrow(metricsResultSchema, value);
}
