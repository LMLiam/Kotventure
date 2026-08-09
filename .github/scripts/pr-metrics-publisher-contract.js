'use strict';

const { WORKFLOW_NAME } = require('../actions/pr-metrics-comment/lib/metrics-result-contract.js');

const EXPECTED_WORKFLOW_PATH = '.github/workflows/ci.yml';
const MAX_ARTIFACT_BYTES = 256 * 1024;
const RESULT_ARTIFACT_PREFIX = 'pr-metrics-result-';
const RESULT_FILE_NAME = 'pr-metrics-result.json';

module.exports = {
    EXPECTED_WORKFLOW_PATH,
    MAX_ARTIFACT_BYTES,
    RESULT_ARTIFACT_PREFIX,
    RESULT_FILE_NAME,
    WORKFLOW_NAME,
};
