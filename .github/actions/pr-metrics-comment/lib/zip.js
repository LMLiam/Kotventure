'use strict';

const EOCD_SIG = 0x06054b50;
const CENTRAL_SIG = 0x02014b50;
const EOCD_MIN_SIZE = 22;
const CENTRAL_HEADER_SIZE = 46;

function findEocd(buffer) {
  const start = Math.max(0, buffer.length - EOCD_MIN_SIZE - 0xffff);
  for (let i = buffer.length - EOCD_MIN_SIZE; i >= start; i--) {
    if (buffer.readUInt32LE(i) !== EOCD_SIG) {
      continue;
    }
    const commentLength = buffer.readUInt16LE(i + 20);
    if (i + EOCD_MIN_SIZE + commentLength === buffer.length) return i;
  }
  return -1;
}

function countClassEntries(buffer) {
  if (buffer.length < EOCD_MIN_SIZE) return null;
  const eocd = findEocd(buffer);
  if (eocd < 0) return null;
  const totalEntries = buffer.readUInt16LE(eocd + 10);
  const cdOffset = buffer.readUInt32LE(eocd + 16);
  let classes = 0;
  let offset = cdOffset;
  for (let entry = 0; entry < totalEntries; entry++) {
    if (offset + CENTRAL_HEADER_SIZE > buffer.length
        || buffer.readUInt32LE(offset) !== CENTRAL_SIG) {
      return null;
    }
    const nameLength = buffer.readUInt16LE(offset + 28);
    const extraLength = buffer.readUInt16LE(offset + 30);
    const commentLength = buffer.readUInt16LE(offset + 32);
    const recordEnd = offset + CENTRAL_HEADER_SIZE + nameLength + extraLength + commentLength;
    if (recordEnd > buffer.length) return null;
    const name = buffer.toString('utf8', offset + CENTRAL_HEADER_SIZE, offset + CENTRAL_HEADER_SIZE + nameLength);
    if (name.endsWith('.class')) {
      classes += 1;
    }
    offset = recordEnd;
  }
  return classes;
}

module.exports = { countClassEntries };
