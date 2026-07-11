'use strict';

const { formatPct } = require('./format.js');

function formatRanges(ranges) {
  return ranges.map(([start, end]) => (start === end ? `${start}` : `${start}–${end}`)).join(', ');
}

function patchSection(patchCoverage) {
  const total = patchCoverage.covered + patchCoverage.missed;
  if (total === 0) {
    return {
      lines: ['### Patch coverage', '', '_No executable changed lines._', ''],
      verdictPart: null,
      warnings: [],
      changed: false,
    };
  }
  const percentage = (patchCoverage.covered / total) * 100;
  const lines = [
    `### Patch coverage — ${patchCoverage.covered}/${total} changed lines covered (${formatPct(percentage)})`,
    '',
  ];
  if (patchCoverage.uncovered.length > 0) {
    lines.push('<details>', '<summary>Uncovered changed lines</summary>', '');
    for (const file of patchCoverage.uncovered) {
      lines.push(`- \`${file.path}\`: ${formatRanges(file.ranges)}`);
    }
    lines.push('', '</details>', '');
  }
  return {
    lines,
    verdictPart: `patch ${formatPct(percentage)}`,
    warnings: [],
    changed: true,
  };
}

module.exports = { patchSection };
