import { createValidators } from './shared/validation.js';

export const CI_WORKFLOW_NAME = 'CI';
export const CI_WORKFLOW_PATH = '.github/workflows/ci.yml';
export const JUNIT_ARTIFACT_PREFIX = 'junit-results-';
export const JUNIT_BUILD_SHARDS = ['core', 'text', 'runtime'] as const;
export const JUNIT_REPORT_NAMES = {
  build: 'Test Report',
  vanilla: 'Vanilla Conformance Report',
} as const;

export type JunitReportKind = keyof typeof JUNIT_REPORT_NAMES;
export type JunitBuildShard = typeof JUNIT_BUILD_SHARDS[number];
export type JunitArtifactShard = JunitBuildShard | 'vanilla';

export const MAX_JUNIT_ARTIFACT_BYTES = 16 * 1024 * 1024;
export const MAX_JUNIT_ENTRY_BYTES = 4 * 1024 * 1024;
export const MAX_JUNIT_TOTAL_BYTES = 12 * 1024 * 1024;
export const MAX_JUNIT_FILES = 200;
export const MAX_JUNIT_TEST_CASES = 100_000;
export const MAX_JUNIT_ANNOTATIONS = 50;
export const MAX_JUNIT_TEXT = 4_096;

const { requireBoundedInteger, requireSha } = createValidators((message: string): never => {
  throw new Error(message);
});

export interface JunitArtifactNameOptions {
  kind: JunitReportKind;
  shard: JunitArtifactShard;
  workflowId: number;
  runId: number;
  runAttempt: number;
  headSha: string;
}

export interface ParsedJunitArtifactName {
  kind: JunitReportKind;
  shard: JunitArtifactShard;
  workflowId: number;
  runId: number;
  runAttempt: number;
  headSha: string;
}

export function buildJunitArtifactName({
  kind,
  shard,
  workflowId,
  runId,
  runAttempt,
  headSha,
}: JunitArtifactNameOptions): string {
  if (!(kind in JUNIT_REPORT_NAMES)) throw new Error('JUnit report kind is invalid');
  if (kind === 'build' && !JUNIT_BUILD_SHARDS.includes(shard as JunitBuildShard)) {
    throw new Error('JUnit build shard is invalid');
  }
  if (kind === 'vanilla' && shard !== 'vanilla') throw new Error('JUnit Vanilla shard is invalid');
  return `${JUNIT_ARTIFACT_PREFIX}${kind}-${shard}-${requireBoundedInteger(workflowId, 'JUnit workflow id')}-${requireBoundedInteger(runId, 'JUnit workflow run id')}-${requireBoundedInteger(runAttempt, 'JUnit workflow run attempt')}-${requireSha(headSha, 'JUnit head SHA')}`;
}

export function parseJunitArtifactName(name: string): ParsedJunitArtifactName | null {
  const match = name.match(
    new RegExp(`^${JUNIT_ARTIFACT_PREFIX}(build|vanilla)-(core|text|runtime|vanilla)-(\\d+)-(\\d+)-(\\d+)-([0-9a-f]{40})$`),
  );
  if (!match) return null;
  const kind = match[1];
  const shard = match[2];
  const workflowId = Number(match[3]);
  const runId = Number(match[4]);
  const runAttempt = Number(match[5]);
  const headSha = match[6];
  if (kind !== 'build' && kind !== 'vanilla') return null;
  if (shard !== 'core' && shard !== 'text' && shard !== 'runtime' && shard !== 'vanilla') return null;
  if (kind === 'build' && shard === 'vanilla') return null;
  if (kind === 'vanilla' && shard !== 'vanilla') return null;
  if (!Number.isSafeInteger(workflowId) || workflowId < 1
    || !Number.isSafeInteger(runId) || runId < 1
    || !Number.isSafeInteger(runAttempt) || runAttempt < 1) return null;
  if (headSha == null || !/^[0-9a-f]{40}$/.test(headSha)) return null;
  return {
    kind,
    shard,
    workflowId,
    runId,
    runAttempt,
    headSha,
  };
}
