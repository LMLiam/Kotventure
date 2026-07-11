'use strict';

const MAIN_SOURCE = /^modules\/[^/]+\/src\/main\/kotlin\/.+\.kt$/;
const MODIFIERS = 'inline|operator|infix|suspend|tailrec|external|abstract|open|final|override|'
  + 'data|sealed|inner|enum|annotation|value|expect|actual|const|companion';
const DECLARATION = new RegExp(
  `^public\\s+(?:(?:${MODIFIERS})\\s+)*(?:fun|val|var|class|interface|object|typealias|constructor)\\b`,
);

function signature(text) {
  const trimmed = text.trim();
  return trimmed.length > 120 ? `${trimmed.slice(0, 117)}…` : trimmed;
}

function declarationsIn(texts) {
  return texts.map((t) => t.trim()).filter((t) => DECLARATION.test(t)).map(signature);
}

function cancelPairs(added, removed) {
  const remaining = [...removed];
  const kept = [];
  for (const sig of added) {
    const idx = remaining.indexOf(sig);
    if (idx >= 0) {
      remaining.splice(idx, 1);
    } else {
      kept.push(sig);
    }
  }
  return { added: kept, removed: remaining };
}

function computeApiSurface(patches) {
  const addedAll = [];
  const removedAll = [];
  for (const patch of patches) {
    if (!MAIN_SOURCE.test(patch.path)) {
      continue;
    }
    addedAll.push(...declarationsIn(patch.addedLines.map((l) => l.text)));
    removedAll.push(...declarationsIn(patch.removedText));
  }
  return cancelPairs(addedAll, removedAll);
}

module.exports = { computeApiSurface };
