import type { context, getOctokit } from '@actions/github';
import type * as core from '@actions/core';

/**
 * The dependency shape that actions/github-script and the pr-metrics-comment
 * composite action inject into Kotventure's CI scripts.
 *
 * Mirrors github-script v9's own `AsyncFunctionArguments` declaration:
 * `context: typeof context`, `core: typeof core`, and
 * `github: ReturnType<typeof getOctokit>`, all from the maintained toolkit
 * packages. All imports are type-only and erased at emit.
 */
export interface ActionContext {
  readonly github: ReturnType<typeof getOctokit>;
  readonly context: typeof context;
  readonly core: typeof core;
}
