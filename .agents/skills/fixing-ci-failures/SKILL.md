---
name: fixing-ci-failures
description: Use when a GitHub Actions check is red or unexpected on a Kotventure PR — Build/Test/Lint failures, koverVerify coverage gate, ktlint/spotless formatting, conventional title/commit validation, Qodana or CodeQL alerts, dependency review, release-please or vanilla-conformance behaviour — or when deciding whether a check is real signal or known noise.
---

# Fixing CI failures

Architecture and trigger rules live in [`docs/CI.md`](../../../docs/CI.md) — read it for
*why* a workflow ran. This skill is the *what to do* when something is red.

**First move:** open the failing job's **summary** (Format and lint / Gradle build always
write one: toolchain versions + failed task names), then reproduce locally — never push
blind "retry" commits.

## Merge-blocking checks (fix these; ignore-list further down)

| Red check | Cause | Fix |
|---|---|---|
| **Build, Test, and Lint** (aggregator) | One of the two nested jobs failed | Open the failed nested job below |
| Format and lint — declaration check | More than one top-level class/interface/object in a main-source file | Split the file (AGENTS.md §5 hard rule); reproduce with `.github/scripts/check-one-declaration-per-file.sh` |
| Format and lint | `spotlessCheck` / `ktlintCheck` | `./gradlew ktlintFormat` (or `spotlessApply`), commit. **Continuation indent is 8 spaces (IntelliJ style) and the ktlint indent rule is disabled** — formatters won't fix indentation; write it by hand or IDE-reformat |
| Gradle build — test failure | Kotest suite red | `./gradlew test` locally; failed-run artifacts include HTML test reports |
| Gradle build — `koverVerify` | Aggregated line coverage < 85% (`gradle/coverage.gradle`) | **Add tests — never lower the threshold to pass.** `./gradlew koverHtmlReport` → `build/reports/kover/html/index.html` shows uncovered lines per class. (Raising the threshold intentionally: see policy in `.github/CONTRIBUTING.md`) |
| Gradle build — compile | Often `explicitApi()`: missing visibility / return type on public API | Add explicit modifiers + KDoc; see `documenting-public-api` |
| **Validate Pull Request Title** / **Validate Commit Subjects** | Not `verb(area): something` (lowercase, scope required — `^[a-z]+\([a-z0-9][a-z0-9-]*\): \S.*$`) | Edit the PR title in the UI; for commits, rewrite with `git rebase` and force-push — history rewriting on feature branches is fine here |
| **Review Dependency Changes** | New/updated dependency has a moderate+ advisory | Bump to a patched version or justify/replace; not heavy-CI-gated, runs on every PR |

## Known noise — do not "fix" these

- **Qodana "incorrect formatting" on Kotest `StringSpec` bodies** — false positive; leave it.
- **The neutral "Qodana for JVM" check** — informational; only **QDJVM code-scanning alerts**
  at medium+ security/errors severity gate the merge (repo ruleset).
- **CodeRabbit failing/absent** — its credits can run out; not a required check.
- Labeler, Scorecard, CodeQL `Analyze (…)` statuses — not merge gates (CodeQL findings surface
  via code scanning, still worth reading).

## release-please

- **Never hand-edit `CHANGELOG.md`** — release-please generates it from conventional commit
  subjects. Fix a wrong changelog entry by fixing the squash-merge subject, not the file.
- Pure release PRs (branch `release-please--*`, touching only `CHANGELOG.md`,
  `.release-please-manifest.json`, `gradle/libs.versions.toml`) skip Build/Qodana/CodeQL via
  the heavy-CI gate; the required check still reports green. If a release PR unexpectedly
  runs heavy CI, it has an extra changed path.
- Adding release-please `extra-files`? Update the gate allow-list in
  `.github/workflows/heavy-ci-gate.yml` in the same PR.
- Release flow details: [`docs/RELEASING.md`](../../../docs/RELEASING.md).

## Vanilla conformance

Path-filtered, MC-server-backed selector tests ([`docs/vanilla-conformance.md`](../../../docs/vanilla-conformance.md)).
A failure means the typed selector DSL disagrees with the real parser — treat it as a
correctness bug in the DSL or the conformance fixtures, not as flake. Server bundle is cached
by SHA-1; a cache-download failure is retryable.

## Retrying & manual runs

- Actions → failed run → **Re-run failed jobs** for genuine infra flake only.
- Build supports `workflow_dispatch` with optional `tasks` (default
  `build dokkaGenerate koverXmlReport koverHtmlReport`) and `module` (runs
  `:<module>:build`) inputs; path filters are skipped on manual runs.
- Docs-only PRs legitimately skip Gradle/Qodana — a green aggregator with skipped jobs is
  correct, not a bug (markdown under `modules/**` counts as code, though).
