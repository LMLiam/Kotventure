'use strict';

function pct(covered, missed) {
  const total = covered + missed;
  return total > 0 ? (covered / total) * 100 : 0;
}

function formatPct(value) {
  return `${value.toFixed(1)}%`;
}

function formatSigned(value, suffix) {
  const sign = value > 0 ? '+' : '';
  return `${sign}${value.toFixed(1)}${suffix}`;
}

function formatCount(value) {
  const sign = value > 0 ? '+' : '';
  return `${sign}${value}`;
}

function detailsTable(summary, markdownTable) {
  return [
    '<details>',
    `<summary>${summary}</summary>`,
    '',
    markdownTable.trimEnd(),
    '',
    '</details>',
  ].join('\n');
}

function sortedDeltas(labels, values) {
  const pairs = labels.map((label, i) => [label, values[i]]);
  pairs.sort((a, b) => Math.abs(b[1]) - Math.abs(a[1]));
  return { labels: pairs.map((p) => p[0]), values: pairs.map((p) => p[1]) };
}

module.exports = { pct, formatPct, formatSigned, formatCount, detailsTable, sortedDeltas };
