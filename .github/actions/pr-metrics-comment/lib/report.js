'use strict';

const { chartLabel } = require('./names.js');
const { deltaVerticalBars } = require('./mermaid.js');

function pct(covered, missed) {
  const total = covered + missed;
  return total > 0 ? (covered / total) * 100 : 0;
}

function formatPct(value) {
  return `${value.toFixed(1)}%`;
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

function coverageSection(headCoverage, baseCoverage) {
  const hasBase = !!baseCoverage;
  const names = new Set([...headCoverage.modules.keys()]);
  if (hasBase) {
    for (const name of baseCoverage.modules.keys()) {
      names.add(name);
    }
  }
  const ordered = [...names].sort((a, b) => a.localeCompare(b));
  const deltaLabels = [];
  const deltaVals = [];
  let table = hasBase
    ? '| Module | PR | Base | Δ |\n|--------|----|------|---|\n'
    : '| Module | Coverage |\n|--------|----------|\n';

  for (const name of ordered) {
    const head = headCoverage.modules.get(name);
    const base = hasBase ? baseCoverage.modules.get(name) : null;
    const headPct = head ? pct(head.covered, head.missed) : null;
    const basePct = base ? pct(base.covered, base.missed) : null;
    if (headPct == null && basePct == null) {
      continue;
    }
    if (headPct != null && basePct != null) {
      const delta = headPct - basePct;
      const sign = delta > 0 ? '+' : '';
      table += `| ${name} | ${formatPct(headPct)} | ${formatPct(basePct)} | ${sign}${delta.toFixed(1)}pp |\n`;
      if (Math.abs(delta) >= 0.05) {
        deltaLabels.push(chartLabel(name));
        deltaVals.push(delta);
      }
    } else if (headPct != null) {
      table += hasBase
        ? `| ${name} | ${formatPct(headPct)} | — | new |\n`
        : `| ${name} | ${formatPct(headPct)} |\n`;
    } else {
      table += `| ${name} | — | ${formatPct(basePct)} | removed |\n`;
    }
  }

  const headTotal = pct(headCoverage.totalCovered, headCoverage.totalMissed);
  if (hasBase) {
    const baseTotal = pct(baseCoverage.totalCovered, baseCoverage.totalMissed);
    const delta = headTotal - baseTotal;
    const sign = delta > 0 ? '+' : '';
    table += `| **Total** | **${formatPct(headTotal)}** | **${formatPct(baseTotal)}** | **${sign}${delta.toFixed(1)}pp** |\n`;
  } else {
    table += `| **Total** | **${formatPct(headTotal)}** |\n`;
  }

  const lines = ['### Coverage', ''];
  if (!hasBase) {
    lines.push('_Base coverage unavailable — chart omitted; table has absolute head coverage._', '');
  } else if (deltaLabels.length === 0) {
    lines.push('_No per-module coverage delta (≥ 0.05pp) — chart omitted._', '');
  } else {
    lines.push(deltaVerticalBars({
      title: 'Coverage delta (pp, PR − base)',
      labels: deltaLabels,
      deltas: deltaVals,
      yLabel: 'Δ pp',
      color: '#a78bfa',
    }), '');
  }
  lines.push(detailsTable('Coverage data table', table), '');
  return lines;
}

function jarSection(headJars, baseJars, growthThreshold) {
  const warnings = [];
  const modules = [...new Set([...headJars.keys(), ...baseJars.keys()])].sort((a, b) => a.localeCompare(b));
  const deltaLabels = [];
  const deltaVals = [];
  let hasAnyBase = false;
  let table = '| Module | PR | Base | Delta |\n|--------|----|------|-------|\n';

  for (const mod of modules) {
    const size = headJars.get(mod);
    const baseSize = baseJars.get(mod);
    if (typeof size === 'number' && typeof baseSize === 'number' && baseSize > 0) {
      hasAnyBase = true;
      const h = size / 1024;
      const b = baseSize / 1024;
      const delta = ((size - baseSize) / baseSize) * 100;
      const sign = delta > 0 ? '+' : '';
      table += `| ${mod} | ${h.toFixed(1)} KB | ${b.toFixed(1)} KB | ${sign}${delta.toFixed(1)}% |\n`;
      if (Math.abs(delta) >= 0.05) {
        deltaLabels.push(chartLabel(mod));
        deltaVals.push(delta);
      }
      if (delta > growthThreshold) {
        warnings.push(`${mod}: ${sign}${delta.toFixed(1)}%`);
      }
    } else if (typeof size === 'number') {
      table += `| ${mod} | ${(size / 1024).toFixed(1)} KB | — | new |\n`;
    } else if (typeof baseSize === 'number') {
      hasAnyBase = true;
      table += `| ${mod} | — | ${(baseSize / 1024).toFixed(1)} KB | removed |\n`;
    }
  }

  const lines = ['### Artifact sizes', ''];
  if (!hasAnyBase) {
    lines.push('_Base JARs unavailable — chart omitted; table has absolute head sizes._', '');
  } else if (deltaLabels.length === 0) {
    lines.push('_No per-module size delta (≥ 0.05%) — chart omitted._', '');
  } else {
    lines.push(deltaVerticalBars({
      title: 'JAR size delta (%, PR − base)',
      labels: deltaLabels,
      deltas: deltaVals,
      yLabel: 'Δ %',
      color: '#34d399',
    }), '');
  }
  lines.push(detailsTable('Artifact size data table', table), '');

  if (warnings.length > 0) {
    lines.push('> [!WARNING]');
    lines.push(`> Modules exceeding ${growthThreshold}% JAR growth:`);
    for (const w of warnings) {
      lines.push(`> - ${w}`);
    }
    lines.push('');
  }
  return { lines, warnings };
}

function buildReport({ headCoverage, baseCoverage, headJars, baseJars, growthThreshold, baseLabel }) {
  const sections = [
    '## CI metrics',
    '',
    `vs **${baseLabel}**`,
    '',
    'Charts show **delta only** (PR − base). Omitted when nothing changed.',
    '',
  ];
  let warnings = [];

  if (headCoverage) {
    sections.push(...coverageSection(headCoverage, baseCoverage));
  }
  if (headJars.size > 0) {
    const jar = jarSection(headJars, baseJars, growthThreshold);
    sections.push(...jar.lines);
    warnings = jar.warnings;
  }

  return { body: sections.join('\n'), warnings };
}

module.exports = { buildReport };
