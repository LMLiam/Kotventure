'use strict';

const HUNK_HEADER = /^@@ -\d+(?:,\d+)? \+(\d+)(?:,\d+)? @@/;

function parsePatch(patch) {
  const addedLines = [];
  const removedText = [];
  let newLine = 0;
  for (const line of patch.split('\n')) {
    const hunk = line.match(HUNK_HEADER);
    if (hunk) {
      newLine = parseInt(hunk[1], 10);
      continue;
    }
    if (line.startsWith('+')) {
      addedLines.push({ line: newLine, text: line.slice(1) });
      newLine += 1;
    } else if (line.startsWith('-')) {
      removedText.push(line.slice(1));
    } else if (!line.startsWith('\\')) {
      newLine += 1;
    }
  }
  return { addedLines, removedText };
}

function parsePatches(files) {
  const parsed = [];
  for (const file of files) {
    if (!file.patch || file.status === 'removed') {
      continue;
    }
    parsed.push({ path: file.filename, ...parsePatch(file.patch) });
  }
  return parsed;
}

module.exports = { parsePatches };
