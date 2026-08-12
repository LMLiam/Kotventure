import * as fs from 'node:fs';
import * as os from 'node:os';
import * as path from 'node:path';
import type { TestContext } from 'node:test';
import AdmZip from 'adm-zip';
import { serializeMetricsResult } from '../actions/pr-metrics-comment/lib/metrics-result.js';
import {
  RESULT_ARTIFACT_PREFIX,
  RESULT_FILE_NAME,
} from './pr-metrics-publisher-contract.js';
import type {
  Octokit,
  PullRequestData,
  RepositoryData,
  WorkflowData,
  WorkflowRunArtifact,
  WorkflowRunData,
} from './shared/action-context.js';
import type { WorkflowRunEventRecord } from './shared/run-context.js';
import type { WorkflowSource } from './pr-metrics-publisher-validation.js';
import type { downloadMetricsArtifact } from './pr-metrics-publisher-storage.js';
import {
  asApiData,
  mockContext,
  mockFetchImpl,
  mockOctokit,
  type MockFetchImpl,
  type MockFetchOptions,
  type MockFetchRequest,
  type MockFetchResponse,
  type TestGithubMock,
} from './test-support/mocks.js';

const BASE_SHA = 'a'.repeat(40);
const HEAD_SHA = 'b'.repeat(40);
const ARTIFACT_ID = 700;
const ARTIFACT_API_URL = 'https://api.github.test';
const ARTIFACT_STORAGE_URL = 'https://artifact.example/result.zip';
const TEST_TOKEN = 'test-token';

const REPOSITORY = Object.freeze({
  full_name: 'LMLiam/Kotventure',
  id: 1,
  default_branch: 'master',
});

const HEAD_REPOSITORY = Object.freeze({
  full_name: 'LMLiam/Kotventure-fix',
  id: 2,
});

export interface WorkflowSourceInputs {
  eventRun: WorkflowRunEventRecord;
  run: WorkflowRunData;
  workflow: WorkflowData;
  repository: RepositoryData;
  pullRequest: PullRequestData;
  pullNumber: number;
}

export function makeInputs(): WorkflowSourceInputs {
  return {
    eventRun: {
      id: 100,
      run_attempt: 2,
      workflow_id: 55,
      event: 'pull_request',
      status: 'completed',
      conclusion: 'success',
      head_sha: HEAD_SHA,
    },
    run: asApiData<WorkflowRunData>({
      id: 100,
      run_attempt: 2,
      workflow_id: 55,
      event: 'pull_request',
      status: 'completed',
      conclusion: 'success',
      repository: REPOSITORY,
      pull_requests: [{ number: 42 }],
      head_repository: HEAD_REPOSITORY,
      head_branch: 'fix/metrics',
      head_sha: HEAD_SHA,
    }),
    workflow: asApiData<WorkflowData>({
      id: 55,
      name: 'CI',
      path: '.github/workflows/ci.yml',
    }),
    repository: asApiData<RepositoryData>(REPOSITORY),
    pullRequest: asApiData<PullRequestData>({
      number: 42,
      state: 'open',
      base: {
        repo: REPOSITORY,
        ref: 'master',
        sha: BASE_SHA,
      },
      head: {
        repo: HEAD_REPOSITORY,
        ref: 'fix/metrics',
        sha: HEAD_SHA,
      },
    }),
    pullNumber: 42,
  };
}

export function makeSource(overrides: Partial<WorkflowSource> = {}): WorkflowSource {
  return {
    repository: REPOSITORY.full_name,
    repositoryId: REPOSITORY.id,
    workflow: 'CI',
    event: 'pull_request',
    runId: 100,
    runAttempt: 2,
    pullRequest: 42,
    baseRepository: REPOSITORY.full_name,
    baseRepositoryId: REPOSITORY.id,
    baseRef: REPOSITORY.default_branch,
    baseSha: BASE_SHA,
    headRepository: HEAD_REPOSITORY.full_name,
    headRepositoryId: HEAD_REPOSITORY.id,
    headRef: 'fix/metrics',
    headSha: HEAD_SHA,
    ...overrides,
  };
}

export function makeArtifact(source: WorkflowSource = makeSource()): WorkflowRunArtifact {
  return asApiData<WorkflowRunArtifact>({
    id: ARTIFACT_ID,
    name: `${RESULT_ARTIFACT_PREFIX}${source.runId}-${source.runAttempt}`,
    expired: false,
    size_in_bytes: 200,
    workflow_run: {
      id: source.runId,
      repository_id: source.repositoryId,
      head_repository_id: source.headRepositoryId,
      head_branch: source.headRef,
      head_sha: source.headSha,
    },
  });
}

export function makeZip(
  content: Buffer | string,
  {
    compressionMethod = 8,
    fileName = RESULT_FILE_NAME,
  }: {
    compressionMethod?: 0 | 8;
    fileName?: string;
  } = {},
): Buffer {
  const zip = new AdmZip();
  const entry = zip.addFile(
    fileName,
    Buffer.isBuffer(content) ? content : Buffer.from(content, 'utf8'),
  );
  if (compressionMethod === 0) entry.header.method = 0;
  return zip.toBuffer();
}

