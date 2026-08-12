import {
  MAX_ARTIFACT_BYTES,
  MAX_SARIF_BYTES,
  QODANA_SARIF_FILE_NAME,
} from './qodana-contract.js';
import { downloadSingleFileArtifact, type DownloadSingleFileArtifactOptions } from './shared/artifact-download.js';
import { validateQodanaSarif } from './qodana-publisher-validation.js';

export async function downloadQodanaArtifact(
  options: Omit<DownloadSingleFileArtifactOptions, 'fileName' | 'maxArchiveBytes' | 'maxBytes' | 'label' | 'validateResult'>,
): Promise<string> {
  return downloadSingleFileArtifact({
    ...options,
    fileName: QODANA_SARIF_FILE_NAME,
    maxArchiveBytes: MAX_ARTIFACT_BYTES,
    maxBytes: MAX_SARIF_BYTES,
    label: 'Qodana artifact',
    validateResult: validateQodanaSarif,
  });
}
