'use strict';

const {
  MAX_ARTIFACT_BYTES,
  MAX_SARIF_BYTES,
  QODANA_SARIF_FILE_NAME,
} = require('./qodana-contract.js');
const { downloadSingleFileArtifact } = require('./shared/artifact-download.js');
const { validateQodanaSarif } = require('./qodana-publisher-validation.js');

async function downloadQodanaArtifact(options) {
  return downloadSingleFileArtifact({
    ...options,
    fileName: QODANA_SARIF_FILE_NAME,
    maxArchiveBytes: MAX_ARTIFACT_BYTES,
    maxBytes: MAX_SARIF_BYTES,
    label: 'Qodana artifact',
    validateResult: validateQodanaSarif,
  });
}

module.exports = {
  downloadQodanaArtifact,
};
