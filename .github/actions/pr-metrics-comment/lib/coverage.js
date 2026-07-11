'use strict';

const { sanitizeModule } = require('./names.js');

function moduleFromPackage(pkg) {
  const parts = pkg.split('/');
  const kotIdx = parts.indexOf('kotventure');
  if (kotIdx < 0) {
    return sanitizeModule(parts[0] || 'unknown');
  }
  const after = parts.slice(kotIdx + 1);
  if (after[0] === 'test' && after[1] === 'snapshot') {
    return 'test-snapshot';
  }
  return sanitizeModule(after[0] || 'unknown');
}

function packageLineCounter(packageBody) {
  const stripped = packageBody
    .replace(/<class[\s\S]*?<\/class>/g, '')
    .replace(/<sourcefile[\s\S]*?<\/sourcefile>/g, '');
  const match = stripped.match(/<counter type="LINE" missed="(\d+)" covered="(\d+)"\/>/);
  if (!match) {
    return null;
  }
  return { missed: parseInt(match[1], 10), covered: parseInt(match[2], 10) };
}

function parseCoverage(xml) {
  const modules = new Map();
  const packageRegex = /<package name="([^"]+)">([\s\S]*?)<\/package>/g;
  let packageMatch;
  while ((packageMatch = packageRegex.exec(xml)) !== null) {
    const counters = packageLineCounter(packageMatch[2]);
    if (!counters) {
      continue;
    }
    const moduleName = moduleFromPackage(packageMatch[1]);
    if (!modules.has(moduleName)) {
      modules.set(moduleName, { missed: 0, covered: 0 });
    }
    const entry = modules.get(moduleName);
    entry.missed += counters.missed;
    entry.covered += counters.covered;
  }
  const rootXml = xml.replace(/<package[\s\S]*?<\/package>/g, '');
  const totalMatch = rootXml.match(/<counter type="LINE" missed="(\d+)" covered="(\d+)"\/>/);
  let totalMissed = 0;
  let totalCovered = 0;
  if (totalMatch) {
    totalMissed = parseInt(totalMatch[1], 10);
    totalCovered = parseInt(totalMatch[2], 10);
  } else {
    for (const data of modules.values()) {
      totalMissed += data.missed;
      totalCovered += data.covered;
    }
  }
  return { modules, totalMissed, totalCovered };
}

module.exports = { parseCoverage };
