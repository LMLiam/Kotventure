import { createValidators } from './shared/validation.js';

export const CODEQL_WORKFLOW_NAME = 'CodeQL';
export const CODEQL_WORKFLOW_PATH = '.github/workflows/codeql.yml';
export const CODEQL_ARTIFACT_PREFIX = 'codeql-sarif-';
export const CODEQL_SARIF_FILE_NAME = 'codeql.sarif';
export const CODEQL_CATEGORIES = ['actions', 'java-kotlin'] as const;
export const MAX_CODEQL_ARTIFACT_BYTES = 16 * 1024 * 1024;
export const MAX_CODEQL_SARIF_BYTES = 16 * 1024 * 1024;
export const MAX_CODEQL_RESULTS = 50_000;
export const MAX_CODEQL_ARTIFACTS = 100;

export type CodeqlCategory = typeof CODEQL_CATEGORIES[number];

const { requireBoundedInteger, requireSha } = createValidators((message: string): never => {
  throw new Error(message);
});

export interface CodeqlArtifactNameOptions {
  category: CodeqlCategory;
  workflowId: number;
  runId: number;
  runAttempt: number;
  headSha: string;
}

export interface ParsedCodeqlArtifactName {
  category: CodeqlCategory;
  workflowId: number;
  runId: number;
  runAttempt: number;
  headSha: string;
}

export function buildCodeqlArtifactName({ category, workflowId, runId, runAttempt, headSha }: CodeqlArtifactNameOptions): string {
  if (!CODEQL_CATEGORIES.includes(category)) throw new Error('CodeQL category is invalid');
  return `${CODEQL_ARTIFACT_PREFIX}${category}-${requireBoundedInteger(workflowId, 'CodeQL workflow id')}-${requireBoundedInteger(runId, 'CodeQL workflow run id')}-${requireBoundedInteger(runAttempt, 'CodeQL workflow run attempt')}-${requireSha(headSha, 'CodeQL analysed SHA')}`;
}

export function parseCodeqlArtifactName(name: string): ParsedCodeqlArtifactName | null {
  const match = name.match(new RegExp(`^${CODEQL_ARTIFACT_PREFIX}(actions|java-kotlin)-(\\d+)-(\\d+)-(\\d+)-([0-9a-f]{40})$`));
  if (!match) return null;
  const category = match[1];
  const workflowId = Number(match[2]);
  const runId = Number(match[3]);
  const runAttempt = Number(match[4]);
  const headSha = match[5];
  if (category !== 'actions' && category !== 'java-kotlin') return null;
  if (!Number.isSafeInteger(workflowId) || workflowId < 1
    || !Number.isSafeInteger(runId) || runId < 1
    || !Number.isSafeInteger(runAttempt) || runAttempt < 1) return null;
  if (headSha == null || !/^[0-9a-f]{40}$/.test(headSha)) return null;
  return { category, workflowId, runId, runAttempt, headSha };
}
