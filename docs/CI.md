# Continuous integration

How GitHub Actions is organized for Kotventure.

For local development commands, see [CONTRIBUTING.md](../.github/CONTRIBUTING.md). For release automation, see
[RELEASING.md](./RELEASING.md). For Minecraft vanilla conformance, see
[vanilla-conformance.md](./vanilla-conformance.md).

## Workflow map

| Workflow | File | Triggers | Purpose |
|----------|------|----------|---------|
| **CI** | `ci.yml` | PR, push, weekly schedule, `workflow_dispatch` | Gate → path filter → parallel lint + build + analysis; required status check always reports |
| **PR** | `pr.yml` | `pull_request_target` | Title + commit validation, area labels |
| **Release** | `release.yml` | push `master` | Opens/updates release PRs; tags/releases after merge |
| **OpenSSF Scorecard** | `scorecard.yml` | weekly schedule, `branch_protection_rule`, `workflow_dispatch` | Supply-chain scorecard + SARIF |

## CI pipeline tiers

```
CI
│
├─ Tier 0: Triage ─────────────────────────────────────────────────
│   ├─ Gate              (skip release-please PRs + merge commits)
│   └─ Detect changes    (path filter → code, vanilla, per-module)
│
├─ Tier 1: Core (parallel, fast feedback) ─────────────────────────
│   ├─ Lint              (spotlessCheck + ktlintCheck)
│   └─ Build             (compile + test + Dokka + Kover + scan)
│       ├─ 📊 Coverage comment (PR)
│       ├─ 📦 Artifact size guard (PR)
│       └─ 📖 Dokka preview artifact (PR)
│
├─ Tier 2: Deep Analysis (after Tier 1 passes) ────────────────────
│   ├─ CodeQL            (actions + java-kotlin matrix)
│   ├─ Qodana            (static analysis + SARIF)
│   └─ Vanilla conformance  (MC-backed selector tests, path-filtered)
│
├─ Policy (independent of tiers) ──────────────────────────────────
│   ├─ Dependencies      (dependency-review-action, PRs only)
│   └─ Commits           (push-to-master subject validation)
│
└─ Status (required merge-gate check) ─────────────────────────────
    └─ Aggregates Tier 1 + Vanilla + Dependencies
```

Tier 2 runs only after Tier 1 passes — no point running expensive analysis on code that doesn't compile
or pass lint. The Status job always runs and reports a single required check that gates merges.

The `merge_group` trigger is configured so the workflow is ready for GitHub's merge queue.

## When workflows run

### Code paths

Defined inline in `ci.yml` (the `changes` job path filter):

- `modules/**`, `gradle/**`, `buildSrc/**`
- `build.gradle`, `settings.gradle`, `gradle.properties`, `gradlew`, `gradlew.bat`
- `.editorconfig`, `.gitattributes`, `.gitignore`
- `qodana.yaml`, `jitpack.yml`, `release-please-config.json`
- `.github/workflows/**`, `.github/actions/**`, `.github/scripts/**`, `.github/dependabot.yml`

### Push vs PR

| Event | CI workflow | Heavy jobs | Qodana | CodeQL |
|-------|:-----------:|:----------:|:------:|:------:|
| PR (code paths) | ✓ | ✓ | ✓ | ✓ |
| PR (docs/process only) | ✓ | — | — | — |
| Push to `master` (code paths) | ✓ | ✓ | ✓ | ✓ |
| Push to `master` (docs only) | ✓ | — | — | — |
| Weekly schedule | ✓ | ✓ | ✓ | ✓ |
| `workflow_dispatch` | ✓ | ✓ | ✓ | ✓ |

### Manual CI (`workflow_dispatch`)

Actions → **CI** → **Run workflow**. Always runs (path filter skipped).

| Input | Default | Behaviour |
|-------|---------|-----------|
| `tasks` | empty | Default full set: `build dokkaGenerate koverXmlReport koverHtmlReport`. If only `module` is set: `:<module>:build`. |
| `module` | empty | Optional project name (`core`, `minimessage`, `bom`, …). Ignored when `tasks` is non-empty. |

Module names must match `[A-Za-z0-9_-]+`.

### Heavy CI gate (release-please)

Integrated into the CI workflow's `gate` job. Handles both PRs and push-to-master merge commits.

Skips heavy jobs when:

- **PR:** head branch starts with `release-please--` and changed files are release-only
- **Push:** commit message matches `chore(master): release` and changed files are release-only

Release-only files: `CHANGELOG.md`, `.release-please-manifest.json`, `gradle/libs.versions.toml`.

When adding release-please `extra-files`, update the gate allow-list in `ci.yml`.

### Module-scoped builds (PRs)

On PRs, the path filter detects which modules changed. When only a subset changed (and `buildSrc`/`gradle`
are untouched), the Build job runs only `:<module>:build` for affected modules instead of a full `build`.
Push-to-master, schedule, and dispatch always run the full build.

### Merge queue

The CI workflow includes a `merge_group` trigger, making it compatible with GitHub's merge queue. Enable
the merge queue in repository settings when ready — CI will automatically validate the merged result.

