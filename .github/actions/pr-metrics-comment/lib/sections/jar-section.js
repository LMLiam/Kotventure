'use strict';

const { chartLabel } = require('../names.js');
const { deltaVerticalBars } = require('../mermaid.js');
const { formatSigned, formatCount, detailsTable, sortedDeltas } = require('./format.js');

function kb(bytes) {
  return `${(bytes / 1024).toFixed(1)} KB`;
}

function classCell(head, base) {
  if (head?.classes == null) {
    return '—';
  }
  if (base?.classes != null && base.classes !== head.classes) {
    return `${head.classes} (${formatCount(head.classes - base.classes)})`;
  }
  return `${head.classes}`;
}

function jarSection({ headJars, baseJars, growthThreshold }) {
  const warnings = [];
  const modules = [...new Set([...headJars.keys(), ...baseJars.keys()])].sort((a, b) => a.localeCompare(b));
  const deltaLabels = [];
  const deltaVals = [];
  let hasAnyBase = false;
  let membershipChanged = false;
  let headTotal = 0;
  let baseTotal = 0;
  let table = '| Module | PR | Base | Δ | Classes |\n|--------|----|------|---|---------|\n';

  for (const mod of modules) {
    const head = headJars.get(mod);
    const base = baseJars.get(mod);
    if (head && base && base.size > 0) {
      hasAnyBase = true;
      headTotal += head.size;
      baseTotal += base.size;
      const delta = ((head.size - base.size) / base.size) * 100;
      table += `| ${mod} | ${kb(head.size)} | ${kb(base.size)} | ${formatSigned(delta, '%')} | ${classCell(head, base)} |\n`;
      if (Math.abs(delta) >= 0.05) {
        deltaLabels.push(chartLabel(mod));
        deltaVals.push(delta);
      }
      if (delta > growthThreshold) {
        warnings.push(`${mod} jar ${formatSigned(delta, '%')} (>${growthThreshold}% growth)`);
      }
    } else if (head) {
      membershipChanged = true;
      table += `| ${mod} | ${kb(head.size)} | — | new | ${classCell(head, null)} |\n`;
    } else if (base) {
      hasAnyBase = true;
      membershipChanged = true;
      table += `| ${mod} | — | ${kb(base.size)} | removed | — |\n`;
    }
  }

  const lines = ['### Artifact sizes', ''];
  if (!hasAnyBase) {
    lines.push('_Base JARs unavailable — chart omitted; table has absolute head sizes._', '');
  } else if (deltaLabels.length === 0) {
    lines.push('_No per-module size delta (≥ 0.05%) — chart omitted._', '');
  } else {
    const sorted = sortedDeltas(deltaLabels, deltaVals);
    lines.push(deltaVerticalBars({
      title: 'JAR size delta (%, PR − base)',
      labels: sorted.labels,
      deltas: sorted.values,
      yLabel: 'Δ %',
      color: '#34d399',
    }), '');
  }
  lines.push(detailsTable('Artifact size data table', table), '');

  let verdictPart = null;
  if (hasAnyBase && baseTotal > 0) {
    verdictPart = `📦 jars ${formatSigned(((headTotal - baseTotal) / baseTotal) * 100, '%')}`;
  }

  return { lines, verdictPart, warnings, changed: deltaLabels.length > 0 || membershipChanged };
}

module.exports = { jarSection };
