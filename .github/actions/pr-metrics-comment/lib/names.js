'use strict';

function sanitizeModule(name) {
  const cleaned = String(name).replace(/[^a-zA-Z0-9_-]/g, '');
  return cleaned || 'unknown';
}

function chartLabel(name) {
  return sanitizeModule(name)
    .replace(/^test-snapshot$/, 'test-snap')
    .replace(/^minimessage$/, 'mini');
}

module.exports = { sanitizeModule, chartLabel };
