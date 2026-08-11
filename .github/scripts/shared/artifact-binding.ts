import { createValidators, type ValidatorReject } from './validation.js';

export interface ArtifactWorkflowRunBinding {
  id: number;
  repository_id: number;
  head_repository_id: number;
  head_branch: string;
  head_sha: string;
}

export interface ArtifactBindingRecord {
  id: number;
  expired: boolean;
  size_in_bytes: number;
  workflow_run: ArtifactWorkflowRunBinding | null;
}

export interface ArtifactBindingExpectation {
  runId: number;
  repositoryId: number;
  headRepositoryId: number | null | undefined;
  headBranch: string;
  headSha: string;
}

export interface ArtifactBindingOptions {
  artifact: ArtifactBindingRecord;
  expected: ArtifactBindingExpectation;
  maxBytes: number;
  label: string;
}

export function validateArtifactBinding(
  reject: ValidatorReject,
  { artifact, expected, maxBytes, label }: ArtifactBindingOptions,
): void {
  const { requireBoundedInteger, requireEqual, requireObject } = createValidators(reject);

  if (artifact.expired !== false) reject(`${label} is expired`);
  requireBoundedInteger(artifact.size_in_bytes, `${label} size`, 1, maxBytes);

  const workflowRun = requireObject<ArtifactWorkflowRunBinding>(artifact.workflow_run, `${label} workflow run`);
  requireEqual(workflowRun.id, expected.runId, `${label} workflow run id`);
  requireEqual(workflowRun.repository_id, expected.repositoryId, `${label} repository id`);
  requireEqual(workflowRun.head_repository_id, expected.headRepositoryId, `${label} head repository id`);
  requireEqual(workflowRun.head_branch, expected.headBranch, `${label} head branch`);
  requireEqual(workflowRun.head_sha, expected.headSha, `${label} head SHA`);
}
