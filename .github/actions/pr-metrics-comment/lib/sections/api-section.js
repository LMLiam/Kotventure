'use strict';

function apiSection(apiSurface) {
  const added = apiSurface.added.length;
  const removed = apiSurface.removed.length;
  if (added === 0 && removed === 0) {
    return { lines: [], verdictPart: null, warnings: [], changed: false };
  }
  const lines = [
    `### Public API — +${added} / −${removed} declarations`,
    '',
    '<details>',
    '<summary>Declaration diff (grep heuristic until apiDump lands)</summary>',
    '',
    '```diff',
    ...apiSurface.removed.map((sig) => `- ${sig}`),
    ...apiSurface.added.map((sig) => `+ ${sig}`),
    '```',
    '',
    '</details>',
    '',
  ];
  return {
    lines,
    verdictPart: `API +${added}/−${removed}`,
    warnings: [],
    changed: true,
  };
}

module.exports = { apiSection };
