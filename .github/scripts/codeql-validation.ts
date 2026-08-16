import type { RepositoryData, WorkflowData, WorkflowRunArtifact, WorkflowRunData } from './shared/action-context.js';
import type { WorkflowRunEventRecord } from './shared/run-context.js';
import { createValidators } from './shared/validation.js';
import { validateArtifactBinding } from './shared/artifact-binding.js';
import {
  CODEQL_ARTIFACT_PREFIX,
  CODEQL_CATEGORIES,
  CODEQL_WORKFLOW_NAME,
  CODEQL_WORKFLOW_PATH,
  MAX_CODEQL_ARTIFACT_BYTES,
  MAX_CODEQL_RESULTS,
  MAX_CODEQL_SARIF_BYTES,
  parseCodeqlArtifactName,
} from './codeql-contract.js';
import type { CodeqlCategory, ParsedCodeqlArtifactName } from './codeql-contract.js';
import type { JsonValue } from './shared/json.js';

export class CodeqlPublicationRejectedError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'CodeqlPublicationRejectedError';
  }
}

function reject(message: string): never {
  throw new CodeqlPublicationRejectedError(message);
}

const {
  requireBoundedInteger,
  requireEqual,
  requireObject,
  requireSha,
} = createValidators(reject);

const VALID_EVENTS = new Set(['pull_request', 'merge_group']);
const URI_BASE_ID_PATTERN = /^(?:[A-Za-z0-9_.-]{1,128}|%[A-Za-z0-9_.-]{1,126}%)$/;
// A result expands into message, location, region, and URI nodes. Budget
// enough nodes per result so the structural guard never fires before the
// declared result bound; the byte bound remains the hard resource limit.
const MAX_SARIF_NODES_PER_RESULT = 40;
const MAX_SARIF_TRAVERSAL_NODES = MAX_CODEQL_RESULTS * MAX_SARIF_NODES_PER_RESULT;

export interface TrustedCodeqlRun {
  runId: number;
  runAttempt: number;
  repository: string;
  headSha: string;
  event: 'pull_request' | 'merge_group';
}

export function validateCodeqlWorkflowSource({
  eventRun,
  run,
  workflow,
  repository,
}: {
  eventRun: WorkflowRunEventRecord;
  run: WorkflowRunData;
  workflow: WorkflowData;
  repository: RepositoryData;
}): TrustedCodeqlRun {
  requireEqual(eventRun.id, run.id, 'workflow run id');
  requireEqual(eventRun.run_attempt, run.run_attempt, 'workflow run attempt');
  requireEqual(eventRun.head_sha, run.head_sha, 'workflow run head SHA');
  if (eventRun.workflow_id != null) requireEqual(eventRun.workflow_id, run.workflow_id, 'workflow run workflow id');
  const event = run.event;
  if (event !== 'pull_request' && event !== 'merge_group') reject('CodeQL workflow event is invalid');
  requireEqual(eventRun.event, event, 'workflow run event');
  requireEqual(run.repository?.full_name, repository.full_name, 'workflow run repository');
  requireEqual(run.repository?.id, repository.id, 'workflow run repository id');
  requireSha(run.head_sha, 'CodeQL analysed SHA');
  if (typeof run.head_branch !== 'string' || run.head_branch.length < 1) reject('CodeQL head branch is invalid');
  requireEqual(workflow.id, run.workflow_id, 'workflow identity');
  requireEqual(workflow.name, CODEQL_WORKFLOW_NAME, 'workflow name');
  requireEqual(workflow.path, CODEQL_WORKFLOW_PATH, 'workflow path');
  if (eventRun.status != null && eventRun.status !== 'in_progress' && eventRun.status !== 'completed') {
    reject('CodeQL workflow event status is invalid');
  }
  if (eventRun.status === 'completed' && run.status !== 'completed') reject('CodeQL workflow has not completed');
  if (event === 'merge_group') {
    requireEqual(run.head_repository?.full_name, repository.full_name, 'merge-group head repository');
    requireEqual(run.head_repository?.id, repository.id, 'merge-group head repository id');
    if (!run.head_branch.startsWith('gh-readonly-queue/')) reject('merge-group head branch is invalid');
  }
  return {
    runId: requireBoundedInteger(run.id, 'CodeQL workflow run id'),
    runAttempt: requireBoundedInteger(run.run_attempt, 'CodeQL workflow run attempt'),
    repository: repository.full_name,
    headSha: requireSha(run.head_sha, 'CodeQL analysed SHA'),
    event,
  };
}

