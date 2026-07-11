'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { deltaVerticalBars } = require('../lib/mermaid.js');

test('renders a fenced xychart with rounded values', () => {
  const chart = deltaVerticalBars({
    title: 'Coverage delta',
    labels: ['core', 'mini'],
    deltas: [1.26, -0.34],
    yLabel: 'Δ pp',
    color: '#a78bfa',
  });
  assert.ok(chart.startsWith('```mermaid'));
  assert.ok(chart.endsWith('```'));
  assert.ok(chart.includes('xychart-beta'));
  assert.ok(chart.includes('title "Coverage delta"'));
  assert.ok(chart.includes('x-axis ["core", "mini"]'));
  assert.ok(chart.includes('bar [1.3, -0.3]'));
  assert.ok(chart.includes('plotColorPalette: "#a78bfa"'));
});

test('y-axis range always spans zero with padding', () => {
  const chart = deltaVerticalBars({
    title: 't',
    labels: ['core'],
    deltas: [2],
    yLabel: 'y',
  });
  const match = chart.match(/y-axis "y" (-?[\d.]+) --> (-?[\d.]+)/);
  assert.ok(match);
  assert.ok(Number(match[1]) < 0);
  assert.ok(Number(match[2]) > 2);
});

test('returns null when there are no labels', () => {
  assert.equal(deltaVerticalBars({ title: 't', labels: [], deltas: [], yLabel: 'y' }), null);
});
