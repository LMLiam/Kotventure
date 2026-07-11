'use strict';

const fs = require('fs');
const path = require('path');
const { sanitizeModule } = require('./names.js');
const { countClassEntries } = require('./zip.js');

function parseModuleJar(filename) {
  if (!filename.endsWith('.jar') || filename.includes('-sources') || filename.includes('-javadoc')) {
    return null;
  }
  const match = filename.match(/^kotventure-(.+)-(\d+\.\d+\.\d+(?:[-+][A-Za-z0-9.]+)?)\.jar$/);
  if (!match) {
    return null;
  }
  return { module: sanitizeModule(match[1]), version: match[2] };
}

function versionKey(version) {
  return version.split(/[.+-]/).map((part) => {
    const n = Number.parseInt(part, 10);
    return Number.isFinite(n) ? n.toString().padStart(8, '0') : part;
  }).join('.');
}

function collectJars(rootDir) {
  const sizes = new Map();
  const bestVersion = new Map();
  if (!fs.existsSync(rootDir)) {
    return sizes;
  }
  function walk(dir) {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        walk(full);
        continue;
      }
      if (!entry.isFile()) {
        continue;
      }
      const parsed = parseModuleJar(entry.name);
      if (!parsed) {
        continue;
      }
      const prev = bestVersion.get(parsed.module);
      if (!prev || versionKey(parsed.version) > versionKey(prev.version)) {
        const size = fs.statSync(full).size;
        const classes = countClassEntries(fs.readFileSync(full));
        bestVersion.set(parsed.module, { version: parsed.version });
        sizes.set(parsed.module, { size, classes });
      }
    }
  }
  walk(rootDir);
  return sizes;
}

module.exports = { collectJars };
