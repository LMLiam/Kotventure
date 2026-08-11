'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { extractSingleEntryArchive } = require('./artifact-archive.js');

const ARTIFACT_API_TIMEOUT_MS = 30_000;
const ARTIFACT_STORAGE_TIMEOUT_MS = 60_000;

function getHeader(headers, name) {
  if (!headers) return null;

  if (typeof headers.get === 'function') {
    return headers.get(name);
  }

  return headers[name] ?? headers[name.toLowerCase()] ?? null;
}

async function cancelReader(reader) {
  try {
    await reader.cancel();
  } catch {
    // Cancellation must not replace the validation error.
  }
}

async function readStreamingBytes(body, maximumBytes, label) {
  const reader = body.getReader();
  const chunks = [];
  let totalBytes = 0;

  while (true) {
    const { done, value } = await reader.read();

    if (done) {
      return Buffer.concat(chunks, totalBytes);
    }

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

async function readResponseBytes(response, maximumBytes, label) {
  if (!response || response.ok === false) {
    throw new Error(`${label} download failed`);
  }

  const contentLength = Number(getHeader(response.headers, 'content-length'));

  if (Number.isSafeInteger(contentLength) && contentLength > maximumBytes) {
    throw new Error(`${label} download exceeds ${maximumBytes} bytes`);
  }

  if (response.body && typeof response.body.getReader === 'function') {
    return readStreamingBytes(response.body, maximumBytes, label);
  }

  if (typeof response.arrayBuffer !== 'function') {
    throw new Error(`${label} download has no readable body`);
  }

  const result = Buffer.from(await response.arrayBuffer());

  if (result.length > maximumBytes) {
    throw new Error(`${label} download exceeds ${maximumBytes} bytes`);
  }

  return result;
}

function validateDownloadOptions({
  owner,
  repo,
  artifactId,
  outputDirectory,
  token,
  fetchImpl,
  label,
}) {
  if (typeof owner !== 'string' || owner.length === 0
  || typeof repo !== 'string' || repo.length === 0) {
    throw new Error(`${label} download requires a repository`);
  }

  if (!Number.isSafeInteger(artifactId) || artifactId < 1) {
    throw new Error(`${label} id is invalid`);
  }

  if (typeof outputDirectory !== 'string' || outputDirectory.length === 0) {
    throw new Error(`${label} output directory is required`);
  }

  if (typeof token !== 'string' || token.length === 0) {
    throw new Error(`${label} download requires a GitHub token`);
  }

  if (typeof fetchImpl !== 'function') {
    throw new Error(`${label} download requires fetch`);
  }
}

async function downloadSingleFileArtifact({
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
}) {
  validateDownloadOptions({
    owner,
    repo,
    artifactId,
    outputDirectory,
    token,
    fetchImpl,
    label,
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

  if (response.status !== 302) {
    throw new Error(`${label} download returned HTTP ${response.status}`);
  }

  const location = getHeader(response.headers, 'location');
  let artifactUrl;

  try {
    artifactUrl = new URL(location);
  } catch {
    throw new Error(`${label} download redirect is invalid`);
  }

  if (artifactUrl.protocol !== 'https:') {
    throw new Error(`${label} download redirect is invalid`);
  }

  const archiveResponse = await fetchImpl(artifactUrl.href, {
    redirect: 'follow',
    signal: AbortSignal.timeout(ARTIFACT_STORAGE_TIMEOUT_MS),
  });

  const archive = await readResponseBytes(archiveResponse, maxArchiveBytes, label);
  const result = extractSingleEntryArchive(archive, {
    errorPrefix: `${label} archive`,
    expectedFileName: fileName,
    maxArchiveBytes,
    maxBytes,
  });

  if (validateResult) {
    validateResult(result);
  }

  fs.mkdirSync(outputDirectory, { recursive: true });

  const filePath = path.join(outputDirectory, fileName);
  fs.writeFileSync(filePath, result, {
    flag: 'wx',
    mode: 0o600,
  });

  return filePath;
}

module.exports = { downloadSingleFileArtifact };
