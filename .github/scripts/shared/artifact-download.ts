import * as fs from 'node:fs';
import * as path from 'node:path';
import { extractSingleEntryArchive } from './artifact-archive.js';

const ARTIFACT_API_TIMEOUT_MS = 30_000;
const ARTIFACT_STORAGE_TIMEOUT_MS = 60_000;

type ResponseHeaders = Headers | { readonly [name: string]: string | null | undefined };

function isHeaders(headers: ResponseHeaders): headers is Headers {
  return typeof headers.get === 'function';
}

function getHeader(headers: ResponseHeaders | null, name: string): string | null {
  if (!headers) return null;

  if (isHeaders(headers)) return headers.get(name);

  return headers[name] ?? headers[name.toLowerCase()] ?? null;
}

async function cancelReader(reader: ReadableStreamDefaultReader<Uint8Array>): Promise<void> {
  try {
    await reader.cancel();
  } catch {
    // Cancellation must not replace the validation error.
  }
}

async function readStreamingBytes(
  body: ReadableStream<Uint8Array>,
  maximumBytes: number,
  label: string,
): Promise<Buffer> {
  const reader = body.getReader();
  const chunks: Buffer[] = [];
  let totalBytes = 0;

  while (true) {
    const { done, value } = await reader.read();

    if (done) return Buffer.concat(chunks, totalBytes);

    if (!(value instanceof Uint8Array)) {
      await cancelReader(reader);
      throw new Error(`${label} download returned an invalid body`);
    }

    if (totalBytes + value.byteLength > maximumBytes) {
      await cancelReader(reader);
      throw new Error(`${label} download exceeds ${maximumBytes} bytes`);
    }

    const chunk = Buffer.from(value);
    chunks.push(chunk);
    totalBytes += chunk.length;
  }
}

async function readResponseBytes(response: Response, maximumBytes: number, label: string): Promise<Buffer> {
  if (!response || response.ok === false) throw new Error(`${label} download failed`);

  const contentLength = Number(getHeader(response.headers, 'content-length'));

  if (Number.isSafeInteger(contentLength) && contentLength > maximumBytes) {
    throw new Error(`${label} download exceeds ${maximumBytes} bytes`);
  }

  if (response.body && typeof response.body.getReader === 'function') {
    return readStreamingBytes(response.body, maximumBytes, label);
  }

  if (typeof response.arrayBuffer !== 'function') throw new Error(`${label} download has no readable body`);

  const result = Buffer.from(await response.arrayBuffer());

  if (result.length > maximumBytes) throw new Error(`${label} download exceeds ${maximumBytes} bytes`);

  return result;
}

interface DownloadValidationOptions {
  owner: string;
  repo: string;
  artifactId: number;
  outputDirectory: string;
  fileName: string;
  maxArchiveBytes: number;
  maxBytes: number;
  token: string | undefined;
  fetchImpl: typeof fetch;
  label: string;
}

function validateDownloadOptions(options: DownloadValidationOptions): void {
  const {
    owner,
    repo,
    artifactId,
    outputDirectory,
    fileName,
    maxArchiveBytes,
    maxBytes,
    token,
    fetchImpl,
    label,
  } = options;

  if (typeof owner !== 'string' || owner.length === 0
    || typeof repo !== 'string' || repo.length === 0) {
    throw new Error(`${label} download requires a repository`);
  }

  if (!Number.isSafeInteger(artifactId) || artifactId < 1) throw new Error(`${label} id is invalid`);

  if (typeof outputDirectory !== 'string' || outputDirectory.length === 0) {
    throw new Error(`${label} output directory is required`);
  }

  if (typeof fileName !== 'string' || fileName.length === 0
    || fileName === '.' || fileName === '..'
    || fileName.includes('/') || fileName.includes('\\')) {
    throw new Error(`${label} download file name is invalid`);
  }

  if (!Number.isSafeInteger(maxArchiveBytes) || maxArchiveBytes < 1) {
    throw new Error(`${label} download archive size limit is invalid`);
  }

  if (!Number.isSafeInteger(maxBytes) || maxBytes < 1) throw new Error(`${label} download size limit is invalid`);

  if (typeof token !== 'string' || token.length === 0) throw new Error(`${label} download requires a GitHub token`);

  if (typeof fetchImpl !== 'function') throw new Error(`${label} download requires fetch`);
}

