import { MAX_RESULT_BYTES } from '../actions/pr-metrics-comment/lib/metrics-result-contract.js';
import {
  MAX_ARTIFACT_BYTES,
  RESULT_FILE_NAME,
} from './pr-metrics-publisher-contract.js';
import { extractSingleEntryArchive } from './shared/artifact-archive.js';

export async function extractMetricsResultArchive(archive: Buffer): Promise<Buffer> {
  return extractSingleEntryArchive(archive, {
    errorPrefix: 'metrics artifact archive',
    expectedFileName: RESULT_FILE_NAME,
    maxArchiveBytes: MAX_ARTIFACT_BYTES,
    maxBytes: MAX_RESULT_BYTES,
  });
}
