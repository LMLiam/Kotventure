'use strict';

const { coverageSection } = require('./sections/coverage-section.js');
const { jarSection } = require('./sections/jar-section.js');
const { patchSection } = require('./sections/patch-section.js');
const { apiSection } = require('./sections/api-section.js');
const { statsSection } = require('./sections/stats-section.js');

function footer({ headSha, links }) {
  const parts = [];
  if (headSha) {
    parts.push(`Updated for \`${headSha}\``);
  }
  if (links?.run) {
    parts.push(`[Run](${links.run})`);
  }
  if (links?.dokka) {
    parts.push(`[Dokka preview](${links.dokka})`);
  }
  if (links?.tests) {
    parts.push(`[Test results](${links.tests})`);
  }
  return parts.length > 0 ? ['---', '', parts.join(' · '), ''] : [];
}

function buildReport({
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
}) {
  const parts = [];
  if (headCoverage) {
    parts.push(coverageSection({ headCoverage, baseCoverage, gateThreshold }));
  }
  if (patchCoverage) {
    parts.push(patchSection(patchCoverage));
  }
  if (headJars.size > 0) {
    parts.push(jarSection({ headJars, baseJars, growthThreshold }));
  }
  if (apiSurface) {
    parts.push(apiSection(apiSurface));
  }
  parts.push(statsSection({ headMetrics, baseMetrics }));

  const warnings = parts.flatMap((p) => p.warnings);
  const verdictParts = parts.map((p) => p.verdictPart).filter(Boolean);
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

module.exports = { buildReport };
