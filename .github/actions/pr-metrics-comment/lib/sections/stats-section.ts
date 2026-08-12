import { formatCount, detailsTable } from './format.js';
import type { SectionResult } from './format.js';
import type { BuildMetrics } from '../metrics-result-validation.js';

function formatDuration(seconds: number | null | undefined): string {
  if (seconds == null) return '—';
  const minutes = Math.floor(seconds / 60);
  return minutes > 0 ? `${minutes}m${String(seconds % 60).padStart(2, '0')}s` : `${seconds}s`;
}

export interface StatsSectionOptions {
  headMetrics: BuildMetrics | null;
  baseMetrics: BuildMetrics | null;
}

export function statsSection({ headMetrics, baseMetrics }: StatsSectionOptions): SectionResult {
  if (!headMetrics) return { lines: [], verdictPart: null, warnings: [], changed: false };
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
