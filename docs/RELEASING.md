# Releasing

Kotventure uses release-please to make release pull requests from conventional commits on `master`.
Release PRs update `CHANGELOG.md`, `versions.project`, and `.release-please-manifest.json`.
After the release PR merge, release-please makes the Git tag and GitHub Release.

## Release Automation

The `Release Please` workflow runs after a push to `master`. It reads `release-please-config.json` and
`.release-please-manifest.json`. Then, it opens or updates one root release PR.

The workflow uses `RELEASE_PLEASE_TOKEN` when it is available. If it is not available, the workflow uses `GITHUB_TOKEN`.
Use `RELEASE_PLEASE_TOKEN` for normal releases. Items made with `GITHUB_TOKEN` do not start subsequent workflows.

`RELEASE_PLEASE_TOKEN` is usually a fine-grained personal access token or GitHub App installation token.
It needs repository permission to write contents, issues, and pull requests. GitHub attributes a personal token to its user account.
Thus, heavy CI uses a **path-based** release-please gate instead of actor detection. Refer to [CI.md](./CI.md).
A GitHub App or machine-user token gives the automation a clear identity with the same workflow.

## Version Policy

Release automation uses the roadmap version ranges:

- `0.0.x` is reserved for the pre-alpha spike.
- The first feature release after `0.0.x` is `0.1.0`, which marks the start of the alpha line.
- Before `1.0.0`, `feat(...)` and breaking changes increase the minor version.
  Use patch releases for other work on the current minor line.

After a release automation change, compare the policy with [ROADMAP.md](./ROADMAP.md) and [DESIGN.md](./DESIGN.md).

## Branch Protection

Keep `master` protected and release through the release PR. The **Master** ruleset requires a PR, reviews, and specified status checks.
[CI.md](./CI.md) lists these checks. Release automation must update the release branch and apply release labels.
Merge the release PR only after all required checks pass.

Use `RELEASE_PLEASE_TOKEN` so the release PR starts the normal CI workflows.
Release-please can open a PR with `GITHUB_TOKEN`, but GitHub prevents subsequent workflows from that token.

A pure release-please PR changes only the changelog, manifest, and `gradle/libs.versions.toml`.
It does not run the heavy Build, Qodana, and CodeQL work. Dependency Review and conventional-title checks remain required.
The workflow also applies labels. Refer to [CI.md](./CI.md).

## Publishing Coordination

This workflow makes only GitHub Releases and tags. Issue #59 tracks the separate Maven Central publication work.
Do not add automatic publication before issue #59 is complete. First, document the necessary secrets, signing keys, and staging policy.
