---
name: fixing-ci-failures
description: >-
  Use this skill to investigate a failed or unusual GitHub Actions check on a Kotventure pull request. It covers build,
  test, lint, coverage, format, title, dependency, analysis, release, and conformance checks.
---

# Fixing CI failures

[`docs/CI.md`](../../../docs/CI.md) describes the architecture and trigger rules. Read it to learn why a workflow
started. This skill specifies how to investigate a failed check.

First, open the failed job's **summary**. Lint and Build write toolchain versions and failed task names there. Then,
reproduce the failure locally. Do not push a commit only to start the check again.

## Checks that block a merge

| Red check | Cause | Fix |
|---|---|---|
| **Status** (aggregator) | Lint, Build, or Dependencies failed | Open the failed nested job below |
| Lint: declaration check | More than one top-level class/interface/object in a main-source file | Split the file. Refer to section 5 of AGENTS.md. Reproduce with `.github/scripts/check-one-declaration-per-file.sh`. |
| Lint | `spotlessCheck` / `ktlintCheck` | Run `./gradlew ktlintFormat` or `spotlessApply`, and commit the result. Continuation indents have eight spaces. The ktlint indent rule is disabled, so use the IDE or edit indentation manually. |
| Build: test failure | Kotest suite failed | Run `./gradlew test` locally. Failed-run artefacts include HTML test reports. |
| Build: `koverVerify` | Aggregated line coverage below 85 percent | Add tests. Do not decrease the threshold. Run `./gradlew koverHtmlReport` and open `build/reports/kover/html/index.html` to find uncovered lines. Refer to the policy in `.github/CONTRIBUTING.md` before you increase the threshold. |
| Build: compile | Public API frequently has no visibility or return type | Add explicit modifiers and KDoc. Refer to `documenting-public-api`. |
| **Title** / **Commits** | Does not match `verb(area): something` | Edit the pull-request title. For commits, use `git rebase` and force-push the feature branch. The pattern is `^[a-z]+\([a-z0-9][a-z0-9-]*\): \S.*$`. |
| **Dependencies** | New or updated dependency has a moderate or higher advisory | Update to a corrected version, replace the dependency, or give a justification. This check starts on each pull request. |

## Known non-errors

- Do not change Kotest `StringSpec` bodies for a Qodana "incorrect formatting" message. It is a false positive.
- The neutral "Qodana for JVM" check is informational. Only QDJVM alerts with medium or higher security severity, or
  error severity, block the merge.
- CodeRabbit can fail or be absent when its credits are exhausted. It is not a required check.
- Labeler, Scorecard, and CodeQL `Analyze (…)` statuses do not block a merge. Read CodeQL findings in code scanning.

## release-please

- **Do not edit `CHANGELOG.md` manually.** Release-please generates it from conventional commit subjects. Correct the
  squash-merge subject to correct a changelog entry.
- Pure release PRs (branch `release-please--*`, touching only `CHANGELOG.md` and
  `.release-please-manifest.json`) skip resource-intensive CI jobs through the Gate job in `ci.yml`. The
  required Status check still reports green. Version-catalog changes
  (`gradle/libs.versions.toml`) always run heavy CI. If a release PR unexpectedly runs
  heavy CI, it has an extra changed path.
- When you add release-please `extra-files`, update the gate allowlist in the `gate` job of
  `.github/workflows/ci.yml` in the same pull request.
- Release flow details: [`docs/RELEASING.md`](../../../docs/RELEASING.md).

## Vanilla conformance

Path-filtered, MC-server-backed selector tests ([`docs/vanilla-conformance.md`](../../../docs/vanilla-conformance.md)).
A failure means that the typed selector DSL and the vanilla parser disagree. Investigate the DSL and the conformance
fixtures. The cache uses the server bundle SHA-1. You can start the check again after a cache download failure.

## Retrying & manual runs

- Select Actions → failed run → **Re-run failed jobs** only for an infrastructure failure.
- CI supports `workflow_dispatch` with optional `tasks` (default
  `build dokkaGenerate koverXmlReport koverHtmlReport`) and `module` (runs
  `:<module>:build` plus root verification: kover/BOM/release/Dokka reports) inputs. Manual, scheduled, and merge-queue
  runs do not use path filters. The default pull-request and push Build uses the full multiproject task set.
- A documentation-only pull request correctly skips resource-intensive jobs. A green Status check with skipped jobs is
  correct. Markdown under `modules/**` counts as code.
