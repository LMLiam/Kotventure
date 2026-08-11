import {
  MAX_ARTIFACT_BYTES,
  MAX_SARIF_BYTES,
  QODANA_SARIF_FILE_NAME,
} from './qodana-contract.js';
import { extractSingleEntryArchive } from './shared/artifact-archive.js';

export async function extractQodanaSarifArchive(archive: Buffer): Promise<Buffer> {
  return extractSingleEntryArchive(archive, {
    errorPrefix: 'Qodana artifact archive',
    expectedFileName: QODANA_SARIF_FILE_NAME,
    maxArchiveBytes: MAX_ARTIFACT_BYTES,
    maxBytes: MAX_SARIF_BYTES,
  });
}
