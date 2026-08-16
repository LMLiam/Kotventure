import {
  buildJunitArtifactName,
  JUNIT_ARTIFACT_PREFIX,
  MAX_JUNIT_ARTIFACT_BYTES,
  MAX_JUNIT_ENTRY_BYTES,
  MAX_JUNIT_FILES,
  MAX_JUNIT_TOTAL_BYTES,
  parseJunitArtifactName,
} from './junit-contract.js';
import type { JunitArtifactShard, JunitReportKind, ParsedJunitArtifactName } from './junit-contract.js';
import { aggregateJunitReports, parseJunitReport } from './junit-parser.js';
import type { JunitAggregate, JunitReport } from './junit-parser.js';
import type { RepositoryData, WorkflowRunArtifact, WorkflowRunData } from './shared/action-context.js';
import { extractArchiveEntries } from './shared/artifact-archive.js';
import { downloadArtifactArchive } from './shared/artifact-download.js';
import { validateArtifactBinding } from './shared/artifact-binding.js';
import { createValidators } from './shared/validation.js';

const { requireBoundedInteger, requireSha } = createValidators((message: string): never => {
  throw new JunitPublicationRejectedError(message);
});

export class JunitPublicationRejectedError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'JunitPublicationRejectedError';
  }
}

function reject(message: string): never {
  throw new JunitPublicationRejectedError(message);
}

export interface JunitArtifactSelection {
  artifact: WorkflowRunArtifact;
  descriptor: ParsedJunitArtifactName;
}

function expectedArtifactName({
  kind,
  shard,
  run,
  headSha,
}: {
  kind: JunitReportKind;
  shard: JunitArtifactShard;
  run: WorkflowRunData;
  headSha?: string;
}): string {
  const trustedHeadSha = requireSha(headSha ?? run.head_sha, 'JUnit head SHA');
  return buildJunitArtifactName({
    kind,
    shard,
    workflowId: requireBoundedInteger(run.workflow_id, 'JUnit workflow id'),
    runId: requireBoundedInteger(run.id, 'JUnit workflow run id'),
    runAttempt: requireBoundedInteger(run.run_attempt, 'JUnit workflow run attempt'),
    headSha: trustedHeadSha,
  });
}

export function selectJunitArtifact({
  artifacts,
  run,
  repository,
  kind,
  shard,
  headSha,
  allowMissing = false,
}: {
  artifacts: WorkflowRunArtifact[];
  run: WorkflowRunData;
  repository: RepositoryData;
  kind: JunitReportKind;
  shard: JunitArtifactShard;
  headSha?: string;
  allowMissing?: boolean;
}): JunitArtifactSelection | null {
  if (!Array.isArray(artifacts)) reject('workflow artifacts are missing');
  const trustedHeadSha = requireSha(headSha ?? run.head_sha, 'JUnit head SHA');
  const name = expectedArtifactName({ kind, shard, run, headSha: trustedHeadSha });
  const candidates = artifacts.filter((artifact) => artifact.name === name);
  if (candidates.length !== 1) {
    if (allowMissing && candidates.length === 0) return null;
    reject(`expected exactly one ${name}, found ${candidates.length}`);
  }
  const artifact = candidates[0] as WorkflowRunArtifact;
  const descriptor = parseJunitArtifactName(artifact.name);
  if (descriptor == null || descriptor.kind !== kind || descriptor.shard !== shard) {
    reject('JUnit artefact name is invalid');
  }
  if (descriptor.workflowId !== run.workflow_id) reject('JUnit artefact workflow id does not match');
  if (descriptor.headSha !== trustedHeadSha) reject('JUnit artefact head SHA does not match');
  validateArtifactBinding((message) => reject(message), {
    artifact,
    expected: {
      runId: run.id,
      repositoryId: repository.id,
      headRepositoryId: typeof run.head_repository?.id === 'number' ? run.head_repository.id : 0,
      headBranch: run.head_branch,
      headSha: run.head_sha,
    },
    maxBytes: MAX_JUNIT_ARTIFACT_BYTES,
    label: 'JUnit artefact',
  });
  return { artifact, descriptor };
}

function expectedReportPath(kind: JunitReportKind, fileName: string): boolean {
  const normalized = fileName.startsWith('junit-handoff/') ? fileName.slice('junit-handoff/'.length) : fileName;
  if (kind === 'vanilla') return /^modules\/core\/build\/test-results\/vanillaConformanceTest\/TEST-[^/]+\.xml$/.test(normalized);
  return /^modules\/[^/]+\/build\/test-results\/test\/TEST-[^/]+\.xml$/.test(normalized);
}

export async function downloadJunitReports({
  owner,
  repo,
  artifact,
  kind,
  token,
  fetchImpl,
}: {
  owner: string;
  repo: string;
  artifact: WorkflowRunArtifact;
  kind: JunitReportKind;
  token: string;
  fetchImpl?: typeof fetch;
}): Promise<JunitReport[]> {
  const archive = await downloadArtifactArchive({
    owner,
    repo,
    artifactId: artifact.id,
    maxArchiveBytes: MAX_JUNIT_ARTIFACT_BYTES,
    label: 'JUnit artefact',
    token,
    fetchImpl,
  });
  const entries = await extractArchiveEntries(archive, {
    errorPrefix: 'JUnit artefact archive',
    maxArchiveBytes: MAX_JUNIT_ARTIFACT_BYTES,
    maxEntries: MAX_JUNIT_FILES,
    maxEntryBytes: MAX_JUNIT_ENTRY_BYTES,
    maxTotalBytes: MAX_JUNIT_TOTAL_BYTES,
  });
  if (entries.length < 1) reject('JUnit artefact contains no files');
  const reports: JunitReport[] = [];
  for (const entry of entries) {
    if (!expectedReportPath(kind, entry.fileName)) reject(`JUnit artefact path is unexpected: ${entry.fileName}`);
    const fileName = entry.fileName.startsWith('junit-handoff/')
      ? entry.fileName.slice('junit-handoff/'.length)
      : entry.fileName;
    reports.push(parseJunitReport(fileName, entry.content));
  }
  if (reports.length < 1) reject('JUnit artefact contains no test reports');
  return reports;
}

export function aggregateJunitArtifactReports(reports: JunitReport[][]): JunitAggregate {
  return aggregateJunitReports(reports.flat());
}

export { JUNIT_ARTIFACT_PREFIX };
