'use strict';

const RELEASE_ONLY_FILES = new Set([
  'CHANGELOG.md',
  '.release-please-manifest.json',
  'gradle/libs.versions.toml',
]);

const DOCUMENTATION_PATH_PATTERNS = [
  /^README\.md$/,
  /^LICENSE\.md$/,
  /^AGENTS\.md$/,
  /^docs\/.+$/,
  /^\.github\/(?:CONTRIBUTING|SUPPORT)\.md$/,
  /^\.github\/pull_request_template\.md$/,
  /^\.github\/(?:PULL_REQUEST_TEMPLATE|ISSUE_TEMPLATE)\/[^/]+$/,
  /^modules\/[^/]+\/README\.md$/,
  /^assets\/.+\.(?:svg|png|jpe?g|gif|webp)$/i,
];

function isSafeRepositoryPath(name) {
  return typeof name === 'string'
    && name.length > 0
    && !name.includes('\\')
    && !name.startsWith('/')
    && !name.split('/').includes('..')
    && !name.includes('\u0000');
}

function isDocumentationPath(name) {
  return isSafeRepositoryPath(name)
    && DOCUMENTATION_PATH_PATTERNS.some((pattern) => pattern.test(name));
}

function changedPathNames(files) {
  if (!Array.isArray(files) || files.length === 0) {
    return null;
  }

  const paths = [];
  for (const file of files) {
    if (!file || typeof file.filename !== 'string' || file.filename.length === 0) {
      return null;
    }
    paths.push(file.filename);
    if (file.previous_filename != null) {
      if (typeof file.previous_filename !== 'string' || file.previous_filename.length === 0) {
        return null;
      }
      paths.push(file.previous_filename);
    }
  }
  return paths;
}

function classifyChangedFiles(files) {
  const paths = changedPathNames(files);
  if (!paths) {
    return 'code';
  }
  if (paths.every(isDocumentationPath)) {
    return 'documentation';
  }
  if (paths.every((name) => isSafeRepositoryPath(name) && RELEASE_ONLY_FILES.has(name))) {
    return 'release-candidate';
  }
  return 'code';
}

module.exports = {
  DOCUMENTATION_PATH_PATTERNS,
  RELEASE_ONLY_FILES,
  changedPathNames,
  classifyChangedFiles,
  isDocumentationPath,
  isSafeRepositoryPath,
};
