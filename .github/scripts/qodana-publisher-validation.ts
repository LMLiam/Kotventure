import {
  MAX_ARTIFACT_BYTES,
  MAX_SARIF_BYTES,
  MAX_SARIF_RESULTS,
  QODANA_ARTIFACT_PREFIX,
  QODANA_WORKFLOW_NAME,
  QODANA_WORKFLOW_PATH,
  parseArtifactName,
} from './qodana-contract.js';
import type { ParsedQodanaArtifactName } from './qodana-contract.js';
import type {
  QodanaSarifArtifact,
  QodanaSarifDocument,
  QodanaSarifLocation,
  QodanaSarifRun,
} from './qodana-attestation.js';
import type { PullRequestSource } from './qodana-source.js';
import type { RepositoryData, WorkflowData, WorkflowRunArtifact, WorkflowRunData } from './shared/action-context.js';
import { createValidators } from './shared/validation.js';
import { validateArtifactBinding } from './shared/artifact-binding.js';
import type { WorkflowRunEventRecord } from './shared/run-context.js';
import type { JsonValue } from './shared/json.js';

const VALID_RUN_CONCLUSIONS = ['success', 'failure', 'cancelled', 'timed_out'];

export class QodanaPublicationRejectedError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'QodanaPublicationRejectedError';
  }
}

function reject(message: string): never {
  throw new QodanaPublicationRejectedError(message);
}

const {
  requireBoundedInteger,
  requireEqual,
  requireObject,
  requireSha,
} = createValidators(reject);

export interface TrustedQodanaRun {
  runId: number;
  runAttempt: number;
  repository: string;
  conclusion: string;
}

function validateQodanaWorkflowSource({ eventRun, run, workflow, repository }: {
  eventRun: WorkflowRunEventRecord;
  run: WorkflowRunData;
  workflow: WorkflowData;
  repository: RepositoryData;
}): TrustedQodanaRun {
  requireEqual(eventRun.id, run.id, 'workflow run id');
  requireEqual(eventRun.run_attempt, run.run_attempt, 'workflow run attempt');
  if (eventRun.workflow_id != null) requireEqual(eventRun.workflow_id, run.workflow_id, 'workflow run workflow id');
  requireEqual(eventRun.event, 'pull_request_target', 'workflow run event');
  requireEqual(eventRun.status, 'completed', 'workflow run event status');
  requireEqual(eventRun.conclusion, run.conclusion, 'workflow run event conclusion');
  requireEqual(run.event, 'pull_request_target', 'workflow run event');
  requireEqual(run.status, 'completed', 'workflow run status');
  const conclusion = run.conclusion;
  if (conclusion == null || !VALID_RUN_CONCLUSIONS.includes(conclusion)) {
    reject('workflow run conclusion is invalid');
  }
  requireEqual(run.repository?.full_name, repository.full_name, 'workflow run repository');
  requireEqual(run.repository?.id, repository.id, 'workflow run repository id');
  requireEqual(run.head_repository?.full_name, repository.full_name, 'workflow head repository');
  requireEqual(run.head_repository?.id, repository.id, 'workflow head repository id');
  if (typeof run.head_branch !== 'string' || run.head_branch.length === 0) {
    reject('workflow run head branch is invalid');
  }
  requireSha(run.head_sha, 'workflow run head SHA');
  requireEqual(workflow.id, run.workflow_id, 'workflow identity');
  requireEqual(workflow.name, QODANA_WORKFLOW_NAME, 'workflow name');
  requireEqual(workflow.path, QODANA_WORKFLOW_PATH, 'workflow path');

  return {
    runId: requireBoundedInteger(run.id, 'Qodana workflow run id'),
    runAttempt: requireBoundedInteger(run.run_attempt, 'Qodana workflow run attempt'),
    repository: repository.full_name,
    conclusion,
  };
}

