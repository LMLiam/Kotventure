import type { context, getOctokit } from '@actions/github';
import type * as core from '@actions/core';

/**
 * The dependency shape that actions/github-script and the pr-metrics-comment
 * composite action inject into Kotventure's CI scripts. All imports are
 * type-only and erased at emit.
 */
export interface ActionContext {
  readonly github: ReturnType<typeof getOctokit>;
  readonly context: typeof context;
  readonly core: typeof core;
}
