'use strict';

const zlib = require('node:zlib');

const END_OF_CENTRAL_DIRECTORY_SIGNATURE = 0x06054b50;
const CENTRAL_DIRECTORY_SIGNATURE = 0x02014b50;
const LOCAL_FILE_SIGNATURE = 0x04034b50;

const END_OF_CENTRAL_DIRECTORY_LENGTH = 22;
const CENTRAL_DIRECTORY_HEADER_LENGTH = 46;
const LOCAL_FILE_HEADER_LENGTH = 30;
const MAX_ZIP_COMMENT_LENGTH = 0xffff;

const ZIP64_16_BIT_VALUE = 0xffff;
const ZIP64_32_BIT_VALUE = 0xffffffff;
const ENCRYPTED_FLAG = 0x0001;
const DATA_DESCRIPTOR_FLAG = 0x0008;

const STORED_COMPRESSION = 0;
const DEFLATE_COMPRESSION = 8;

function extractSingleEntryArchive(archive, {
  errorPrefix,
  expectedFileName,
  maxArchiveBytes,
  maxBytes,
}) {
  const expectedName = Buffer.isBuffer(expectedFileName)
    ? expectedFileName
    : Buffer.from(expectedFileName, 'utf8');

  function rejectArchive(message) {
    throw new Error(`${errorPrefix} ${message}`);
  }

  function assertRange(value, offset, length, label) {
    if (!Number.isSafeInteger(offset)
      || !Number.isSafeInteger(length)
      || offset < 0
      || length < 0
      || offset + length > value.length) {
      rejectArchive(`${label} is outside the archive`);
    }
  }

  function findEndOfCentralDirectory() {
    const firstOffset = Math.max(
      0,
      archive.length - END_OF_CENTRAL_DIRECTORY_LENGTH - MAX_ZIP_COMMENT_LENGTH,
    );

    for (
      let offset = archive.length - END_OF_CENTRAL_DIRECTORY_LENGTH;
      offset >= firstOffset;
      offset -= 1
    ) {
      if (archive.readUInt32LE(offset) !== END_OF_CENTRAL_DIRECTORY_SIGNATURE) {
        continue;
      }

      const commentLength = archive.readUInt16LE(offset + 20);

      if (offset + END_OF_CENTRAL_DIRECTORY_LENGTH + commentLength === archive.length) {
        return offset;
      }
    }

    rejectArchive('has no valid end record');
  }

  function readCentralDirectoryEntry() {
    const endOffset = findEndOfCentralDirectory();

    const diskNumber = archive.readUInt16LE(endOffset + 4);
    const centralDirectoryDisk = archive.readUInt16LE(endOffset + 6);
    const entriesOnDisk = archive.readUInt16LE(endOffset + 8);
    const totalEntries = archive.readUInt16LE(endOffset + 10);
    const centralDirectorySize = archive.readUInt32LE(endOffset + 12);
    const centralDirectoryOffset = archive.readUInt32LE(endOffset + 16);

    const usesZip64 = entriesOnDisk === ZIP64_16_BIT_VALUE
      || totalEntries === ZIP64_16_BIT_VALUE
      || centralDirectorySize === ZIP64_32_BIT_VALUE
      || centralDirectoryOffset === ZIP64_32_BIT_VALUE;

    if (diskNumber !== 0 || centralDirectoryDisk !== 0 || usesZip64) {
      rejectArchive('uses unsupported ZIP64 or multi-disk metadata');
    }

    if (entriesOnDisk !== 1 || totalEntries !== 1) {
      rejectArchive('must contain exactly one file');
    }

    assertRange(archive, centralDirectoryOffset, centralDirectorySize, 'central directory');

    if (centralDirectoryOffset + centralDirectorySize !== endOffset) {
      rejectArchive('has a misplaced central directory');
    }

    if (centralDirectorySize < CENTRAL_DIRECTORY_HEADER_LENGTH
      || archive.readUInt32LE(centralDirectoryOffset) !== CENTRAL_DIRECTORY_SIGNATURE) {
      rejectArchive('has an invalid central directory');
    }

    const fileNameLength = archive.readUInt16LE(centralDirectoryOffset + 28);
    const extraLength = archive.readUInt16LE(centralDirectoryOffset + 30);
    const commentLength = archive.readUInt16LE(centralDirectoryOffset + 32);
    const recordLength = CENTRAL_DIRECTORY_HEADER_LENGTH
      + fileNameLength
      + extraLength
      + commentLength;

    if (recordLength !== centralDirectorySize) {
      rejectArchive('has an invalid central directory record');
    }

    const fileNameOffset = centralDirectoryOffset + CENTRAL_DIRECTORY_HEADER_LENGTH;
    assertRange(archive, fileNameOffset, fileNameLength, 'file name');

    const fileName = archive.subarray(fileNameOffset, fileNameOffset + fileNameLength);

    if (!fileName.equals(expectedName)) {
      rejectArchive(`must contain only ${expectedName.toString('utf8')}`);
    }

    const flags = archive.readUInt16LE(centralDirectoryOffset + 8);
    const compressionMethod = archive.readUInt16LE(centralDirectoryOffset + 10);
    const compressedSize = archive.readUInt32LE(centralDirectoryOffset + 20);
    const uncompressedSize = archive.readUInt32LE(centralDirectoryOffset + 24);
    const diskStart = archive.readUInt16LE(centralDirectoryOffset + 34);
    const localHeaderOffset = archive.readUInt32LE(centralDirectoryOffset + 42);

    const unsupportedCompression = compressionMethod !== STORED_COMPRESSION
      && compressionMethod !== DEFLATE_COMPRESSION;

    const usesZip64Entry = compressedSize === ZIP64_32_BIT_VALUE
      || uncompressedSize === ZIP64_32_BIT_VALUE
      || localHeaderOffset === ZIP64_32_BIT_VALUE;

    if ((flags & ENCRYPTED_FLAG) !== 0
      || unsupportedCompression
      || usesZip64Entry
      || diskStart !== 0) {
      rejectArchive('uses unsupported compression or metadata');
    }

    if (compressedSize < 1
      || compressedSize > archive.length
      || uncompressedSize < 1
      || uncompressedSize > maxBytes) {
      rejectArchive('contains an entry outside the size limit');
    }

    if (localHeaderOffset >= centralDirectoryOffset) {
      rejectArchive('has an invalid local file header offset');
    }

    return {
      centralDirectoryOffset,
      compressedSize,
      compressionMethod,
      fileName,
      flags,
      localHeaderOffset,
      uncompressedSize,
    };
  }

  function readCompressedData(entry) {
    assertRange(archive, entry.localHeaderOffset, LOCAL_FILE_HEADER_LENGTH, 'local file header');

    if (archive.readUInt32LE(entry.localHeaderOffset) !== LOCAL_FILE_SIGNATURE) {
      rejectArchive('has an invalid local file header');
    }

    const localFlags = archive.readUInt16LE(entry.localHeaderOffset + 6);
    const localCompressionMethod = archive.readUInt16LE(entry.localHeaderOffset + 8);
    const localCompressedSize = archive.readUInt32LE(entry.localHeaderOffset + 18);
    const localUncompressedSize = archive.readUInt32LE(entry.localHeaderOffset + 22);
    const localFileNameLength = archive.readUInt16LE(entry.localHeaderOffset + 26);
    const localExtraLength = archive.readUInt16LE(entry.localHeaderOffset + 28);

    if (localFlags !== entry.flags || localCompressionMethod !== entry.compressionMethod) {
      rejectArchive('has inconsistent file metadata');
    }

    if ((localFlags & DATA_DESCRIPTOR_FLAG) === 0
      && (localCompressedSize !== entry.compressedSize
        || localUncompressedSize !== entry.uncompressedSize)) {
      rejectArchive('has inconsistent file sizes');
    }

    const localFileNameOffset = entry.localHeaderOffset + LOCAL_FILE_HEADER_LENGTH;

    assertRange(
      archive,
      localFileNameOffset,
      localFileNameLength + localExtraLength,
      'local file name',
    );

    const localFileName = archive.subarray(
      localFileNameOffset,
      localFileNameOffset + localFileNameLength,
    );

    if (!localFileName.equals(entry.fileName)) {
      rejectArchive('has inconsistent file names');
    }

    const compressedDataOffset = localFileNameOffset + localFileNameLength + localExtraLength;

    assertRange(archive, compressedDataOffset, entry.compressedSize, 'compressed data');

    if (compressedDataOffset + entry.compressedSize > entry.centralDirectoryOffset) {
      rejectArchive('has compressed data outside the file entry');
    }

    return archive.subarray(
      compressedDataOffset,
      compressedDataOffset + entry.compressedSize,
    );
  }

  function decompressEntry(compressedData, entry) {
    try {
      if (entry.compressionMethod === STORED_COMPRESSION) {
        return Buffer.from(compressedData);
      }

      return zlib.inflateRawSync(compressedData, {
        maxOutputLength: maxBytes,
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      rejectArchive(`cannot be decompressed: ${message}`);
    }
  }

  if (!Buffer.isBuffer(archive)
    || archive.length < END_OF_CENTRAL_DIRECTORY_LENGTH
    || archive.length > maxArchiveBytes) {
    rejectArchive('is not a ZIP archive');
  }

  const entry = readCentralDirectoryEntry();
  const compressedData = readCompressedData(entry);
  const result = decompressEntry(compressedData, entry);

  if (result.length !== entry.uncompressedSize || result.length > maxBytes) {
    rejectArchive('decompresses to an invalid size');
  }

  return result;
}

module.exports = { extractSingleEntryArchive };
