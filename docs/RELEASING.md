# Releasing

Kotventure uses release-please to turn conventional commits on `master` into release pull requests. Release PRs update
`CHANGELOG.md`, bump `versions.project` in `gradle/libs.versions.toml`, update `.release-please-manifest.json`, and
create the Git tag and GitHub Release after the Release PR is merged.

## Release Automation

The `Release Please` workflow runs on pushes to `master`. It reads `release-please-config.json` and
`.release-please-manifest.json`, then opens or updates one root Release PR for the repository.

The workflow uses the `RELEASE_PLEASE_TOKEN` secret when it is configured, falling back to `GITHUB_TOKEN` otherwise.
Prefer `RELEASE_PLEASE_TOKEN` for normal releases because PRs, tags, and releases created with the built-in
`GITHUB_TOKEN` do not trigger follow-up workflows.

`RELEASE_PLEASE_TOKEN` is typically a fine-grained personal access token (or GitHub App installation token) with
repository access to write contents, issues, and pull requests. Commits and PRs created with a **personal** PAT are
attributed to that user account, so they look like human activity rather than a bot identity. Heavy CI therefore uses a
**path-based** release-please gate (branch prefix + allow-listed files), not bot-actor detection — see [CI.md](./CI.md).
A dedicated GitHub App or machine user token keeps the same workflow shape while making automation identity clearer.

## Version Policy

Release automation follows the roadmap version ranges literally:

- `0.0.x` is reserved for the pre-alpha spike.
- The first feature release after `0.0.x` is `0.1.0`, which marks the start of the alpha line.
- Before `1.0.0`, `feat(...)` and breaking changes bump the minor version; patch releases remain available for
  non-feature follow-up work on the current minor line.

When adjusting release automation, verify the policy still matches [ROADMAP.md](./ROADMAP.md) and the phase ranges in
[DESIGN.md](./DESIGN.md).

## Branch Protection

Keep `master` protected and release through the Release PR. The **Master** ruleset requires a PR, reviews, and the
status checks listed in [CI.md](./CI.md) (including `Build, Test, and Lint` and Dependency Review). Release automation
must create and update the Release PR branch and apply release labels; merge only after those required checks pass.

Use `RELEASE_PLEASE_TOKEN` so the Release PR triggers the same CI workflows as human-authored PRs. If the repository
falls back to `GITHUB_TOKEN`, release-please can still open PRs, but GitHub will suppress workflows triggered by that
token's PR, tag, and release events.

Pure release-please PRs (changelog, manifest, and `gradle/libs.versions.toml` only) skip Build/Qodana/CodeQL heavy work;
Dependency Review, titles, and labels still run (and remain required). See [CI.md](./CI.md).

## Publishing Coordination

This workflow creates GitHub Releases and tags only. Maven Central publishing remains explicit and is tracked separately
by the Central release work in #59. Do not add a publish-on-release workflow until that publishing setup lands and its
required secrets, signing keys, and staging policy are documented.
