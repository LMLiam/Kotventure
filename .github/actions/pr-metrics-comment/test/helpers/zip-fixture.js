'use strict';

const AdmZip = require('adm-zip');

function buildZip(names) {
  const zip = new AdmZip();
  for (const name of names) {
    zip.addFile(name, Buffer.alloc(0));
  }
  return zip.toBuffer();
}

module.exports = { buildZip };
