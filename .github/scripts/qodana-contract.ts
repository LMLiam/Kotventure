import { createValidators } from './shared/validation.js';

export {
  DOCUMENTATION_PATH_PATTERNS,
  RELEASE_ONLY_FILES,
  changedPathNames,
  classifyChangedFiles,
  isDocumentationPath,
  isSafeRepositoryPath,
} from './shared/path-classification.js';

export type QodanaSourceKind = 'code' | 'documentation' | 'release';
export type QodanaPathClassification = 'code' | 'documentation' | 'release-candidate';

export const CI_WORKFLOW_NAME = 'CI';
export const CI_WORKFLOW_PATH = '.github/workflows/ci.yml';
export const QODANA_WORKFLOW_NAME = 'Qodana';
export const QODANA_WORKFLOW_PATH = '.github/workflows/qodana.yml';
export const QODANA_ARTIFACT_PREFIX = 'qodana-sarif-';
export const QODANA_SARIF_FILE_NAME = 'qodana.sarif.json';
export const MAX_ARTIFACT_BYTES = 16 * 1024 * 1024;
export const MAX_SARIF_BYTES = 16 * 1024 * 1024;
export const MAX_SARIF_RESULTS = 50_000;

const {
  requireBoundedInteger,
  requireSha,
} = createValidators((message: string): never => {
  throw new Error(message);
});

export interface QodanaArtifactNameOptions {
  sourceKind: QodanaSourceKind;
  qodanaRunId: number;
  qodanaRunAttempt: number;
  headSha: string;
  baseSha: string;
}

export function buildArtifactName({
  sourceKind,
  qodanaRunId,
  qodanaRunAttempt,
  headSha,
  baseSha,
}: QodanaArtifactNameOptions): string {
  if (!['code', 'documentation', 'release'].includes(sourceKind)) throw new Error('Qodana source kind is invalid');
  return `${QODANA_ARTIFACT_PREFIX}${sourceKind}-${requireBoundedInteger(qodanaRunId, 'Qodana workflow run id')}-${requireBoundedInteger(qodanaRunAttempt, 'Qodana workflow run attempt')}-${requireSha(headSha, 'head SHA')}-${requireSha(baseSha, 'base SHA')}`;
}

export interface ParsedQodanaArtifactName {
  sourceKind: QodanaSourceKind;
  qodanaRunId: number;
  qodanaRunAttempt: number;
  headSha: string;
  baseSha: string;
}

export function parseArtifactName(name: string): ParsedQodanaArtifactName | null {
  const match = name.match(
    new RegExp(`^${QODANA_ARTIFACT_PREFIX}(code|documentation|release)-(\\d+)-(\\d+)-([0-9a-f]{40})-([0-9a-f]{40})$`),
  );
  if (!match) return null;
  const sourceKind = match[1];
  const qodanaRunId = Number(match[2]);
  const qodanaRunAttempt = Number(match[3]);
  const headSha = match[4];
  const baseSha = match[5];
  if (sourceKind !== 'code' && sourceKind !== 'documentation' && sourceKind !== 'release') return null;
  if (headSha == null || baseSha == null) return null;
  if (!Number.isSafeInteger(qodanaRunId) || qodanaRunId < 1
    || !Number.isSafeInteger(qodanaRunAttempt) || qodanaRunAttempt < 1) {
    return null;
  }
  return {
    sourceKind,
    qodanaRunId,
    qodanaRunAttempt,
    headSha,
    baseSha,
  };
}
