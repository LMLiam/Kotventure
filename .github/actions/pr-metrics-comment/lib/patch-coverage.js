'use strict';

const MAIN_SOURCE = /^modules\/[^/]+\/src\/main\/kotlin\/(.+\.kt)$/;

function toRanges(lineNumbers) {
  const sorted = [...lineNumbers].sort((a, b) => a - b);
  const ranges = [];
  for (const n of sorted) {
    const last = ranges[ranges.length - 1];
    if (last && n === last[1] + 1) {
      last[1] = n;
    } else {
      ranges.push([n, n]);
    }
  }
  return ranges;
}

function computePatchCoverage(patches, coverageFiles) {
  let covered = 0;
  let missed = 0;
  const uncovered = [];
  for (const patch of patches) {
    const match = patch.path.match(MAIN_SOURCE);
    if (!match) {
      continue;
    }
    const lines = coverageFiles.get(match[1]);
    if (!lines) {
      continue;
    }
    const missedLines = [];
    for (const added of patch.addedLines) {
      const lineCovered = lines.get(added.line);
      if (lineCovered === undefined) {
        continue;
      }
      if (lineCovered) {
        covered += 1;
      } else {
        missed += 1;
        missedLines.push(added.line);
      }
    }
    if (missedLines.length > 0) {
      uncovered.push({ path: patch.path, ranges: toRanges(missedLines) });
    }
  }
  return { covered, missed, uncovered };
}

module.exports = { computePatchCoverage };
