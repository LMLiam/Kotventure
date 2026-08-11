'use strict';

const fs = require('node:fs');
const path = require('node:path');
const {
    MAX_RESULT_BYTES,
    validateMetricsResult,
} = require('../actions/pr-metrics-comment/lib/metrics-result.js');
const {
    MAX_ARTIFACT_BYTES,
    RESULT_FILE_NAME,
} = require('./pr-metrics-publisher-contract.js');
const { downloadSingleFileArtifact } = require('./shared/artifact-download.js');

async function downloadMetricsArtifact(options) {
    return downloadSingleFileArtifact({
        ...options,
        fileName: RESULT_FILE_NAME,
        maxArchiveBytes: MAX_ARTIFACT_BYTES,
        maxBytes: MAX_RESULT_BYTES,
        label: 'metrics artifact',
    });
}

function readMetricsArtifact(directory) {
    if (typeof directory !== 'string' || directory.length === 0) {
        throw new Error('metrics artifact directory is required');
    }

    const entries = fs.readdirSync(directory, { withFileTypes: true });

    if (entries.length !== 1 || entries[0].name !== RESULT_FILE_NAME) {
        throw new Error(`metrics artifact must contain only ${RESULT_FILE_NAME}`);
    }

    const filePath = path.join(directory, RESULT_FILE_NAME);
    const stats = fs.lstatSync(filePath);

    if (!stats.isFile() || stats.isSymbolicLink()) {
        throw new Error(`${RESULT_FILE_NAME} must be a regular file`);
    }

    if (stats.size > MAX_RESULT_BYTES) {
        throw new Error(`metrics result exceeds ${MAX_RESULT_BYTES} bytes`);
    }

    const bytes = fs.readFileSync(filePath);

    if (bytes.length > MAX_RESULT_BYTES) {
        throw new Error(`metrics result exceeds ${MAX_RESULT_BYTES} bytes`);
    }

    let result;

    try {
        result = JSON.parse(bytes.toString('utf8'));
    } catch (error) {
        const message = error instanceof Error ? error.message : String(error);
        throw new Error(`metrics result is not valid JSON: ${message}`);
    }

    return validateMetricsResult(result);
}

module.exports = {
    downloadMetricsArtifact,
    readMetricsArtifact,
};
