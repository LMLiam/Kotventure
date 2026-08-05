'use strict';

const fs = require('fs');
const path = require('path');
const {
  MAX_RESULT_BYTES,
  validateMetricsResult,
} = require('../actions/pr-metrics-comment/lib/metrics-result.js');
const {
  MAX_ARTIFACT_BYTES,
  RESULT_FILE_NAME,
} = require('./pr-metrics-publisher-contract.js');
const { extractMetricsResultArchive } = require('./pr-metrics-publisher-archive.js');

const ARTIFACT_API_TIMEOUT_MS = 30_000;
const ARTIFACT_STORAGE_TIMEOUT_MS = 60_000;

function headerValue(headers, name) {
  if (!headers) {
    return null;
  }
  if (typeof headers.get === 'function') {
    return headers.get(name);
  }
  return headers[name] ?? headers[name.toLowerCase()] ?? null;
}

async function readResponseBytes(response, maximumBytes) {
  if (!response || response.ok === false) {
    throw new Error('metrics artifact download failed');
  }
  const declaredLength = Number(headerValue(response.headers, 'content-length'));
  if (Number.isSafeInteger(declaredLength) && declaredLength > maximumBytes) {
    throw new Error(`metrics artifact download exceeds ${maximumBytes} bytes`);
  }

  if (response.body && typeof response.body.getReader === 'function') {
    const reader = response.body.getReader();
    const chunks = [];
    let total = 0;
    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }
      if (!value || !Number.isSafeInteger(value.byteLength) || total + value.byteLength > maximumBytes) {
        await reader.cancel();
        throw new Error(`metrics artifact download exceeds ${maximumBytes} bytes`);
      }
      const chunk = Buffer.from(value);
      total += chunk.length;
      chunks.push(chunk);
    }
    return Buffer.concat(chunks, total);
  }

  if (typeof response.arrayBuffer !== 'function') {
    throw new Error('metrics artifact download has no readable body');
  }
  const result = Buffer.from(await response.arrayBuffer());
  if (result.length > maximumBytes) {
    throw new Error(`metrics artifact download exceeds ${maximumBytes} bytes`);
  }
  return result;
}

async function downloadMetricsArtifact({
  owner,
  repo,
  artifactId,
  outputDirectory,
  apiUrl = process.env.GITHUB_API_URL || 'https://api.github.com',
  token = process.env.GITHUB_TOKEN,
  fetchImpl = globalThis.fetch,
}) {
  if (!Number.isSafeInteger(artifactId) || artifactId < 1) {
    throw new Error('metrics artifact id is invalid');
  }
  if (typeof fetchImpl !== 'function') {
    throw new Error('metrics artifact download requires fetch');
  }
  if (typeof token !== 'string' || token.length === 0) {
    throw new Error('metrics artifact download requires a GitHub token');
  }
  const response = await fetchImpl(
    `${apiUrl}/repos/${owner}/${repo}/actions/artifacts/${artifactId}/zip`,
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
    throw new Error(`metrics artifact download returned HTTP ${response.status}`);
  }
  const location = headerValue(response.headers, 'location');
  if (typeof location !== 'string' || !location.startsWith('https://')) {
    throw new Error('metrics artifact download redirect is invalid');
  }
  const archiveResponse = await fetchImpl(location, {
    redirect: 'follow',
    signal: AbortSignal.timeout(ARTIFACT_STORAGE_TIMEOUT_MS),
  });
  const archive = await readResponseBytes(archiveResponse, MAX_ARTIFACT_BYTES);
  const result = extractMetricsResultArchive(archive);
  fs.mkdirSync(outputDirectory, { recursive: true });
  const filePath = path.join(outputDirectory, RESULT_FILE_NAME);
  fs.writeFileSync(filePath, result, { mode: 0o600 });
  return filePath;
}

function readMetricsArtifact(directory) {
  if (typeof directory !== 'string' || directory.length === 0) {
    throw new Error('metrics artifact directory is required');
  }
  const entries = fs.readdirSync(directory, { withFileTypes: true });
  if (entries.length !== 1 || entries[0].name !== RESULT_FILE_NAME) {
    throw new Error(`metrics artifact must contain only ${RESULT_FILE_NAME}`);
  }
  const [entry] = entries;
  if (entry.isSymbolicLink() || !entry.isFile()) {
    throw new Error(`${RESULT_FILE_NAME} must be a regular file`);
  }
  const filePath = path.join(directory, RESULT_FILE_NAME);
  const stats = fs.statSync(filePath);
  if (!stats.isFile() || stats.size > MAX_RESULT_BYTES) {
    throw new Error(`metrics result exceeds ${MAX_RESULT_BYTES} bytes`);
  }
  const text = fs.readFileSync(filePath, 'utf8');
  if (Buffer.byteLength(text, 'utf8') > MAX_RESULT_BYTES) {
    throw new Error(`metrics result exceeds ${MAX_RESULT_BYTES} bytes`);
  }
  let result;
  try {
    result = JSON.parse(text);
  } catch (error) {
    throw new Error(`metrics result is not valid JSON: ${error.message}`);
  }
  return validateMetricsResult(result);
}

module.exports = {
  downloadMetricsArtifact,
  readMetricsArtifact,
};
