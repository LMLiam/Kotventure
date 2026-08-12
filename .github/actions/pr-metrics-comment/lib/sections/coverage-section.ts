import { chartLabel } from '../names.js';
import { deltaVerticalBars } from '../mermaid.js';
import { pct, formatPct, formatSigned, detailsTable, sortedDeltas } from './format.js';
import type { SectionResult } from './format.js';
import type { CoverageData } from '../coverage.js';

interface ModuleRowMembership {
  changed: boolean;
}

function moduleRows(
  headCoverage: CoverageData,
  baseCoverage: CoverageData | null,
  deltaLabels: string[],
  deltaVals: number[],
  membership: ModuleRowMembership,
): string {
  const hasBase = !!baseCoverage;
  const names = new Set<string>([...headCoverage.modules.keys()]);
  if (hasBase) {
    for (const name of baseCoverage.modules.keys()) {
      names.add(name);
    }
  }
  let table = hasBase
    ? '| Module | PR | Base | Δ |\n|--------|----|------|---|\n'
    : '| Module | Coverage |\n|--------|----------|\n';
  for (const name of [...names].sort((a, b) => a.localeCompare(b))) {
    const head = headCoverage.modules.get(name);
    const base = hasBase ? baseCoverage.modules.get(name) : undefined;
    const headPct = head ? pct(head.covered, head.missed) : null;
    const basePct = base ? pct(base.covered, base.missed) : null;
    if (headPct != null && basePct != null) {
      const delta = headPct - basePct;
      table += `| ${name} | ${formatPct(headPct)} | ${formatPct(basePct)} | ${formatSigned(delta, 'pp')} |\n`;
      if (Math.abs(delta) >= 0.05) {
        deltaLabels.push(chartLabel(name));
        deltaVals.push(delta);
      }
    } else if (headPct != null) {
      if (hasBase) membership.changed = true;
      table += hasBase
        ? `| ${name} | ${formatPct(headPct)} | — | new |\n`
        : `| ${name} | ${formatPct(headPct)} |\n`;
    } else if (basePct != null) {
      membership.changed = true;
      table += `| ${name} | — | ${formatPct(basePct)} | removed |\n`;
    }
  }
  return table;
}

export interface CoverageSectionOptions {
  headCoverage: CoverageData;
  baseCoverage: CoverageData | null;
  gateThreshold: number | null | undefined;
}

export function coverageSection({ headCoverage, baseCoverage, gateThreshold }: CoverageSectionOptions): SectionResult {
  const hasBase = !!baseCoverage;
  const deltaLabels: string[] = [];
  const deltaVals: number[] = [];
  const membership: ModuleRowMembership = { changed: false };
  let table = moduleRows(headCoverage, baseCoverage, deltaLabels, deltaVals, membership);

  const headTotal = pct(headCoverage.totalCovered, headCoverage.totalMissed);
  const warnings: string[] = [];
  let totalDelta: number | null = null;
  if (hasBase) {
    const baseTotal = pct(baseCoverage.totalCovered, baseCoverage.totalMissed);
    totalDelta = headTotal - baseTotal;
    table += `| **Total** | **${formatPct(headTotal)}** | **${formatPct(baseTotal)}** | **${formatSigned(totalDelta, 'pp')}** |\n`;
    if (totalDelta <= -0.5) {
      warnings.push(`Total coverage dropped ${formatSigned(totalDelta, 'pp')} (${formatPct(headTotal)})`);
    }
  } else {
    table += `| **Total** | **${formatPct(headTotal)}** |\n`;
  }
  if (gateThreshold != null && headTotal - gateThreshold < 0.5) {
    warnings.push(`Coverage ${formatPct(headTotal)} is within 0.5pp of the ${gateThreshold}% gate`);
  }

  const lines = ['### Coverage', ''];
  if (!hasBase) lines.push('_Base coverage unavailable — chart omitted; table has absolute head coverage._', '');
  else if (deltaLabels.length === 0) lines.push('_No per-module coverage delta (≥ 0.05pp) — chart omitted._', '');
  else {
    const sorted = sortedDeltas(deltaLabels, deltaVals);
    const chart = deltaVerticalBars({
      title: 'Coverage delta (pp, PR − base)',
      labels: sorted.labels,
      deltas: sorted.values,
      yLabel: 'Δ pp',
      color: '#a78bfa',
    });
    if (chart) lines.push(chart, '');
  }
  lines.push(detailsTable('Coverage data table', table), '');

  const gatePart = gateThreshold != null ? `gate ${gateThreshold}%` : '';
  const deltaPart = totalDelta != null ? formatSigned(totalDelta, 'pp') : '';
  const parenthetical = [deltaPart, gatePart].filter(Boolean).join(' · ');
  const verdictPart = `Coverage ${formatPct(headTotal)}${parenthetical ? ` (${parenthetical})` : ''}`;

  return {
    lines,
    verdictPart,
    warnings,
    changed: hasBase && (deltaLabels.length > 0 || membership.changed || Math.abs(totalDelta ?? 0) >= 0.05),
  };
}
