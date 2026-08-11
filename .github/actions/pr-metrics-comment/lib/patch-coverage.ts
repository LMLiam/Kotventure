import type { ParsedPatch } from './patch.js';

const MAIN_SOURCE = /^modules\/[^/]+\/src\/main\/kotlin\/(.+\.kt)$/;

export interface UncoveredFile {
  readonly path: string;
  readonly ranges: ReadonlyArray<readonly [number, number]>;
}

export interface PatchCoverage {
  readonly covered: number;
  readonly missed: number;
  readonly uncovered: UncoveredFile[];
}

function toRanges(lineNumbers: number[]): Array<[number, number]> {
  const sorted = [...lineNumbers].sort((a, b) => a - b);
  const ranges: Array<[number, number]> = [];
  for (const n of sorted) {
    const last = ranges[ranges.length - 1];
    if (last && n === last[1] + 1) last[1] = n; else ranges.push([n, n]);
  }
  return ranges;
}

export function computePatchCoverage(
  patches: ParsedPatch[],
  coverageFiles: Map<string, Map<number, boolean>>,
): PatchCoverage {
  let covered = 0;
  let missed = 0;
  const uncovered: UncoveredFile[] = [];
  for (const patch of patches) {
    const match = patch.path.match(MAIN_SOURCE);
    if (!match) continue;
    const key = match[1];
    if (!key) continue;
    const lines = coverageFiles.get(key);
    if (!lines) continue;
    const missedLines: number[] = [];
    for (const added of patch.addedLines) {
      const lineCovered = lines.get(added.line);
      if (lineCovered === undefined) continue;
      if (lineCovered) {
        covered += 1;
      } else {
        missed += 1;
        missedLines.push(added.line);
      }
    }
    if (missedLines.length > 0) uncovered.push({ path: patch.path, ranges: toRanges(missedLines) });
  }
  return { covered, missed, uncovered };
}
