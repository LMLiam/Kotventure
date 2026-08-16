import {
  CODEQL_SARIF_FILE_NAME,
  MAX_CODEQL_ARTIFACT_BYTES,
  MAX_CODEQL_SARIF_BYTES,
} from './codeql-contract.js';
import type { CodeqlCategory } from './codeql-contract.js';
import { downloadSingleFileArtifact } from './shared/artifact-download.js';
import { validateCodeqlSarif } from './codeql-validation.js';
import type { WorkflowRunArtifact } from './shared/action-context.js';

export async function downloadCodeqlSarif({
  owner,
  repo,
  artifact,
  category,
  token,
  outputDirectory,
  fetchImpl,
}: {
  owner: string;
  repo: string;
  artifact: WorkflowRunArtifact;
  category: CodeqlCategory;
  token: string;
  outputDirectory: string;
  fetchImpl?: typeof fetch;
}): Promise<string> {
  return downloadSingleFileArtifact({
    owner,
    repo,
    artifactId: artifact.id,
    outputDirectory,
    fileName: CODEQL_SARIF_FILE_NAME,
    maxArchiveBytes: MAX_CODEQL_ARTIFACT_BYTES,
    maxBytes: MAX_CODEQL_SARIF_BYTES,
    label: `CodeQL ${category} artefact`,
    token,
    fetchImpl,
    validateResult: validateCodeqlSarif,
  });
}
