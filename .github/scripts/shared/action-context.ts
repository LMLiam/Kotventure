import type { context, getOctokit } from '@actions/github';
import type * as core from '@actions/core';

export interface ActionContext {
  readonly github: ReturnType<typeof getOctokit>;
  readonly context: typeof context;
  readonly core: typeof core;
}
