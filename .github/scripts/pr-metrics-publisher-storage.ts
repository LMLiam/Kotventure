import * as fs from 'node:fs';
import * as path from 'node:path';
import {
  MAX_RESULT_BYTES,
  validateMetricsResult,
} from '../actions/pr-metrics-comment/lib/metrics-result.js';
import type { MetricsResultValue } from '../actions/pr-metrics-comment/lib/metrics-result-validation.js';
import {
  MAX_ARTIFACT_BYTES,
  RESULT_FILE_NAME,
} from './pr-metrics-publisher-contract.js';
import { downloadSingleFileArtifact, type DownloadSingleFileArtifactOptions } from './shared/artifact-download.js';

export async function downloadMetricsArtifact(
  options: Omit<DownloadSingleFileArtifactOptions, 'fileName' | 'maxArchiveBytes' | 'maxBytes' | 'label'>,
): Promise<string> {
  return downloadSingleFileArtifact({
    ...options,
    fileName: RESULT_FILE_NAME,
    maxArchiveBytes: MAX_ARTIFACT_BYTES,
    maxBytes: MAX_RESULT_BYTES,
    label: 'metrics artifact',
  });
}

export function readMetricsArtifact(directory: string): MetricsResultValue {
  if (typeof directory !== 'string' || directory.length === 0) {
    throw new Error('metrics artifact directory is required');
  }

  const entries = fs.readdirSync(directory, { withFileTypes: true });
  const entry = entries[0];

  if (entries.length !== 1 || entry == null || entry.name !== RESULT_FILE_NAME) {
    throw new Error(`metrics artifact must contain only ${RESULT_FILE_NAME}`);
  }

  const filePath = path.join(directory, RESULT_FILE_NAME);
  const stats = fs.lstatSync(filePath);

  if (!stats.isFile() || stats.isSymbolicLink()) throw new Error(`${RESULT_FILE_NAME} must be a regular file`);

  if (stats.size > MAX_RESULT_BYTES) throw new Error(`metrics result exceeds ${MAX_RESULT_BYTES} bytes`);

  const bytes = fs.readFileSync(filePath);

  if (bytes.length > MAX_RESULT_BYTES) throw new Error(`metrics result exceeds ${MAX_RESULT_BYTES} bytes`);

  let result;

  try {
    result = JSON.parse(bytes.toString('utf8'));
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    throw new Error(`metrics result is not valid JSON: ${message}`);
  }

  return validateMetricsResult(result);
}