export interface CodeqlArtifactSelection {
  artifact: WorkflowRunArtifact;
  descriptor: ParsedCodeqlArtifactName;
}

export function selectCodeqlArtifact({
  artifacts,
  run,
  repository,
  category,
  headSha,
}: {
  artifacts: WorkflowRunArtifact[];
  run: WorkflowRunData;
  repository: RepositoryData;
  category: CodeqlCategory;
  headSha?: string;
}): CodeqlArtifactSelection {
  if (!Array.isArray(artifacts)) reject('CodeQL workflow artefacts are missing');
  const trustedHeadSha = requireSha(headSha ?? run.head_sha, 'CodeQL analysed SHA');
  const candidates = artifacts
    .filter((artifact) => typeof artifact?.name === 'string' && artifact.name.startsWith(CODEQL_ARTIFACT_PREFIX))
    .map((artifact) => ({ artifact, descriptor: parseCodeqlArtifactName(artifact.name) }))
    .filter((candidate): candidate is CodeqlArtifactSelection => candidate.descriptor != null
      && candidate.descriptor.category === category
      && candidate.descriptor.workflowId === run.workflow_id
      && candidate.descriptor.runId === run.id
      && candidate.descriptor.runAttempt === run.run_attempt
      && candidate.descriptor.headSha === trustedHeadSha);
  if (candidates.length !== 1) reject(`expected exactly one CodeQL ${category} artefact, found ${candidates.length}`);
  const candidate = candidates[0] as CodeqlArtifactSelection;
  validateArtifactBinding(reject, {
    artifact: candidate.artifact,
    expected: {
      runId: run.id,
      repositoryId: repository.id,
      headRepositoryId: requireBoundedInteger(run.head_repository?.id, 'CodeQL head repository id'),
      headBranch: run.head_branch,
      headSha: run.head_sha,
    },
    maxBytes: MAX_CODEQL_ARTIFACT_BYTES,
    label: `CodeQL ${category} artefact`,
  });
  return candidate;
}

function validateUriBaseId(value: JsonValue): void {
  if (typeof value !== 'string' || !URI_BASE_ID_PATTERN.test(value)) reject('SARIF location base id is invalid');
}

function validateBaseUri(value: JsonValue): void {
  if (typeof value !== 'string' || value.length > 4096 || value.includes('\u0000')) reject('SARIF base URI is invalid');
  if (value.startsWith('file:')) {
    let url: URL;
    try {
      url = new URL(value);
    } catch {
      reject('SARIF base URI is invalid');
    }
    if (url.protocol !== 'file:' || (url.hostname !== '' && url.hostname !== 'localhost')) {
      reject('SARIF base URI is invalid');
    }
    try {
      if (decodeURIComponent(url.pathname).split(/[\\/]/).includes('..')) reject('SARIF base URI escapes the project');
    } catch {
      reject('SARIF base URI is invalid');
    }
    return;
  }
  if (value.startsWith('/') || /^[A-Za-z]:[\\/]/.test(value)
    || /^[A-Za-z][A-Za-z0-9+.-]*:/.test(value)
    || value.split(/[\\/]/).includes('..')) reject('SARIF base URI escapes the project');
}

