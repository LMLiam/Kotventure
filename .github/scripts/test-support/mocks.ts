import type * as core from '@actions/core';
import type { ActionContext, Octokit } from '../shared/action-context.js';

/**
 * The endpoints the tooling invokes, keyed as `area.method`. The github mock
 * contract check below verifies each endpoint the mock provides is a function,
 * so a fixture that omits or mistypes a method the production code now calls
 * fails loudly instead of throwing a runtime TypeError mid-test.
 */
const TOOLING_ENDPOINTS = [
  'actions.getWorkflow',
  'actions.getWorkflowRun',
  'actions.listJobsForWorkflowRun',
  'actions.listWorkflowRunArtifacts',
  'actions.listWorkflowRuns',
  'checks.create',
  'checks.get',
  'checks.listForRef',
  'checks.update',
  'issues.createComment',
  'issues.listComments',
  'pulls.get',
  'pulls.listFiles',
  'repos.compareCommitsWithBasehead',
  'repos.get',
  'repos.listPullRequestsAssociatedWithCommit',
] as const;

export interface TestGithubMock {
  rest: { [area: string]: { [method: string]: unknown } };
  paginate?: (method: (...args: never[]) => object, ...parameters: never[]) => Promise<object[]>;
}

function mockEndpointValue(mock: TestGithubMock, endpoint: string): unknown {
  const separator = endpoint.indexOf('.');
  if (separator < 1) return undefined;
  const area = endpoint.slice(0, separator);
  const method = endpoint.slice(separator + 1);
  return mock.rest[area]?.[method];
}

/**
 * Presents a structurally typed github mock at the `Octokit` boundary. The
 * real `@actions/github` client cannot be constructed at test time (the
 * package is ESM-only), so this adapter is the single place the mock is
 * widened to the production type.
 */
export function mockOctokit(mock: TestGithubMock): Octokit {
  if (typeof mock !== 'object' || mock === null) throw new Error('mock github client must be an object');

  if (typeof mock.rest !== 'object' || mock.rest === null) {
    throw new Error('mock github client rest must be an object');
  }

  for (const endpoint of TOOLING_ENDPOINTS) {
    const value = mockEndpointValue(mock, endpoint);
    if (value !== undefined && typeof value !== 'function') {
      throw new Error(`mock github endpoint ${endpoint} must be a function`);
    }
  }

  if (mock.paginate !== undefined && typeof mock.paginate !== 'function') {
    throw new Error('mock github client paginate must be a function');
  }

  return mock as unknown as Octokit;
}

export interface TestContextMock {
  repo: { owner: string; repo: string };
  eventName?: string;
  serverUrl?: string;
  payload?: object;
}

/**
 * Presents a minimal action context at the `ActionContext['context']`
 * boundary. The production context type is the full `@actions/github`
 * context object; the tests supply only the fields the tooling reads.
 */
export function mockContext(context: TestContextMock): ActionContext['context'] {
  if (typeof context !== 'object' || context === null) throw new Error('mock action context must be an object');

  if (typeof context.repo?.owner !== 'string' || typeof context.repo?.repo !== 'string') {
    throw new Error('mock action context repository is invalid');
  }

  return context as unknown as ActionContext['context'];
}

export interface TestCoreMock {
  warning?: (message: string | Error) => void;
  info?: (message: string | Error) => void;
  setOutput?: (name: string, value: string | boolean | number) => void;
}

/**
 * Presents a minimal `@actions/core` mock at the production core boundary.
 */
export function mockCore(coreMock: TestCoreMock): typeof core {
  if (typeof coreMock !== 'object' || coreMock === null) throw new Error('mock core must be an object');

  return coreMock as unknown as typeof core;
}

export interface MockFetchOptions {
  redirect?: 'manual' | 'follow';
  headers?: { readonly [name: string]: string | undefined };
  signal?: { readonly aborted: boolean };
}

export interface MockFetchRequest {
  location: string;
  options: MockFetchOptions;
}

export interface MockFetchResponse {
  status?: number;
  ok?: boolean;
  headers?: Headers | { readonly [name: string]: string | undefined };
  body?: {
    getReader(): {
      read(): Promise<{ done: boolean; value?: Uint8Array | null }>;
      cancel(): Promise<void>;
    };
  };
  arrayBuffer?: () => Promise<ArrayBuffer | Uint8Array>;
}

export type MockFetchImpl = (location: string, options: MockFetchOptions) => Promise<MockFetchResponse>;

/**
 * Presents a fetch mock at the `typeof fetch` boundary. The mock returns the
 * response shape the artifact downloader reads, never a full `Response`.
 */
export function mockFetchImpl(impl: MockFetchImpl): typeof fetch {
  if (typeof impl !== 'function') throw new Error('mock fetch implementation must be a function');

  return impl as unknown as typeof fetch;
}

/**
 * Presents a fixture record at a typed octokit response-data boundary. The
 * records deliberately carry only the fields the tooling reads, so they are
 * not assignable to the full octokit data types without this adapter.
 */
export function asApiData<T extends object>(value: object): T {
  return value as T;
}
