'use strict';

function centralEntry(name) {
  const nameBytes = Buffer.from(name, 'utf8');
  const header = Buffer.alloc(46);
  header.writeUInt32LE(0x02014b50, 0);
  header.writeUInt16LE(nameBytes.length, 28);
  return Buffer.concat([header, nameBytes]);
}

function buildZip(names) {
  const localPart = Buffer.alloc(10, 1);
  const centralDirectory = Buffer.concat(names.map(centralEntry));
  const eocd = Buffer.alloc(22);
  eocd.writeUInt32LE(0x06054b50, 0);
  eocd.writeUInt16LE(names.length, 8);
  eocd.writeUInt16LE(names.length, 10);
  eocd.writeUInt32LE(centralDirectory.length, 12);
  eocd.writeUInt32LE(localPart.length, 16);
  return Buffer.concat([localPart, centralDirectory, eocd]);
}

module.exports = { buildZip };
