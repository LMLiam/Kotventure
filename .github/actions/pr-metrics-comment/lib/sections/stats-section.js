'use strict';

const { formatCount, detailsTable } = require('./format.js');

function formatDuration(seconds) {
  if (seconds == null) {
    return '—';
  }
  const minutes = Math.floor(seconds / 60);
  return minutes > 0 ? `${minutes}m${String(seconds % 60).padStart(2, '0')}s` : `${seconds}s`;
}

function statsSection({ headMetrics, baseMetrics }) {
  if (!headMetrics) {
    return { lines: [], verdictPart: null, warnings: [], changed: false };
  }
  const testsDelta = baseMetrics ? headMetrics.tests - baseMetrics.tests : null;
  let table = '| | PR | Base |\n|--|----|------|\n';
  table += `| Tests | ${headMetrics.tests} | ${baseMetrics ? baseMetrics.tests : '—'} |\n`;
  table += `| Skipped | ${headMetrics.skipped} | ${baseMetrics ? baseMetrics.skipped : '—'} |\n`;
  table += `| Build time (indicative) | ${formatDuration(headMetrics.durationSeconds)} | ${formatDuration(baseMetrics?.durationSeconds)} |\n`;

  const deltaPart = testsDelta ? ` (${formatCount(testsDelta)})` : '';
  return {
    lines: [detailsTable('Build stats', table), ''],
    verdictPart: `🧪 ${headMetrics.tests} tests${deltaPart}`,
    warnings: [],
    changed: testsDelta != null && testsDelta !== 0,
  };
}

module.exports = { statsSection };