function validateOriginalUriBaseIds(value: JsonValue): void {
  if (value === null || typeof value !== 'object' || Array.isArray(value)) reject('SARIF URI base definitions are invalid');
  for (const [baseId, definition] of Object.entries(value)) {
    validateUriBaseId(baseId);
    if (definition === null || typeof definition !== 'object' || Array.isArray(definition)) {
      reject('SARIF URI base definition is invalid');
    }
    const uri = definition.uri;
    const uriBaseId = definition.uriBaseId;
    if (uri !== undefined) validateBaseUri(uri);
    if (uriBaseId !== undefined) validateUriBaseId(uriBaseId);
  }
}

function validateLocation(location: JsonValue): void {
  if (location === null || typeof location !== 'object' || Array.isArray(location)) reject('SARIF location is invalid');
  const { uri, uriBaseId } = location;
  if (uriBaseId != null) validateUriBaseId(uriBaseId);
  if (uri == null) return;
  if (typeof uri !== 'string' || uri.length > 4096 || uri.includes('\u0000')
    || uri.startsWith('/') || /^[A-Za-z]:[\\/]/.test(uri) || /^[A-Za-z][A-Za-z0-9+.-]*:/.test(uri)
    || uri.split(/[\\/]/).includes('..')) reject('SARIF location escapes the project');
}

function validateSarifLocations(root: JsonValue): void {
  const pending: Array<{ value: JsonValue; depth: number }> = [{ value: root, depth: 0 }];
  let visited = 0;
  while (pending.length > 0) {
    const entry = pending.pop();
    if (entry == null) continue;
    visited += 1;
    if (visited > MAX_SARIF_TRAVERSAL_NODES || entry.depth > 100) reject('SARIF structure is too complex');
    if (entry.value === null || typeof entry.value !== 'object') continue;
    if (Array.isArray(entry.value)) {
      for (const child of entry.value) pending.push({ value: child, depth: entry.depth + 1 });
      continue;
    }
    if (Object.hasOwn(entry.value, 'uri') || Object.hasOwn(entry.value, 'uriBaseId')) {
      validateLocation(entry.value);
    }
    for (const [key, child] of Object.entries(entry.value)) {
      if (key === 'originalUriBaseIds') {
        validateOriginalUriBaseIds(child);
        continue;
      }
      if (key === 'artifactLocation') validateLocation(child);
      pending.push({ value: child, depth: entry.depth + 1 });
    }
  }
}

export interface CodeqlSarifDocument {
  version?: unknown;
  runs?: Array<{
    tool?: { driver?: { name?: unknown } };
    results?: unknown[];
  }>;
}

export function validateCodeqlSarif(value: Buffer | string): CodeqlSarifDocument {
  const bytes = Buffer.isBuffer(value) ? value : Buffer.from(String(value), 'utf8');
  if (bytes.length < 1 || bytes.length > MAX_CODEQL_SARIF_BYTES) reject('SARIF size is invalid');
  let document: CodeqlSarifDocument;
  try {
    document = requireObject<CodeqlSarifDocument>(JSON.parse(bytes.toString('utf8')), 'SARIF document');
  } catch (error) {
    if (error instanceof CodeqlPublicationRejectedError) throw error;
    reject(`SARIF is not valid JSON: ${error instanceof Error ? error.message : String(error)}`);
  }
  requireEqual(document.version, '2.1.0', 'SARIF version');
  if (!Array.isArray(document.runs) || document.runs.length !== 1) reject('SARIF must contain exactly one run');
  const run = document.runs[0];
  if (run == null) reject('SARIF run is missing');
  const tool = requireObject<{ driver?: { name?: unknown } }>(run.tool, 'SARIF tool');
  const driver = requireObject<{ name?: unknown }>(tool.driver, 'SARIF driver');
  if (driver.name !== 'CodeQL') reject('SARIF driver name is invalid');
  if (!Array.isArray(run.results) || run.results.length > MAX_CODEQL_RESULTS) reject('SARIF results are invalid');
  validateSarifLocations(document as unknown as JsonValue);
  return document;
}

export { CODEQL_CATEGORIES };
