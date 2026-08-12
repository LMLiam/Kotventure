import { parsePatch } from 'diff';
import type { PullRequestFile } from '../../../scripts/shared/action-context.js';

export type { PullRequestFile };

export interface AddedLine {
  readonly line: number;
  readonly text: string;
}

export interface ParsedPatch {
  readonly path: string;
  readonly addedLines: AddedLine[];
  readonly removedText: string[];
}

function parseHunks(patch: string): { addedLines: AddedLine[]; removedText: string[] } {
  const addedLines: AddedLine[] = [];
  const removedText: string[] = [];
  for (const file of parsePatch(patch)) {
    for (const hunk of file.hunks) {
      let newLine = hunk.newStart;
      for (const line of hunk.lines) {
        if (line.startsWith('+')) {
          addedLines.push({ line: newLine, text: line.slice(1) });
          newLine += 1;
        } else if (line.startsWith('-')) {
          removedText.push(line.slice(1));
        } else if (!line.startsWith('\\')) {
          newLine += 1;
        }
      }
    }
  }
  return { addedLines, removedText };
}

export function parsePatches(files: PullRequestFile[]): ParsedPatch[] {
  const parsed: ParsedPatch[] = [];
  for (const file of files) {
    if (!file.patch || file.status === 'removed') continue;
    parsed.push({ path: file.filename, ...parseHunks(file.patch) });
  }
  return parsed;
}
