# Releasing

Kotventure uses release-please to make release pull requests from conventional commits on `master`.
Release PRs update `CHANGELOG.md`, `versions.project`, and `.release-please-manifest.json`.
After the release PR merge, release-please makes the Git tag and GitHub Release.

## Release Automation

The `Release Please` workflow runs after a push to `master`. It reads
`release-please-config.json` and `.release-please-manifest.json`. Then, it
opens or updates one root release PR.

The workflow creates a short-lived installation token for the dedicated GitHub
App `release-please-kotventure`. The resulting bot login is
`release-please-kotventure[bot]`. Release Please receives this token explicitly.
The workflow does not use a personal access token, a machine-user token, or a
fallback `GITHUB_TOKEN` token.

Install the App only on `LMLiam/Kotventure`. Give it these repository
permissions:

- Metadata: read.
- Contents: read and write.
- Issues: read and write.
- Pull requests: read and write.

Store the App ID in the Actions variable `RELEASE_PLEASE_APP_ID`. Store the
private key in the Actions secret `RELEASE_PLEASE_APP_PRIVATE_KEY`. Do not
print or commit the private key.

The workflow scopes the installation token to this repository. The action
revokes the token after the job. Refer to [CI.md](./CI.md) for the trust and
permission model.

## Release PR provenance

The Release Please branch is `release-please--branches--master`. Configure an
active `Protect Release Please branches` ruleset for
`refs/heads/release-please--branches--*`.

The ruleset must allow only the `release-please-kotventure` App Integration to
create, update, or delete these branches. It must block force pushes. It must
not give bypass access to administrators, repository roles, teams, users,
`github-actions[bot]`, or unrelated GitHub Apps.

Treat the active ruleset as a deployment prerequisite. Do not merge or enable
the workflow changes until GitHub confirms the ruleset, ref pattern, and sole
App bypass. Keep full CI enabled until this verification is complete.

The CI gate skips heavy jobs only after `release-provenance.yml` verifies the
App identity, repository, branch, event sender, and complete release-file
allowlist. The provenance workflow runs from the default branch. It does not
check out the pull request.

Human edits, human commits, fork pull requests, copied titles, copied labels,
and release-like branches run normal CI. Titles and labels are not
authentication data.

PR #343 is human-authored. Do not merge or close it in this phase. It runs full
CI. A later App-created Release Please PR is required for the release-only
optimisation.

## App setup and key rotation

If the App does not exist, register `release-please-kotventure` in GitHub.
Do not add `[bot]` to the registered App name. GitHub adds the suffix to the
bot login.

Install the App only on `LMLiam/Kotventure`. Disable webhooks. Generate a
private key and store it directly in the repository secret. Do not paste the
key into chat or a terminal command.

If the key is exposed or rotation is due, generate a new key in GitHub. Store
the new key in `RELEASE_PLEASE_APP_PRIVATE_KEY`. Run a controlled Release
Please test. Revoke the old key only after the new key works.

If the App must be replaced, disable the release workflow first. Install the
replacement App only on this repository. Update the variable, secret, and
ruleset bypass. Verify the App bot login and branch ruleset. Then enable the
workflow.

If a Release Please run cannot update its branch, check the App installation,
repository scope, App permissions, variable name, secret presence, and the
ruleset Integration ID. Do not add an administrator or role bypass.

## Version Policy

Release automation uses the roadmap version ranges:

- `0.0.x` is reserved for the pre-alpha spike.
- The first feature release after `0.0.x` is `0.1.0`, which marks the start of the alpha line.
- Before `1.0.0`, `feat(...)` and breaking changes increase the minor version.
  Use patch releases for other work on the current minor line.

After a release automation change, compare the policy with [ROADMAP.md](./ROADMAP.md) and [DESIGN.md](./DESIGN.md).

## Branch Protection

Keep `master` protected and release through the release PR. The **Master**
ruleset requires a PR, reviews, and specified status checks. [CI.md](./CI.md)
lists these checks. Release automation must update the release branch and
apply release labels. Merge the release PR only after all required checks pass.

A trusted pure Release Please PR changes only the changelog, manifest, and
`gradle/libs.versions.toml`. It does not run the heavy Build, Qodana, and
CodeQL work. Dependency Review and conventional-title checks remain required.
An untrusted release candidate runs normal CI. The workflow also applies
labels. Refer to [CI.md](./CI.md).

If rollback is required, keep full CI enabled. Disable the release workflow
before changing the App. Disable the release-branch ruleset before removing
its bypass. Remove the repository variable and secret without reading the
private key. Uninstall the App and revoke its key through GitHub. Do not
restore the old personal access token or the weak branch-name check.

## Publishing Coordination

This workflow makes only GitHub Releases and tags. Issue #59 tracks the separate Maven Central publication work.
Do not add automatic publication before issue #59 is complete. First, document the necessary secrets, signing keys, and staging policy.
