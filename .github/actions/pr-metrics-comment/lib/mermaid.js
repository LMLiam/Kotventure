'use strict';

function round1(n) {
  return Math.round(n * 10) / 10;
}

function mermaidFence(config, bodyLines) {
  return ['```mermaid', '---', 'config:', ...config, '---', ...bodyLines, '```'].join('\n');
}

function deltaVerticalBars({ title, labels, deltas, yLabel, color }) {
  if (!labels.length) {
    return null;
  }
  const values = deltas.map((d) => round1(d));
  const min = Math.min(0, ...values);
  const max = Math.max(0, ...values);
  const pad = Math.max(0.5, Math.ceil((max - min) * 0.15 * 10) / 10);
  const yMin = Math.floor((min - pad) * 10) / 10;
  const yMax = Math.ceil((max + pad) * 10) / 10;
  const width = Math.max(640, Math.min(1100, 100 + labels.length * 90));
  const height = 400;
  const config = [
    '  xyChart:',
    `    width: ${width}`,
    `    height: ${height}`,
    '  themeVariables:',
    '    xyChart:',
    '      backgroundColor: "transparent"',
    '      titleColor: "#c4b5fd"',
    '      xAxisLabelColor: "#94a3b8"',
    '      xAxisTitleColor: "#94a3b8"',
    '      xAxisTickColor: "#334155"',
    '      xAxisLineColor: "#334155"',
    '      yAxisLabelColor: "#94a3b8"',
    '      yAxisTitleColor: "#94a3b8"',
    '      yAxisTickColor: "#334155"',
    '      yAxisLineColor: "#334155"',
    '      plotColorPalette: "' + (color || '#a78bfa') + '"',
  ];
  const axisLabels = labels.map((l) => `"${l}"`).join(', ');
  const lines = [
    'xychart-beta',
    `  title "${title}"`,
    `  x-axis [${axisLabels}]`,
    `  y-axis "${yLabel}" ${yMin} --> ${yMax}`,
    `  bar [${values.join(', ')}]`,
  ];
  return mermaidFence(config, lines);
}

module.exports = { deltaVerticalBars };