export interface DownloadSingleFileArtifactOptions {
  owner: string;
  repo: string;
  artifactId: number;
  outputDirectory: string;
  fileName: string;
  maxArchiveBytes: number;
  maxBytes: number;
  label: string;
  apiUrl?: string | undefined;
  token?: string | undefined;
  fetchImpl?: typeof fetch | undefined;
  validateResult?: (result: Buffer) => void;
}

export interface DownloadArtifactArchiveOptions {
  owner: string;
  repo: string;
  artifactId: number;
  maxArchiveBytes: number;
  label: string;
  apiUrl?: string | undefined;
  token?: string | undefined;
  fetchImpl?: typeof fetch | undefined;
}

function validateArchiveDownloadOptions(options: DownloadArtifactArchiveOptions): void {
  const {
    owner,
    repo,
    artifactId,
    maxArchiveBytes,
    label,
    token,
    fetchImpl,
  } = options;
  if (typeof owner !== 'string' || owner.length === 0
    || typeof repo !== 'string' || repo.length === 0) {
    throw new Error(`${label} download requires a repository`);
  }
  if (!Number.isSafeInteger(artifactId) || artifactId < 1) throw new Error(`${label} id is invalid`);
  if (!Number.isSafeInteger(maxArchiveBytes) || maxArchiveBytes < 1) {
    throw new Error(`${label} download archive size limit is invalid`);
  }
  if (typeof token !== 'string' || token.length === 0) throw new Error(`${label} download requires a GitHub token`);
  if (typeof fetchImpl !== 'function') throw new Error(`${label} download requires fetch`);
}

export async function downloadArtifactArchive(options: DownloadArtifactArchiveOptions): Promise<Buffer> {
  const {
    owner,
    repo,
    artifactId,
    maxArchiveBytes,
    label,
    apiUrl = process.env.GITHUB_API_URL || 'https://api.github.com',
    token = process.env.GITHUB_TOKEN,
    fetchImpl = globalThis.fetch,
  } = options;
  validateArchiveDownloadOptions({
    owner,
    repo,
    artifactId,
    maxArchiveBytes,
    label,
    token,
    fetchImpl,
  });

  const baseApiUrl = apiUrl.replace(/\/+$/, '');
  const response = await fetchImpl(
    `${baseApiUrl}/repos/${owner}/${repo}/actions/artifacts/${artifactId}/zip`,
    {
      headers: {
        accept: 'application/vnd.github+json',
        authorization: `Bearer ${token}`,
      },
      redirect: 'manual',
      signal: AbortSignal.timeout(ARTIFACT_API_TIMEOUT_MS),
    },
  );
  if (response.status !== 302) throw new Error(`${label} download returned HTTP ${response.status}`);

  const location = getHeader(response.headers, 'location');
  let artifactUrl: URL;
  try {
    artifactUrl = new URL(location ?? '');
  } catch {
    throw new Error(`${label} download redirect is invalid`);
  }
  if (artifactUrl.protocol !== 'https:') throw new Error(`${label} download redirect is invalid`);

  const archiveResponse = await fetchImpl(artifactUrl.href, {
    redirect: 'follow',
    signal: AbortSignal.timeout(ARTIFACT_STORAGE_TIMEOUT_MS),
  });
  return readResponseBytes(archiveResponse, maxArchiveBytes, label);
}

export async function downloadSingleFileArtifact(options: DownloadSingleFileArtifactOptions): Promise<string> {
  const {
    owner,
    repo,
    artifactId,
    outputDirectory,
    fileName,
    maxArchiveBytes,
    maxBytes,
    label,
    apiUrl = process.env.GITHUB_API_URL || 'https://api.github.com',
    token = process.env.GITHUB_TOKEN,
    fetchImpl = globalThis.fetch,
    validateResult = null,
  } = options;

  validateDownloadOptions({
    owner,
    repo,
    artifactId,
    outputDirectory,
    fileName,
    maxArchiveBytes,
    maxBytes,
    token,
    fetchImpl,
    label,
  });

  const archive = await downloadArtifactArchive({
    owner,
    repo,
    artifactId,
    maxArchiveBytes,
    label,
    apiUrl,
    token,
    fetchImpl,
  });
  const result = await extractSingleEntryArchive(archive, {
    errorPrefix: `${label} archive`,
    expectedFileName: fileName,
    maxArchiveBytes,
    maxBytes,
  });

  if (validateResult) validateResult(result);

  fs.mkdirSync(outputDirectory, { recursive: true });

  const filePath = path.join(outputDirectory, fileName);
  fs.writeFileSync(filePath, result, {
    flag: 'wx',
    mode: 0o600,
  });

  return filePath;
}
