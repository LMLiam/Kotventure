import type { context, getOctokit } from '@actions/github';
import type * as core from '@actions/core';

export interface ActionContext {
  readonly github: ReturnType<typeof getOctokit>;
  readonly context: typeof context;
  readonly core: typeof core;
}

type Octokit = ActionContext['github'];

export type { Octokit };

export type RepositoryData = Awaited<ReturnType<Octokit['rest']['repos']['get']>>['data'];
export type WorkflowRunData = Awaited<ReturnType<Octokit['rest']['actions']['getWorkflowRun']>>['data'];
export type WorkflowData = Awaited<ReturnType<Octokit['rest']['actions']['getWorkflow']>>['data'];
export type PullRequestData = Awaited<ReturnType<Octokit['rest']['pulls']['get']>>['data'];
export type PullRequestFile = Awaited<ReturnType<Octokit['rest']['pulls']['listFiles']>>['data'][number];
export type WorkflowRunListItem = Awaited<ReturnType<Octokit['rest']['actions']['listWorkflowRuns']>>['data']['workflow_runs'][number];
export type JobItem = Awaited<ReturnType<Octokit['rest']['actions']['listJobsForWorkflowRun']>>['data']['jobs'][number];
export type WorkflowRunArtifact = Awaited<ReturnType<Octokit['rest']['actions']['listWorkflowRunArtifacts']>>['data']['artifacts'][number];
export type CheckRunData = Awaited<ReturnType<Octokit['rest']['checks']['create']>>['data'];
