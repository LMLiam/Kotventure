import type { context, getOctokit } from '@actions/github';
import type * as core from '@actions/core';

/**
 * The dependency shape that actions/github-script and the pr-metrics-comment
 * composite action inject into Kotventure's CI scripts.
 *
 * The aggregate shape has no npm library to import: actions/github-script is a
 * GitHub-hosted action, not a published package, and @types/github-script does
 * not exist. Each field is therefore the maintained toolkit type that
 * github-script's own `AsyncFunctionArguments` declaration uses
 * (src/async-function.ts): `context: typeof context`, `core: typeof core`, and
 * `github: ReturnType<typeof getOctokit>`. The local declaration is only a
 * thin alias over maintained types, never hand-rolled structural typing. All
 * imports are type-only and erased at emit.
 */
export interface ActionContext {
  readonly github: ReturnType<typeof getOctokit>;
  readonly context: typeof context;
  readonly core: typeof core;
}
