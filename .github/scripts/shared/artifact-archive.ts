import * as yauzl from 'yauzl';
import { createValidators } from './validation.js';

const END_OF_CENTRAL_DIRECTORY_SIGNATURE = 0x06054b50;
const CENTRAL_DIRECTORY_SIGNATURE = 0x02014b50;

const END_OF_CENTRAL_DIRECTORY_LENGTH = 22;
const CENTRAL_DIRECTORY_HEADER_LENGTH = 46;
const MAX_ZIP_COMMENT_LENGTH = 0xffff;

const ZIP64_16_BIT_VALUE = 0xffff;
const ZIP64_32_BIT_VALUE = 0xffffffff;
const ZIP64_EXTRA_FIELD_ID = 0x0001;
const DATA_DESCRIPTOR_FLAG = 0x0008;

const STORED_COMPRESSION = 0;
const DEFLATE_COMPRESSION = 8;

function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}

export interface ExtractSingleEntryArchiveOptions {
  errorPrefix: string;
  expectedFileName: string | Buffer;
  maxArchiveBytes: number;
  maxBytes: number;
}

export async function extractSingleEntryArchive(
  archive: Buffer,
  options: ExtractSingleEntryArchiveOptions,
): Promise<Buffer> {
  const { errorPrefix, expectedFileName, maxArchiveBytes, maxBytes } = options;
  const { requireBoundedInteger, requireString } = createValidators((message: string): never => {
    throw new Error(`archive extraction options ${message}`);
  });
  requireString(errorPrefix, 'error prefix');
  requireBoundedInteger(maxArchiveBytes, 'maximum archive bytes');
  requireBoundedInteger(maxBytes, 'maximum entry bytes');
  if (!Buffer.isBuffer(expectedFileName)
    && (typeof expectedFileName !== 'string' || expectedFileName.length === 0)) {
    throw new Error('archive extraction options expected file name is invalid');
  }

  const expectedName = Buffer.isBuffer(expectedFileName)
    ? expectedFileName
    : Buffer.from(expectedFileName, 'utf8');

  function rejectArchive(message: string): never {
    throw new Error(`${errorPrefix} ${message}`);
  }

  function rejectParse(message: string): never {
    rejectArchive(`cannot be parsed: ${message}`);
  }

  if (!Buffer.isBuffer(archive)
    || archive.length < END_OF_CENTRAL_DIRECTORY_LENGTH
    || archive.length > maxArchiveBytes) {
    rejectArchive('is not a ZIP archive');
  }

  const { centralDirectoryOffset } = readEndOfCentralDirectory(archive, rejectArchive);

  let zipfile: yauzl.ZipFile;
  try {
    zipfile = await yauzl.fromBufferPromise(archive, { decodeStrings: false });
  } catch (error) {
    rejectParse(errorMessage(error));
  }

  if (zipfile.entryCount !== 1) rejectArchive('must contain exactly one file');

  let entry: yauzl.Entry | undefined;
  try {
    for await (const candidate of zipfile.eachEntry()) {
      entry = candidate;
      break;
    }
  } catch (error) {
    rejectParse(errorMessage(error));
  }
  if (entry == null) rejectArchive('must contain exactly one file');

  if (!entry.fileNameRaw.equals(expectedName)) {
    rejectArchive(`must contain only ${expectedName.toString('utf8')}`);
  }

  if (entry.isEncrypted()
    || (entry.compressionMethod !== STORED_COMPRESSION
      && entry.compressionMethod !== DEFLATE_COMPRESSION)) {
    rejectArchive('uses unsupported compression or metadata');
  }

  if (entry.extraFields.some((field) => field.id === ZIP64_EXTRA_FIELD_ID)) {
    rejectArchive('uses unsupported ZIP64 or multi-disk metadata');
  }

  if (entry.compressedSize < 1
    || entry.compressedSize > archive.length
    || entry.uncompressedSize < 1
    || entry.uncompressedSize > maxBytes) {
    rejectArchive('contains an entry outside the size limit');
  }

  if (entry.relativeOffsetOfLocalHeader >= centralDirectoryOffset) {
    rejectArchive('has an invalid local file header offset');
  }

  let localHeader: yauzl.LocalFileHeader;
  try {
    localHeader = await zipfile.readLocalFileHeaderPromise(entry);
  } catch (error) {
    rejectParse(errorMessage(error));
  }

  if (localHeader.generalPurposeBitFlag !== entry.generalPurposeBitFlag
    || localHeader.compressionMethod !== entry.compressionMethod) {
    rejectArchive('has inconsistent file metadata');
  }

  if ((localHeader.generalPurposeBitFlag & DATA_DESCRIPTOR_FLAG) === 0
    && (localHeader.compressedSize !== entry.compressedSize
      || localHeader.uncompressedSize !== entry.uncompressedSize)) {
    rejectArchive('has inconsistent file sizes');
  }

  if (!localHeader.fileName.equals(entry.fileNameRaw)) {
    rejectArchive('has inconsistent file names');
  }

  if (localHeader.fileDataStart + entry.compressedSize > centralDirectoryOffset) {
    rejectArchive('has compressed data outside the file entry');
  }

  const chunks: Buffer[] = [];
  let total = 0;
  try {
    const readStream = await zipfile.openReadStreamPromise(entry);
    for await (const chunk of readStream) {
      if (total + chunk.length > maxBytes) rejectArchive('decompresses to an invalid size');
      chunks.push(chunk);
      total += chunk.length;
    }
  } catch (error) {
    rejectParse(errorMessage(error));
  }

  const result = Buffer.concat(chunks, total);
  if (result.length !== entry.uncompressedSize) rejectArchive('decompresses to an invalid size');

  return result;
}

function readEndOfCentralDirectory(
  archive: Buffer,
  rejectArchive: (message: string) => never,
): { centralDirectoryOffset: number } {
  const firstOffset = Math.max(
    0,
    archive.length - END_OF_CENTRAL_DIRECTORY_LENGTH - MAX_ZIP_COMMENT_LENGTH,
  );

  let endOffset = -1;
  for (
    let offset = archive.length - END_OF_CENTRAL_DIRECTORY_LENGTH;
    offset >= firstOffset;
    offset -= 1
  ) {
    if (archive.readUInt32LE(offset) !== END_OF_CENTRAL_DIRECTORY_SIGNATURE) continue;

    const commentLength = archive.readUInt16LE(offset + 20);

    if (offset + END_OF_CENTRAL_DIRECTORY_LENGTH + commentLength === archive.length) {
      endOffset = offset;
      break;
    }
  }

  if (endOffset < 0) rejectArchive('has no valid end record');

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

  if (entriesOnDisk !== 1 || totalEntries !== 1) rejectArchive('must contain exactly one file');

  if (centralDirectoryOffset + centralDirectorySize > archive.length) {
    rejectArchive('central directory is outside the archive');
  }

  if (centralDirectoryOffset + centralDirectorySize !== endOffset) rejectArchive('has a misplaced central directory');

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

  if (recordLength !== centralDirectorySize) rejectArchive('has an invalid central directory record');

  const diskStart = archive.readUInt16LE(centralDirectoryOffset + 34);

  if (diskStart !== 0) rejectArchive('uses unsupported compression or metadata');

  return { centralDirectoryOffset };
}
