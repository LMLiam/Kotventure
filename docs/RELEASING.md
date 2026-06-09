# Releasing

Kotventure uses release-please to turn conventional commits on `master` into release pull requests. Release PRs update
`CHANGELOG.md`, bump `versions.project` in `gradle/libs.versions.toml`, update `.release-please-manifest.json`, and
create the Git tag and GitHub Release after the Release PR is merged.

## Release Automation

The `Release Please` workflow runs on pushes to `master`. It reads `release-please-config.json` and
`.release-please-manifest.json`, then opens or updates one root Release PR for the repository.

The workflow uses the `RELEASE_PLEASE_TOKEN` secret when it is configured, falling back to `GITHUB_TOKEN` otherwise.
Prefer `RELEASE_PLEASE_TOKEN` for normal releases because PRs, tags, and releases created with the built-in
`GITHUB_TOKEN` do not trigger follow-up workflows. Use a fine-grained PAT or GitHub App token for the GitHub Actions bot
with repository access and the ability to write contents, issues, and pull requests.

## Branch Protection

Keep `master` protected and release through the Release PR. Branch protection must allow the GitHub Actions bot or the
release bot behind `RELEASE_PLEASE_TOKEN` to create and update the Release PR branch, apply release labels, and merge
only after the required checks pass.

If branch protection requires all PR checks, use `RELEASE_PLEASE_TOKEN` so the Release PR triggers the same CI workflows
as human-authored PRs. If the repository falls back to `GITHUB_TOKEN`, release-please can still open PRs, but GitHub will
suppress workflows triggered by that token's PR, tag, and release events.

## Publishing Coordination

This workflow creates GitHub Releases and tags only. Maven Central publishing remains explicit and is tracked separately
by the Central release work in #59. Do not add a publish-on-release workflow until that publishing setup lands and its
required secrets, signing keys, and staging policy are documented.
