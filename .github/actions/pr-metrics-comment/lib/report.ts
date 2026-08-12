import { coverageSection } from './sections/coverage-section.js';
import { jarSection } from './sections/jar-section.js';
import { patchSection } from './sections/patch-section.js';
import { apiSection } from './sections/api-section.js';
import { statsSection } from './sections/stats-section.js';
import type { SectionResult } from './sections/format.js';
import type { CoverageData } from './coverage.js';
import type { JarInfo } from './jars.js';
import type { PatchCoverage } from './patch-coverage.js';
import type { ApiSurface } from './api-surface.js';
import type { BuildMetrics } from './metrics-result-validation.js';

export interface ReportLinks {
  readonly run?: string;
  readonly dokka?: string;
  readonly tests?: string;
}

function footer({ headSha, links }: { headSha: string | null; links: ReportLinks }): string[] {
  const parts: string[] = [];
  if (headSha) parts.push(`Updated for \`${headSha}\``);
  if (links.run) parts.push(`[Run](${links.run})`);
  if (links.dokka) parts.push(`[Dokka preview](${links.dokka})`);
  if (links.tests) parts.push(`[Test results](${links.tests})`);
  return parts.length > 0 ? ['---', '', parts.join(' · '), ''] : [];
}

export interface BuildReportOptions {
  headCoverage: CoverageData | null;
  baseCoverage?: CoverageData | null;
  headJars: Map<string, JarInfo>;
  baseJars: Map<string, JarInfo>;
  headMetrics?: BuildMetrics | null;
  baseMetrics?: BuildMetrics | null;
  patchCoverage?: PatchCoverage | null;
  apiSurface?: ApiSurface | null;
  gateThreshold?: number | null;
  growthThreshold: number;
  baseLabel: string;
  headSha?: string | null;
  links?: ReportLinks;
}

export interface BuildReportResult {
  body: string;
  warnings: string[];
}

export function buildReport({
  headCoverage,
  baseCoverage = null,
  headJars,
  baseJars,
  headMetrics = null,
  baseMetrics = null,
  patchCoverage = null,
  apiSurface = null,
  gateThreshold = null,
  growthThreshold,
  baseLabel,
  headSha = null,
  links = {},
}: BuildReportOptions): BuildReportResult {
  const parts: SectionResult[] = [];
  if (headCoverage) parts.push(coverageSection({ headCoverage, baseCoverage, gateThreshold }));
  if (patchCoverage) parts.push(patchSection(patchCoverage));
  if (headJars.size > 0) parts.push(jarSection({ headJars, baseJars, growthThreshold }));
  if (apiSurface) parts.push(apiSection(apiSurface));
  parts.push(statsSection({ headMetrics, baseMetrics }));

  const warnings = parts.flatMap((p) => p.warnings);
  const verdictParts = parts.flatMap((p) => (p.verdictPart ? [p.verdictPart] : []));
  const changed = parts.some((p) => p.changed);
  const hasAnyBase = !!baseCoverage || baseJars.size > 0 || !!baseMetrics;

  const sections = [
    '## CI metrics',
    '',
    `${warnings.length > 0 ? '⚠️' : '✅'} ${verdictParts.join(' · ')}`,
    '',
    `vs **${baseLabel}**`,
    '',
  ];

  if (!changed && hasAnyBase && warnings.length === 0) {
    sections.push('_No metric changes._', '');
    sections.push(...footer({ headSha, links }));
    return { body: sections.join('\n'), warnings };
  }

  for (const part of parts) {
    sections.push(...part.lines);
  }
  if (warnings.length > 0) {
    sections.push('> [!WARNING]');
    for (const warning of warnings) {
      sections.push(`> - ${warning}`);
    }
    sections.push('');
  }
  sections.push(...footer({ headSha, links }));
  return { body: sections.join('\n'), warnings };
}