export interface ArtifactSelection {
  artifact: WorkflowRunArtifact;
  descriptor: ParsedQodanaArtifactName;
}

function matchingRunArtifacts({ artifacts, qodanaRun, prefix, parse }: {
  artifacts: WorkflowRunArtifact[];
  qodanaRun: WorkflowRunData;
  prefix: string;
  parse: (name: string) => ParsedQodanaArtifactName | null;
}): ArtifactSelection[] {
  if (!Array.isArray(artifacts)) reject('workflow artifacts are missing');
  return artifacts
    .filter((artifact) => typeof artifact?.name === 'string'
      && artifact.name.startsWith(prefix))
    .map((artifact) => ({ artifact, descriptor: parse(artifact.name) }))
    .filter((candidate): candidate is ArtifactSelection =>
      candidate.descriptor != null
      && candidate.descriptor.qodanaRunId === qodanaRun.id
      && candidate.descriptor.qodanaRunAttempt === qodanaRun.run_attempt);
}

function selectRunArtifact({
  artifacts,
  qodanaRun,
  repository,
  prefix,
  parse,
  maximumBytes,
  label,
}: {
  artifacts: WorkflowRunArtifact[];
  qodanaRun: WorkflowRunData;
  repository: RepositoryData;
  prefix: string;
  parse: (name: string) => ParsedQodanaArtifactName | null;
  maximumBytes: number;
  label: string;
}): ArtifactSelection {
  const candidates = matchingRunArtifacts({ artifacts, qodanaRun, prefix, parse });
  if (candidates.length !== 1) reject(`expected exactly one ${label}, found ${candidates.length}`);
  const candidate = candidates[0];
  if (candidate == null) reject(`expected exactly one ${label}, found 0`);
  validateArtifactBinding(reject, {
    artifact: candidate.artifact,
    expected: {
      runId: qodanaRun.id,
      repositoryId: repository.id,
      headRepositoryId: qodanaRun.head_repository?.id,
      headBranch: qodanaRun.head_branch,
      headSha: qodanaRun.head_sha,
    },
    maxBytes: maximumBytes,
    label,
  });
  return candidate;
}

function selectQodanaRunArtifact({ artifacts, qodanaRun, repository }: {
  artifacts: WorkflowRunArtifact[];
  qodanaRun: WorkflowRunData;
  repository: RepositoryData;
}): ArtifactSelection {
  return selectRunArtifact({
    artifacts,
    qodanaRun,
    repository,
    prefix: QODANA_ARTIFACT_PREFIX,
    parse: parseArtifactName,
    maximumBytes: MAX_ARTIFACT_BYTES,
    label: 'Qodana SARIF artifact',
  });
}

function validateQodanaArtifactSource({ descriptor, source }: {
  descriptor: ParsedQodanaArtifactName;
  source: PullRequestSource;
}): void {
  requireEqual(descriptor.sourceKind, source.sourceKind, 'Qodana source kind');
  requireEqual(descriptor.headSha, source.headSha, 'Qodana artifact head SHA');
  requireEqual(descriptor.baseSha, source.baseSha, 'Qodana artifact base SHA');
}

function selectQodanaArtifact({ artifacts, qodanaRun, source, repository }: {
  artifacts: WorkflowRunArtifact[];
  qodanaRun: WorkflowRunData;
  source: PullRequestSource;
  repository: RepositoryData;
}): WorkflowRunArtifact {
  const selection = selectQodanaRunArtifact({ artifacts, qodanaRun, repository });
  validateQodanaArtifactSource({ descriptor: selection.descriptor, source });
  return selection.artifact;
}

