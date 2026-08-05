'use strict';

const {
  MAX_REF_LENGTH,
  MAX_RESULT_BYTES,
  SCHEMA_VERSION,
  WORKFLOW_NAME,
} = require('./metrics-result-contract.js');
const { validateMetricsResult } = require('./metrics-result-validation.js');
const { serializeMetricsResult } = require('./metrics-result-serialization.js');
const { deserializeMetricsResult } = require('./metrics-result-deserialization.js');

module.exports = {
  MAX_REF_LENGTH,
  MAX_RESULT_BYTES,
  SCHEMA_VERSION,
  WORKFLOW_NAME,
  deserializeMetricsResult,
  serializeMetricsResult,
  validateMetricsResult,
};
