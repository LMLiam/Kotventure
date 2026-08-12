export function pct(covered: number, missed: number): number {
  const total = covered + missed;
  return total > 0 ? (covered / total) * 100 : 0;
}

export function formatPct(value: number): string {
  return `${value.toFixed(1)}%`;
}

export function formatSigned(value: number, suffix: string): string {
  const sign = value > 0 ? '+' : '';
  return `${sign}${value.toFixed(1)}${suffix}`;
}

export function formatCount(value: number): string {
  const sign = value > 0 ? '+' : '';
  return `${sign}${value}`;
}

export function detailsTable(summary: string, markdownTable: string): string {
  return [
    '<details>',
    `<summary>${summary}</summary>`,
    '',
    markdownTable.trimEnd(),
    '',
    '</details>',
  ].join('\n');
}

export function sortedDeltas(labels: string[], values: number[]): { labels: string[]; values: number[] } {
  const pairs: Array<[string, number]> = labels.map((label, i) => [label, values[i] as number]);
  pairs.sort((a, b) => Math.abs(b[1]) - Math.abs(a[1]));
  return { labels: pairs.map((p) => p[0]), values: pairs.map((p) => p[1]) };
}

export interface SectionResult {
  readonly lines: string[];
  readonly verdictPart: string | null;
  readonly warnings: string[];
  readonly changed: boolean;
}