function validateArtifactLocation(location: JsonValue): void {
  if (location === null || typeof location !== 'object' || Array.isArray(location)) {
    reject('SARIF artifact location is invalid');
  }
  const { uri, uriBaseId } = location;
  if (uriBaseId != null
    && (typeof uriBaseId !== 'string' || !/^[A-Za-z0-9_.-]{1,128}$/.test(uriBaseId))) {
    reject('SARIF artifact location base id is invalid');
  }
  if (uri == null) return;
  if (typeof uri !== 'string' || uri.length > 4096 || uri.includes('\u0000')) {
    reject('SARIF artifact location is invalid');
  }
  if (uri.startsWith('/')
    || /^[A-Za-z]:[\\/]/.test(uri)
    || /^[A-Za-z][A-Za-z0-9+.-]*:/.test(uri)
    || uri.split(/[\\/]/).includes('..')) {
    reject('SARIF artifact location escapes the project');
  }
}

function validateNestedArtifactLocations(root: JsonValue): void {
  const pending: Array<{ value: JsonValue; depth: number }> = [{ value: root, depth: 0 }];
  let visited = 0;
  while (pending.length > 0) {
    const entry = pending.pop();
    if (entry == null) continue;
    const { value, depth } = entry;
    visited += 1;
    if (visited > 250_000 || depth > 100) reject('SARIF structure is too complex');
    if (value === null || typeof value !== 'object') continue;
    if (Array.isArray(value)) {
      for (const item of value) {
        pending.push({ value: item, depth: depth + 1 });
      }
      continue;
    }
    if (Object.hasOwn(value, 'uri') || Object.hasOwn(value, 'uriBaseId')) validateArtifactLocation(value);
    for (const [key, child] of Object.entries(value)) {
      if (key === 'artifactLocation') validateArtifactLocation(child);
      pending.push({ value: child, depth: depth + 1 });
    }
  }
}

function validateQodanaSarif(value: Buffer | string): QodanaSarifDocument {
  const bytes = Buffer.isBuffer(value) ? value : Buffer.from(String(value), 'utf8');
  if (bytes.length < 1 || bytes.length > MAX_SARIF_BYTES) reject('SARIF size is invalid');
  let document: QodanaSarifDocument;
  try {
    document = requireObject<QodanaSarifDocument>(JSON.parse(bytes.toString('utf8')), 'SARIF document');
  } catch (error) {
    if (error instanceof QodanaPublicationRejectedError) throw error;
    reject(`SARIF is not valid JSON: ${error instanceof Error ? error.message : String(error)}`);
  }
  requireEqual(document.version, '2.1.0', 'SARIF version');
  if (!Array.isArray(document.runs) || document.runs.length !== 1) reject('SARIF must contain exactly one run');
  const run = document.runs[0];
  if (run == null) reject('SARIF must contain exactly one run');
  const tool = requireObject<QodanaSarifRun['tool']>(run.tool, 'SARIF tool');
  const driver = requireObject<QodanaSarifRun['tool']['driver']>(tool.driver, 'SARIF driver');
  if (driver.name !== 'QDJVM') reject('SARIF driver name is invalid');
  if (!Array.isArray(run.results) || run.results.length > MAX_SARIF_RESULTS) reject('SARIF results are invalid');
  for (const result of run.results) {
    requireObject(result, 'SARIF result');
  }
  if (run.artifacts != null) {
    if (!Array.isArray(run.artifacts)) reject('SARIF artifacts are invalid');
    for (const artifact of run.artifacts) {
      const location = requireObject<QodanaSarifArtifact>(artifact, 'SARIF artifact').location;
      if (location != null) validateArtifactLocation(location);
    }
  }
  if (run.originalUriBaseIds != null) {
    const uriBases = requireObject<Record<string, QodanaSarifLocation>>(run.originalUriBaseIds, 'SARIF original URI bases');
    for (const location of Object.values(uriBases)) {
      validateArtifactLocation(location);
    }
  }
  validateNestedArtifactLocations(run);
  return document;
}

export {
  selectQodanaArtifact,
  selectQodanaRunArtifact,
  validateQodanaArtifactSource,
  validateQodanaSarif,
  validateQodanaWorkflowSource,
};
