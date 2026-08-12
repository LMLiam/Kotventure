import { chartLabel } from '../names.js';
import { deltaVerticalBars } from '../mermaid.js';
import { formatSigned, formatCount, detailsTable, sortedDeltas } from './format.js';
import type { SectionResult } from './format.js';
import type { JarInfo } from '../jars.js';

function kb(bytes: number): string {
  return `${(bytes / 1024).toFixed(1)} KB`;
}

function classCell(head: JarInfo | undefined, base: JarInfo | null | undefined): string {
  if (head?.classes == null) return '—';
  if (base?.classes != null && base.classes !== head.classes) {
    return `${head.classes} (${formatCount(head.classes - base.classes)})`;
  }
  return `${head.classes}`;
}

export interface JarSectionOptions {
  headJars: Map<string, JarInfo>;
  baseJars: Map<string, JarInfo>;
  growthThreshold: number;
}

export function jarSection({ headJars, baseJars, growthThreshold }: JarSectionOptions): SectionResult {
  const warnings: string[] = [];
  const modules = [...new Set([...headJars.keys(), ...baseJars.keys()])].sort((a, b) => a.localeCompare(b));
  const deltaLabels: string[] = [];
  const deltaVals: number[] = [];
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
      table += `| ${mod} | ${kb(head.size)} | — | new | ${classCell(head, undefined)} |\n`;
    } else if (base) {
      hasAnyBase = true;
      membershipChanged = true;
      table += `| ${mod} | — | ${kb(base.size)} | removed | — |\n`;
    }
  }

  const lines = ['### Artifact sizes', ''];
  if (!hasAnyBase) lines.push('_Base JARs unavailable — chart omitted; table has absolute head sizes._', '');
  else if (deltaLabels.length === 0) lines.push('_No per-module size delta (≥ 0.05%) — chart omitted._', '');
  else {
    const sorted = sortedDeltas(deltaLabels, deltaVals);
    const chart = deltaVerticalBars({
      title: 'JAR size delta (%, PR − base)',
      labels: sorted.labels,
      deltas: sorted.values,
      yLabel: 'Δ %',
      color: '#34d399',
    });
    if (chart) lines.push(chart, '');
  }
  lines.push(detailsTable('Artifact size data table', table), '');

  let verdictPart: string | null = null;
  if (hasAnyBase && baseTotal > 0) {
    verdictPart = `📦 jars ${formatSigned(((headTotal - baseTotal) / baseTotal) * 100, '%')}`;
  }

  return { lines, verdictPart, warnings, changed: deltaLabels.length > 0 || membershipChanged };
}
