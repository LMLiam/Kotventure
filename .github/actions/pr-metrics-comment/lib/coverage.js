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

function sourcefileLines(packageName, packageBody, files) {
  const sourcefileRegex = /<sourcefile name="([^"]+)">([\s\S]*?)<\/sourcefile>/g;
  const lineRegex = /<line nr="(\d+)" mi="(\d+)" ci="(\d+)"/g;
  let sourcefileMatch;
  while ((sourcefileMatch = sourcefileRegex.exec(packageBody)) !== null) {
    const lines = new Map();
    let lineMatch;
    while ((lineMatch = lineRegex.exec(sourcefileMatch[2])) !== null) {
      lines.set(parseInt(lineMatch[1], 10), parseInt(lineMatch[3], 10) > 0);
    }
    if (lines.size > 0) {
      files.set(`${packageName}/${sourcefileMatch[1]}`, lines);
    }
  }
}

function parseCoverage(xml) {
  const modules = new Map();
  const files = new Map();
  const packageRegex = /<package name="([^"]+)">([\s\S]*?)<\/package>/g;
  let packageMatch;
  while ((packageMatch = packageRegex.exec(xml)) !== null) {
    sourcefileLines(packageMatch[1], packageMatch[2], files);
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
  return { modules, totalMissed, totalCovered, files };
}

module.exports = { parseCoverage };
