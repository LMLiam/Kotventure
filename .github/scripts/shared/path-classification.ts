export const RELEASE_ONLY_FILES: ReadonlySet<string> = new Set([
  'CHANGELOG.md',
  '.release-please-manifest.json',
  'gradle/libs.versions.toml',
]);

export const DOCUMENTATION_PATH_PATTERNS: readonly RegExp[] = [
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

export interface ChangedFileRecord {
  filename: string;
  previous_filename?: string;
}

export function isSafeRepositoryPath(name: string): boolean {
  return typeof name === 'string'
    && name.length > 0
    && !name.includes('\\')
    && !name.startsWith('/')
    && !name.split('/').includes('..')
    && !name.includes('\u0000');
}

export function isDocumentationPath(name: string): boolean {
  return isSafeRepositoryPath(name)
    && DOCUMENTATION_PATH_PATTERNS.some((pattern) => pattern.test(name));
}

export function changedPathNames(files: ChangedFileRecord[] | null | undefined): string[] | null {
  if (!Array.isArray(files) || files.length === 0) return null;

  const paths: string[] = [];
  for (const file of files) {
    if (!file || typeof file.filename !== 'string' || file.filename.length === 0) return null;
    paths.push(file.filename);
    if (file.previous_filename != null) {
      if (typeof file.previous_filename !== 'string' || file.previous_filename.length === 0) return null;
      paths.push(file.previous_filename);
    }
  }
  return paths;
}

export function classifyChangedFiles(files: ChangedFileRecord[] | null | undefined): 'code' | 'documentation' | 'release-candidate' {
  const paths = changedPathNames(files);
  if (!paths) return 'code';
  if (paths.every(isDocumentationPath)) return 'documentation';
  if (paths.every((name) => isSafeRepositoryPath(name) && RELEASE_ONLY_FILES.has(name))) return 'release-candidate';
  return 'code';
}
