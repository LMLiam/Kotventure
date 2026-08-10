'use strict';

const {
  MAX_ARTIFACT_BYTES,
  MAX_SARIF_BYTES,
  QODANA_SARIF_FILE_NAME,
} = require('./qodana-contract.js');
const { extractSingleEntryArchive } = require('./shared/artifact-archive.js');

function extractQodanaSarifArchive(archive) {
  return extractSingleEntryArchive(archive, {
    errorPrefix: 'Qodana artifact archive',
    expectedFileName: QODANA_SARIF_FILE_NAME,
    maxArchiveBytes: MAX_ARTIFACT_BYTES,
    maxBytes: MAX_SARIF_BYTES,
  });
}

module.exports = { extractQodanaSarifArchive };
