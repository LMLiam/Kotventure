'use strict';

const fs = require('node:fs');
const path = require('node:path');
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

function getHeader(headers, name) {
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

    const contentLength = Number(getHeader(response.headers, 'content-length'));

    if (Number.isSafeInteger(contentLength) && contentLength > maximumBytes) {
        throw new Error(`metrics artifact download exceeds ${maximumBytes} bytes`);
    }

    if (response.body && typeof response.body.getReader === 'function') {
        const reader = response.body.getReader();
        const chunks = [];
        let totalBytes = 0;

        while (true) {
            const { done, value } = await reader.read();

            if (done) {
                break;
            }

            if (!value || !Number.isSafeInteger(value.byteLength)) {
                await reader.cancel().catch(() => {});
                throw new Error('metrics artifact download returned an invalid body');
            }

            if (totalBytes + value.byteLength > maximumBytes) {
                await reader.cancel().catch(() => {});
                throw new Error(`metrics artifact download exceeds ${maximumBytes} bytes`);
            }

            const chunk = Buffer.from(value);
            chunks.push(chunk);
            totalBytes += chunk.length;
        }

        return Buffer.concat(chunks, totalBytes);
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

function validateDownloadOptions({
    owner,
    repo,
    artifactId,
    outputDirectory,
    token,
    fetchImpl,
}) {
    if (typeof owner !== 'string' || owner.length === 0
    || typeof repo !== 'string' || repo.length === 0) {
        throw new Error('metrics artifact download requires a repository');
    }

    if (!Number.isSafeInteger(artifactId) || artifactId < 1) {
        throw new Error('metrics artifact id is invalid');
    }

    if (typeof outputDirectory !== 'string' || outputDirectory.length === 0) {
        throw new Error('metrics artifact output directory is required');
    }

    if (typeof token !== 'string' || token.length === 0) {
        throw new Error('metrics artifact download requires a GitHub token');
    }

    if (typeof fetchImpl !== 'function') {
        throw new Error('metrics artifact download requires fetch');
    }
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
    validateDownloadOptions({
        owner,
        repo,
        artifactId,
        outputDirectory,
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
                signal: AbortSignal.timeout(ARTIFACT_API_TIMEOUT_MS)
            },
    );

    if (response.status !== 302) {
        throw new Error(`metrics artifact download returned HTTP ${response.status}`);
    }

    const location = getHeader(response.headers, 'location');
    let artifactUrl;

    try {
        artifactUrl = new URL(location);
    } catch {
        throw new Error('metrics artifact download redirect is invalid');
    }

    if (artifactUrl.protocol !== 'https:') {
        throw new Error('metrics artifact download redirect is invalid');
    }

    const archiveResponse = await fetchImpl(artifactUrl.href, {
        redirect: 'follow',
        signal: AbortSignal.timeout(ARTIFACT_STORAGE_TIMEOUT_MS)
    });

    const archive = await readResponseBytes(archiveResponse, MAX_ARTIFACT_BYTES);
    const result = extractMetricsResultArchive(archive);

    fs.mkdirSync(outputDirectory, { recursive: true });

    const filePath = path.join(outputDirectory, RESULT_FILE_NAME);
    fs.writeFileSync(filePath, result, {
        flag: 'wx',
        mode: 0o600
    });

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

    const filePath = path.join(directory, RESULT_FILE_NAME);
    const stats = fs.lstatSync(filePath);

    if (!stats.isFile() || stats.isSymbolicLink()) {
        throw new Error(`${RESULT_FILE_NAME} must be a regular file`);
    }

    if (stats.size > MAX_RESULT_BYTES) {
        throw new Error(`metrics result exceeds ${MAX_RESULT_BYTES} bytes`);
    }

    const bytes = fs.readFileSync(filePath);

    if (bytes.length > MAX_RESULT_BYTES) {
        throw new Error(`metrics result exceeds ${MAX_RESULT_BYTES} bytes`);
    }

    let result;

    try {
        result = JSON.parse(bytes.toString('utf8'));
    } catch (error) {
        const message = error instanceof Error ? error.message : String(error);
        throw new Error(`metrics result is not valid JSON: ${message}`);
    }

    return validateMetricsResult(result);
}

module.exports = {
    downloadMetricsArtifact,
    readMetricsArtifact
};
