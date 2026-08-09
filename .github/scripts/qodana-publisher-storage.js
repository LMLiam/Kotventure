'use strict';

const fs = require('node:fs');
const path = require('node:path');
const {
  MAX_ARTIFACT_BYTES,
  QODANA_SARIF_FILE_NAME,
} = require('./qodana-contract.js');
const { extractQodanaSarifArchive } = require('./qodana-publisher-archive.js');
const { validateQodanaSarif } = require('./qodana-publisher-validation.js');

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
    throw new Error('Qodana artifact download failed');
  }
  const declaredLength = Number(headerValue(response.headers, 'content-length'));
  if (Number.isSafeInteger(declaredLength) && declaredLength > maximumBytes) {
    throw new Error(`Qodana artifact download exceeds ${maximumBytes} bytes`);
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
        throw new Error(`Qodana artifact download exceeds ${maximumBytes} bytes`);
      }
      const chunk = Buffer.from(value);
      total += chunk.length;
      chunks.push(chunk);
    }
    return Buffer.concat(chunks, total);
  }

  if (typeof response.arrayBuffer !== 'function') {
    throw new Error('Qodana artifact download has no readable body');
  }
  const result = Buffer.from(await response.arrayBuffer());
  if (result.length > maximumBytes) {
    throw new Error(`Qodana artifact download exceeds ${maximumBytes} bytes`);
  }
  return result;
}

async function downloadQodanaArtifact({
  owner,
  repo,
  artifactId,
  outputDirectory,
  apiUrl = process.env.GITHUB_API_URL || 'https://api.github.com',
  token = process.env.GITHUB_TOKEN,
  fetchImpl = globalThis.fetch,
}) {
  if (!Number.isSafeInteger(artifactId) || artifactId < 1) {
    throw new Error('Qodana artifact id is invalid');
  }
  if (typeof fetchImpl !== 'function') {
    throw new Error('Qodana artifact download requires fetch');
  }
  if (typeof token !== 'string' || token.length === 0) {
    throw new Error('Qodana artifact download requires a GitHub token');
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
    throw new Error(`Qodana artifact download returned HTTP ${response.status}`);
  }
  const location = headerValue(response.headers, 'location');
  if (typeof location !== 'string' || !location.startsWith('https://')) {
    throw new Error('Qodana artifact download redirect is invalid');
  }
  const archiveResponse = await fetchImpl(location, {
    redirect: 'follow',
    signal: AbortSignal.timeout(ARTIFACT_STORAGE_TIMEOUT_MS),
  });
  const archive = await readResponseBytes(archiveResponse, MAX_ARTIFACT_BYTES);
  const result = extractQodanaSarifArchive(archive);
  validateQodanaSarif(result);

  fs.mkdirSync(outputDirectory, { recursive: true });
  const filePath = path.join(outputDirectory, QODANA_SARIF_FILE_NAME);
  fs.writeFileSync(filePath, result, { mode: 0o600 });
  return filePath;
}

module.exports = {
  downloadQodanaArtifact,
  readResponseBytes,
};
