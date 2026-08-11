'use strict';

const { parsePatch } = require('diff');

function parseHunks(patch) {
  const addedLines = [];
  const removedText = [];
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

function parsePatches(files) {
  const parsed = [];
  for (const file of files) {
    if (!file.patch || file.status === 'removed') continue;
    parsed.push({ path: file.filename, ...parseHunks(file.patch) });
  }
  return parsed;
}

module.exports = { parsePatches };