## PR workflow jobs

```
PR
├─ Title     (conventional PR title validation)
├─ Commits   (conventional commit subject validation)
└─ Labels    (path → area:* labels)
```

Title and Commits are required status checks.

## Local composite actions

| Action | Path | Used by |
|--------|------|---------|
| **gradle-job** | `.github/actions/gradle-job` | CI (Lint, Build) — JDK/Gradle setup + run tasks + build scan + job summary |
| **setup-jdk-gradle** | `.github/actions/setup-jdk-gradle` | gradle-job, Vanilla, CodeQL — JDK + Gradle wrapper/dependency caches |
| **publish-junit-report** | `.github/actions/publish-junit-report` | CI (Build, Vanilla) — JUnit XML → Checks annotations |
| **coverage-comment** | `.github/actions/coverage-comment` | CI (Build, PRs) — per-module coverage table as PR comment |
| **artifact-size-guard** | `.github/actions/artifact-size-guard` | CI (Build, PRs) — JAR size tracking with growth warnings |

## Scripts

| Script | Role |
|--------|------|
| `validate-conventional-title.sh` | Title/commit subject format |
| `check-one-declaration-per-file.sh` | One top-level class/interface/object per main-source file |
| `normalize-qodana-sarif.sh` | Fix 0-based SARIF regions for GitHub code scanning |
| `write-gradle-job-summary.sh` | Job summary: Java/Gradle/Kotlin versions + failed tasks |
| `vanilla-fixture-cache-key.sh` | Compute MC fixture cache key |

## Action pins and Dependabot

Third-party actions are SHA-pinned with a version comment. Dependabot updates (`github-actions` in
`.github/dependabot.yml`):

- `/` — workflows
- `/.github/actions/gradle-job`
- `/.github/actions/setup-jdk-gradle`
- `/.github/actions/publish-junit-report`

New composites that pin third-party actions need a matching Dependabot directory.

| Ecosystem | Grouping | Open PR limit |
|-----------|----------|---------------|
| Gradle (`/`) | Minor + patch grouped (`gradle-minor-patch`); **majors ungrouped** | 10 |
| GitHub Actions (root + composites) | Minor + patch grouped; majors ungrouped | 10 (root), 5 (composites) |

## Branch protection (`master`)

Repository ruleset **Master** (default branch):

- Block force-push, branch deletion, and direct pushes (updates only via PR).
- Pull requests: one approving review, code-owner review, dismiss stale reviews, resolve conversations, squash only.
- Required status checks — see [Required vs optional checks](#required-vs-optional-checks).
- Code scanning gate for Qodana (`QDJVM`) alerts at medium-or-higher security / errors severity.
- Maintainer bypass remains configured for emergency overrides.

[CODEOWNERS](../.github/CODEOWNERS) assigns `@LMLiam` as default owner and explicitly for `.github/workflows/`,
`.github/actions/`, `.github/scripts/`, and related CI config so code-owner review covers automation changes.

## Required vs optional checks

PRs show many checks; only a subset is merge-blocking via the **Master** ruleset.

| Check | Merge gate | Notes |
|-------|:----------:|-------|
| **Status** | **Required** | Aggregates lint + build + vanilla + deps; green when skipped (docs-only / release-please) |
| **Title** | **Required** | Conventional PR title (from `pr.yml`) |
| **Commits** | **Required** | Conventional commit subjects (from `pr.yml`) |
| **Dependencies** | **Required** | Dependency review |
| Lint / Build | No | Under the Status aggregator |
| Vanilla conformance | No | Under the Status aggregator; path-filtered |
| Qodana / QDJVM | No* | QDJVM code-scanning alerts are ruleset-gated |
| CodeQL | No | SARIF to code scanning |
| Scorecard | No | Schedule / dispatch |
| Labels | No | Labelling only |

\*Qodana is not a required status check. Serious findings surface through the QDJVM code-scanning ruleset rule.

## Performance

| Mechanism | Where |
|-----------|--------|
| Configuration cache + local build cache | `gradle.properties`; CI restores via `setup-gradle` |
| Dependency / wrapper caches | `setup-gradle` defaults in `.github/actions/setup-jdk-gradle` |
| Minecraft conformance fixtures | `actions/cache`; key derived from `targetMinecraftVersion` and `serverBundleSha1` |

### Artifacts (Build job)

| Artifact | When |
|----------|------|
| Test results / HTML test reports | Always (including failed runs) |
| Kover coverage report | Always (including failed runs) |
| Dokka preview | PRs only (on success) — rendered KDoc HTML, 14-day retention |
| Module jars under `build/libs` | Only on **job failure**, or on **push to `master`** |

## Re-running CI

- **Re-run failed jobs** / **Re-run all jobs** on an Actions run.
- CI and Scorecard support `workflow_dispatch` (CI accepts optional `tasks` / `module` inputs).

## Related docs

- [RELEASING.md](./RELEASING.md)
- [vanilla-conformance.md](./vanilla-conformance.md)
- [DESIGN.md](./DESIGN.md)