export function makeWorkflowRunContext(run: WorkflowRunData = makeInputs().run) {
  return mockContext({
    repo: {
      owner: 'LMLiam',
      repo: 'Kotventure',
    },
    serverUrl: 'https://github.com',
    payload: {
      workflow_run: {
        id: run.id,
        run_attempt: run.run_attempt,
        workflow_id: run.workflow_id,
        event: run.event,
        status: run.status,
        conclusion: run.conclusion,
        head_sha: run.head_sha,
      },
    },
  });
}

export interface MakeGithubOptions {
  run?: WorkflowRunData;
  associatedPullRequests?: PullRequestData[];
  artifacts?: WorkflowRunArtifact[];
  pullRequest?: PullRequestData;
}

export function makeGithubMock({
  run = makeInputs().run,
  associatedPullRequests = [],
  artifacts = [],
  pullRequest = makeInputs().pullRequest,
}: MakeGithubOptions = {}): TestGithubMock {
  const listAssociatedPullRequests = async () => [];
  const listWorkflowRunArtifacts = async () => artifacts;
  const paginate = async (method: (...args: never[]) => unknown) => {
    if (method === listAssociatedPullRequests) return associatedPullRequests;
    if (method === listWorkflowRunArtifacts) return artifacts;
    throw new Error('unexpected pagination method');
  };

  return {
    rest: {
      repos: {
        get: async () => ({
          data: REPOSITORY,
        }),
        listPullRequestsAssociatedWithCommit: listAssociatedPullRequests,
      },
      actions: {
        getWorkflowRun: async () => ({
          data: run,
        }),
        getWorkflow: async () => ({
          data: {
            id: 55,
            name: 'CI',
            path: '.github/workflows/ci.yml',
          },
        }),
        listWorkflowRunArtifacts,
      },
      pulls: {
        get: async () => ({
          data: pullRequest,
        }),
      },
    },
    paginate,
  };
}

export function makeGithub(options: MakeGithubOptions = {}): Octokit {
  return mockOctokit(makeGithubMock(options));
}

export function makeResult(source: WorkflowSource = makeSource()): ReturnType<typeof serializeMetricsResult> {
  return serializeMetricsResult({
    context: mockContext({
      repo: {
        owner: 'LMLiam',
        repo: 'Kotventure',
      },
      eventName: 'pull_request',
      payload: {
        pull_request: {
          number: source.pullRequest,
          base: {
            repo: {
              full_name: source.baseRepository,
            },
            ref: source.baseRef,
            sha: source.baseSha,
          },
          head: {
            repo: {
              full_name: source.headRepository,
            },
            ref: source.headRef,
            sha: source.headSha,
          },
        },
      },
    }),
    runId: String(source.runId),
    runAttempt: String(source.runAttempt),
    headCoverage: null,
    baseCoverage: null,
    headJars: new Map([
      [
        'core',
        {
          size: 1,
          classes: 1,
        },
      ],
    ]),
    baseJars: new Map(),
    headMetrics: null,
    baseMetrics: null,
    patchCoverage: null,
    apiSurface: null,
  });
}

export interface ArtifactFetchFixture {
  requests: MockFetchRequest[];
  fetchImpl: typeof fetch;
}

export function makeArtifactFetch(archiveResponse: MockFetchResponse): ArtifactFetchFixture {
  const requests: MockFetchRequest[] = [];
  const downloadUrl = `${ARTIFACT_API_URL}/repos/LMLiam/Kotventure/actions/artifacts/${ARTIFACT_ID}/zip`;
  const fetchImpl: MockFetchImpl = async (location, options: MockFetchOptions) => {
    requests.push({ location, options });

    if (location === downloadUrl) {
      return {
        status: 302,
        headers: {
          location: ARTIFACT_STORAGE_URL,
        },
      };
    }

    if (location === ARTIFACT_STORAGE_URL) return archiveResponse;

    throw new Error(`unexpected artifact request: ${location}`);
  };

  return {
    requests,
    fetchImpl: mockFetchImpl(fetchImpl),
  };
}

export function makeDownloadOptions(
  outputDirectory: string,
  fetchImpl: typeof fetch,
): Parameters<typeof downloadMetricsArtifact>[0] {
  return {
    owner: 'LMLiam',
    repo: 'Kotventure',
    artifactId: ARTIFACT_ID,
    outputDirectory,
    apiUrl: ARTIFACT_API_URL,
    token: TEST_TOKEN,
    fetchImpl,
  };
}

export function makeTempDirectory(t: TestContext, prefix: string): string {
  const directory = fs.mkdtempSync(
    path.join(os.tmpdir(), prefix),
  );

  t.after(() => {
    fs.rmSync(directory, {
      recursive: true,
      force: true,
    });
  });

  return directory;
}

export {
  ARTIFACT_API_URL,
  ARTIFACT_ID,
  ARTIFACT_STORAGE_URL,
  BASE_SHA,
  HEAD_SHA,
  HEAD_REPOSITORY,
  REPOSITORY,
};
