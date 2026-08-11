'use strict';

const { DOMParser } = require('@xmldom/xmldom');
const { sanitizeModule } = require('./names.js');

const ELEMENT_NODE = 1;
const LINE_COUNTER = 'LINE';

function moduleFromPackage(pkg) {
  const parts = pkg.split('/');
  const kotIdx = parts.indexOf('kotventure');
  if (kotIdx < 0) return sanitizeModule(parts[0] || 'unknown');
  const after = parts.slice(kotIdx + 1);
  if (after[0] === 'test' && after[1] === 'snapshot') return 'test-snapshot';
  return sanitizeModule(after[0] || 'unknown');
}

function directChildren(parent, tagName) {
  const children = [];
  for (let i = 0; i < parent.childNodes.length; i += 1) {
    const node = parent.childNodes[i];
    if (node.nodeType === ELEMENT_NODE && node.tagName === tagName) {
      children.push(node);
    }
  }
  return children;
}

function lineCounter(container) {
  for (const counter of directChildren(container, 'counter')) {
    if (counter.getAttribute('type') !== LINE_COUNTER) continue;
    return {
      missed: parseInt(counter.getAttribute('missed'), 10),
      covered: parseInt(counter.getAttribute('covered'), 10),
    };
  }
  return null;
}

function sourcefileLines(packageName, packageElement, files) {
  const sourcefiles = packageElement.getElementsByTagName('sourcefile');
  for (let i = 0; i < sourcefiles.length; i += 1) {
    const sourcefile = sourcefiles[i];
    const lines = new Map();
    const lineElements = sourcefile.getElementsByTagName('line');
    for (let j = 0; j < lineElements.length; j += 1) {
      const line = lineElements[j];
      lines.set(
        parseInt(line.getAttribute('nr'), 10),
        parseInt(line.getAttribute('ci'), 10) > 0,
      );
    }
    if (lines.size > 0) {
      files.set(`${packageName}/${sourcefile.getAttribute('name')}`, lines);
    }
  }
}

function parseCoverage(xml) {
  const document = new DOMParser().parseFromString(xml, 'text/xml');
  const root = document.documentElement;
  const modules = new Map();
  const files = new Map();

  if (!root) return { modules, totalMissed: 0, totalCovered: 0, files };

  const packageElements = root.getElementsByTagName('package');
  for (let i = 0; i < packageElements.length; i += 1) {
    const packageElement = packageElements[i];
    const packageName = packageElement.getAttribute('name');
    sourcefileLines(packageName, packageElement, files);
    const counters = lineCounter(packageElement);
    if (!counters) continue;
    const moduleName = moduleFromPackage(packageName);
    if (!modules.has(moduleName)) modules.set(moduleName, { missed: 0, covered: 0 });
    const entry = modules.get(moduleName);
    entry.missed += counters.missed;
    entry.covered += counters.covered;
  }

  const total = lineCounter(root);
  let totalMissed = 0;
  let totalCovered = 0;
  if (total) {
    totalMissed = total.missed;
    totalCovered = total.covered;
  } else {
    for (const data of modules.values()) {
      totalMissed += data.missed;
      totalCovered += data.covered;
    }
  }

  return { modules, totalMissed, totalCovered, files };
}

module.exports = { parseCoverage };
