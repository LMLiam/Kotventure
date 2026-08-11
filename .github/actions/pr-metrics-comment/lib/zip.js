'use strict';

const yauzl = require('yauzl');

async function countClassEntries(buffer) {
  try {
    const zipfile = await yauzl.fromBufferPromise(buffer);
    let classes = 0;
    for await (const entry of zipfile.eachEntry()) {
      if (entry.fileName.endsWith('.class')) classes += 1;
    }
    return classes;
  } catch {
    return null;
  }
}

module.exports = { countClassEntries };
