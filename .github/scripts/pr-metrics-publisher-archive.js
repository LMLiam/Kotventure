'use strict';

const zlib = require('zlib');
const { MAX_RESULT_BYTES } = require('../actions/pr-metrics-comment/lib/metrics-result-contract.js');
const {
  MAX_ARTIFACT_BYTES,
  RESULT_FILE_NAME,
} = require('./pr-metrics-publisher-contract.js');

const END_OF_CENTRAL_DIRECTORY_SIGNATURE = 0x06054b50;
const CENTRAL_DIRECTORY_SIGNATURE = 0x02014b50;
const LOCAL_FILE_SIGNATURE = 0x04034b50;
const ZIP64_VALUE = 0xffff;
const ZIP64_SIZE = 0xffffffff;
const END_OF_CENTRAL_DIRECTORY_LENGTH = 22;
const MAX_ZIP_COMMENT_LENGTH = 0xffff;

function reject(message) {
  throw new Error(`metrics artifact archive ${message}`);
}

function requireRange(archive, offset, length, label) {
  if (!Number.isSafeInteger(offset)
    || !Number.isSafeInteger(length)
    || offset < 0
    || length < 0
    || offset + length > archive.length) {
    reject(`${label} is outside the archive`);
  }
}

function findEndOfCentralDirectory(archive) {
  const firstOffset = Math.max(0, archive.length - END_OF_CENTRAL_DIRECTORY_LENGTH - MAX_ZIP_COMMENT_LENGTH);
  for (let offset = archive.length - END_OF_CENTRAL_DIRECTORY_LENGTH; offset >= firstOffset; offset -= 1) {
    if (archive.readUInt32LE(offset) === END_OF_CENTRAL_DIRECTORY_SIGNATURE) {
      const commentLength = archive.readUInt16LE(offset + 20);
      if (offset + END_OF_CENTRAL_DIRECTORY_LENGTH + commentLength === archive.length) {
        return offset;
      }
    }
  }
  reject('has no valid end record');
}

function readEntryMetadata(archive) {
  const endOffset = findEndOfCentralDirectory(archive);
  const diskNumber = archive.readUInt16LE(endOffset + 4);
  const centralDirectoryDisk = archive.readUInt16LE(endOffset + 6);
  const entriesOnDisk = archive.readUInt16LE(endOffset + 8);
  const totalEntries = archive.readUInt16LE(endOffset + 10);
  const centralDirectorySize = archive.readUInt32LE(endOffset + 12);
  const centralDirectoryOffset = archive.readUInt32LE(endOffset + 16);

  if (diskNumber !== 0 || centralDirectoryDisk !== 0
    || entriesOnDisk === ZIP64_VALUE || totalEntries === ZIP64_VALUE
    || centralDirectorySize === ZIP64_SIZE || centralDirectoryOffset === ZIP64_SIZE) {
    reject('uses unsupported ZIP64 or multi-disk metadata');
  }
  if (totalEntries !== 1 || entriesOnDisk !== 1) {
    reject('must contain exactly one file');
  }
  requireRange(archive, centralDirectoryOffset, centralDirectorySize, 'central directory');
  if (centralDirectoryOffset + centralDirectorySize !== endOffset) {
    reject('has a misplaced central directory');
  }
  if (centralDirectorySize < 46
    || archive.readUInt32LE(centralDirectoryOffset) !== CENTRAL_DIRECTORY_SIGNATURE) {
    reject('has an invalid central directory');
  }

  const fileNameLength = archive.readUInt16LE(centralDirectoryOffset + 28);
  const extraLength = archive.readUInt16LE(centralDirectoryOffset + 30);
  const commentLength = archive.readUInt16LE(centralDirectoryOffset + 32);
  const recordLength = 46 + fileNameLength + extraLength + commentLength;
  if (recordLength !== centralDirectorySize) {
    reject('has an invalid central directory record');
  }
  const fileNameOffset = centralDirectoryOffset + 46;
  requireRange(archive, fileNameOffset, fileNameLength, 'file name');
  const fileName = archive.subarray(fileNameOffset, fileNameOffset + fileNameLength);
  if (!fileName.equals(Buffer.from(RESULT_FILE_NAME, 'utf8'))) {
    reject(`must contain only ${RESULT_FILE_NAME}`);
  }

  const flags = archive.readUInt16LE(centralDirectoryOffset + 8);
  const compressionMethod = archive.readUInt16LE(centralDirectoryOffset + 10);
  const compressedSize = archive.readUInt32LE(centralDirectoryOffset + 20);
  const uncompressedSize = archive.readUInt32LE(centralDirectoryOffset + 24);
  const localHeaderOffset = archive.readUInt32LE(centralDirectoryOffset + 42);
  if ((flags & 0x1) !== 0
    || compressionMethod !== 0 && compressionMethod !== 8
    || compressedSize === ZIP64_SIZE
    || uncompressedSize === ZIP64_SIZE
    || localHeaderOffset === ZIP64_SIZE) {
    reject('uses unsupported compression or metadata');
  }
  if (compressedSize < 1 || compressedSize > archive.length || uncompressedSize < 1
    || uncompressedSize > MAX_RESULT_BYTES) {
    reject('contains an entry outside the size limit');
  }

  return {
    flags,
    compressionMethod,
    compressedSize,
    uncompressedSize,
    localHeaderOffset,
    fileName,
  };
}

function extractMetricsResultArchive(archive) {
  if (!Buffer.isBuffer(archive)
    || archive.length < END_OF_CENTRAL_DIRECTORY_LENGTH
    || archive.length > MAX_ARTIFACT_BYTES) {
    reject('is not a ZIP archive');
  }
  const metadata = readEntryMetadata(archive);
  requireRange(archive, metadata.localHeaderOffset, 30, 'local file header');
  if (archive.readUInt32LE(metadata.localHeaderOffset) !== LOCAL_FILE_SIGNATURE) {
    reject('has an invalid local file header');
  }

  const localFlags = archive.readUInt16LE(metadata.localHeaderOffset + 6);
  const localCompressionMethod = archive.readUInt16LE(metadata.localHeaderOffset + 8);
  const localFileNameLength = archive.readUInt16LE(metadata.localHeaderOffset + 26);
  const localExtraLength = archive.readUInt16LE(metadata.localHeaderOffset + 28);
  if (localFlags !== metadata.flags || localCompressionMethod !== metadata.compressionMethod) {
    reject('has inconsistent file metadata');
  }
  const localFileNameOffset = metadata.localHeaderOffset + 30;
  requireRange(archive, localFileNameOffset, localFileNameLength + localExtraLength, 'local file name');
  const localFileName = archive.subarray(localFileNameOffset, localFileNameOffset + localFileNameLength);
  if (!localFileName.equals(metadata.fileName)) {
    reject('has inconsistent file names');
  }

  const compressedDataOffset = localFileNameOffset + localFileNameLength + localExtraLength;
  requireRange(archive, compressedDataOffset, metadata.compressedSize, 'compressed data');
  const compressedData = archive.subarray(
    compressedDataOffset,
    compressedDataOffset + metadata.compressedSize,
  );
  let result;
  try {
    result = metadata.compressionMethod === 0
      ? Buffer.from(compressedData)
      : zlib.inflateRawSync(compressedData, { maxOutputLength: MAX_RESULT_BYTES });
  } catch (error) {
    reject(`cannot be decompressed: ${error.message}`);
  }
  if (result.length !== metadata.uncompressedSize || result.length > MAX_RESULT_BYTES) {
    reject('decompresses to an invalid size');
  }
  return result;
}

module.exports = { extractMetricsResultArchive };
