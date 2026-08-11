'use strict';

const { MAX_RESULT_BYTES } = require('../actions/pr-metrics-comment/lib/metrics-result-contract.js');
const {
    MAX_ARTIFACT_BYTES,
    RESULT_FILE_NAME,
} = require('./pr-metrics-publisher-contract.js');
const { extractSingleEntryArchive } = require('./shared/artifact-archive.js');

function extractMetricsResultArchive(archive) {
    return extractSingleEntryArchive(archive, {
        errorPrefix: 'metrics artifact archive',
        expectedFileName: RESULT_FILE_NAME,
        maxArchiveBytes: MAX_ARTIFACT_BYTES,
        maxBytes: MAX_RESULT_BYTES,
    });
}

module.exports = { extractMetricsResultArchive };
