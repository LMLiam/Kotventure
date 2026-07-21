# Continuous integration

This document explains the GitHub Actions configuration for Kotventure.

For local development commands, see [CONTRIBUTING.md](../.github/CONTRIBUTING.md). For release automation, see
[RELEASING.md](./RELEASING.md). For Minecraft vanilla conformance, see
[vanilla-conformance.md](./vanilla-conformance.md).

## Workflow map

| Workflow | File | Triggers | Purpose |
|----------|------|----------|---------|
| **CI** | `ci.yml` | PR, push, `merge_group`, weekly schedule, `workflow_dispatch` | Gate, path filter, parallel lint, build, and analysis. Always reports the required status check |
| **PR** | `pr.yml` | `pull_request_target` | Title + commit validation, area labels |
| **Release** | `release.yml` | push `master` | Opens or updates release PRs. Creates tags and releases after merge |
| **OpenSSF Scorecard** | `scorecard.yml` | weekly schedule, `branch_protection_rule`, `workflow_dispatch` | Supply-chain scorecard + SARIF |

## CI pipeline tiers

```
CI
│
├─ Tier 0: Triage ─────────────────────────────────────────────────
│   ├─ Gate              (skip release-please PRs + merge commits)
│   └─ Detect changes    (path filter → code, vanilla)
│
├─ Tier 1: Core (parallel, fast feedback) ─────────────────────────
│   ├─ Lint              (declaration check + spotlessCheck + ktlintCheck)
│   └─ Build             (full multi-project build + Dokka + Kover + Build Scan)
│       └─ PR feedback   (one non-gating metrics comment: coverage Δ + JAR sizes)
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

Tier 2 starts only after Tier 1 passes. This sequence prevents unnecessary analysis of code that does not compile or
pass lint. The Status job always starts. It reports one required check that controls merges.

The workflow listens for `merge_group` events. Merge groups, schedules, and manual dispatches do not use the path
filter. They always start the full pipeline: Build, Vanilla, Qodana, and CodeQL.

## When workflows run

### Code paths

The `changes` job in `ci.yml` defines these code paths:

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
| `merge_group` | ✓ | ✓ (path filter skipped) | ✓ | ✓ |
| Weekly schedule | ✓ | ✓ | ✓ | ✓ |
| `workflow_dispatch` | ✓ | ✓ | ✓ | ✓ |

### Manual CI (`workflow_dispatch`)

Select Actions → **CI** → **Run workflow**. A manual workflow always starts and does not use the path filter.

| Input | Default | Behaviour |
|-------|---------|-----------|
| `tasks` | empty | Default full set: `build dokkaGenerate koverXmlReport koverHtmlReport`. If only `module` is set: `:<module>:build` plus root verification (`koverVerify`, BOM/release checks, Kover reports, Dokka). |
| `module` | empty | Optional project name (`core`, `minimessage`, `bom`, …). Ignored when `tasks` is non-empty. |

Module names must match `[A-Za-z0-9_-]+`.

### Heavy CI gate (release-please)

The CI workflow contains this gate in its `gate` job. The gate handles pull requests and merge commits on `master`.

The gate skips resource-intensive jobs in these conditions:

- **PR:** The head branch starts with `release-please--`, and only release files changed.
- **Push:** The commit message matches `chore(master): release`, and only release files changed.

The release files are `CHANGELOG.md` and `.release-please-manifest.json`.

The gate does **not** skip `gradle/libs.versions.toml`. A version catalogue change always starts the full CI pipeline.

When you add release-please `extra-files`, update the gate allowlist in `ci.yml`.

### Full builds

Pull requests, pushes, merge groups, schedules, and manual dispatches use the complete default task set:
`build dokkaGenerate koverXmlReport koverHtmlReport`. Path filters control whether the full CI pipeline starts. They do
not control which modules compile. Thus, each code pull request includes the coverage gate, BOM checks, and dependent
module compilation.

### Merge queue

The CI configuration supports a merge queue. Both `ci.yml` and `pr.yml` listen for `merge_group`. Queue batches do not
use the path filter. They start the full pipeline, which includes Vanilla conformance. The Title and Commits jobs report
successful placeholders to keep their required checks present. Dependency review operates only on pull requests. The
Status job accepts a skipped dependency review on other events.

When the account supports the feature, enable the queue in the **Master** ruleset or in branch protection. Configure
`merge_queue` and squash merges. Until then, use the usual pull-request squash merge.

### PR metrics (coverage, patch coverage, sizes, API, tests)

After Build, the **PR feedback** job posts **one** bot comment (`<!-- pr-metrics -->`). The comment contains:

- A visible **verdict line** with total coverage, gate margin, patch coverage, aggregate JAR change, test count change,
  and public API change.
- **Patch coverage** from the pull-request diff and the Kover line data. It identifies uncovered added lines with
  ranges such as `file.kt:12–15`.
- Mermaid bar charts that show the changes in coverage and JAR size. The bars use the absolute change for their order.
  Collapsed tables contain the absolute values and `.class` entry counts.
- A **public API change** count for added and removed `public` declarations. A grep heuristic supplies this value until
  an apiDump baseline exists. A collapsed diff block shows the declarations.
- Collapsed **build statistics** with test counts, skipped-test counts, and approximate build time.
- Warnings for JAR growth greater than 10 percent, a coverage decrease of at least 0.5 percentage points, and coverage
  within 0.5 percentage points of the Kover gate. The job reads the threshold from `gradle/coverage.gradle`.
- Links to the workflow run, `dokka-preview` artefact, and `gradle-test-results` artefact.
- Only the verdict line and "No metric changes" when no metric changed.

The job searches for a baseline in this order:

1. The **Actions cache** key `ci-baseline-<base-sha>`. A successful push to `master` writes this key. It contains the
   Kover report, module JARs, and `ci-metrics.json`.
2. The `coverage-report`, `module-jars`, and `ci-metrics` **artefacts** from a successful CI run for the base commit.
3. A JAR-only Gradle build of the base SHA. If no base report is available, the coverage value stays absolute.

The `.github/actions/pr-metrics-comment` action builds the comment. Its `action.yml` file calls plain Node modules in
`lib/`. These modules parse patches, coverage, JARs, and ZIP files. Renderers are in `lib/sections/`. Tests in `test/`
use `node:test`. The Lint job starts these tests.

### Build Scans

Build scans are **off by default**. The `build-scan: true` input on `gradle-job` adds Develocity and publishes a public
scan. The `build-scan-publish: true` input on `setup-jdk-gradle` has the same effect. A scan greatly decreases local
build-cache hits. In one measurement, the same task set changed from approximately 55 hits in 26 seconds to one hit in
four minutes. Keep scans off for pull-request and push CI.

For an occasional diagnostic scan, give `build-scan: true` to `gradle-job`. The action accepts the Gradle terms through
`setup-gradle` and adds `--scan`. For a private Develocity server, configure its URL and access key on `setup-gradle`.

### Trust and permissions

| Surface | Behaviour |
|---------|-----------|
| Default workflow permissions | `contents: read` |
| Build job | Uses `checks: write` and `contents: read`. Cannot write to pull requests. Clears `GITHUB_TOKEN` for Gradle |
| PR feedback job | Uses `actions: read`, `pull-requests: write`, and `contents: read`. Posts one metrics comment. Uses the cache or artefacts before a base JAR-only build. Clears `GITHUB_TOKEN` for Gradle |
| Build scans | Off by default. Enable with `build-scan: true` |
| Dokka preview artefact | Contains untrusted HTML from the pull request. Retain for 14 days. Do not publish as Pages |

## PR workflow jobs

```
PR
├─ Title     (conventional PR title validation)
├─ Commits   (conventional commit subject validation)
└─ Labels    (path → area:* labels)
```

The Title and Commits jobs are required status checks.

## Local composite actions

| Action | Path | Used by |
|--------|------|---------|
| **gradle-job** | `.github/actions/gradle-job` | CI (Lint, Build): JDK and Gradle setup, tasks, Build Scan, and job summary |
| **setup-jdk-gradle** | `.github/actions/setup-jdk-gradle` | gradle-job, Vanilla, CodeQL, PR feedback fallback: JDK, Gradle caches, and scan TOS |
| **publish-junit-report** | `.github/actions/publish-junit-report` | CI (Build, Vanilla): JUnit XML to Checks annotations |
| **pr-metrics-comment** | `.github/actions/pr-metrics-comment` | CI (PR feedback): one coverage and JAR size comment |

Before Spotless and ktlint, Lint starts two additional checks. The declaration script permits one top-level type in
each main-source file. The `pr-metrics-comment` tests use `node --test`.

PR feedback does not control a merge because it uses `continue-on-error`. A failure does not fail Build or Status.

## Scripts

| Script | Role |
|--------|------|
| `validate-conventional-title.sh` | Title/commit subject format |
| `check-one-declaration-per-file.sh` | One top-level class/interface/object per main-source file |
| `normalize-qodana-sarif.sh` | Fix 0-based SARIF regions for GitHub code scanning |
| `write-gradle-job-summary.sh` | Job summary: Java/Gradle/Kotlin versions + failed tasks |
| `vanilla-fixture-cache-key.sh` | Compute MC fixture cache key |
| `download-base-metrics.sh` | PR feedback: fetch base coverage/jars/metrics from the base commit's CI run |
| `build-base-jars.sh` | PR feedback: last-resort jar-only Gradle build of the base SHA |
| `collect-ci-metrics.sh` | Build: test/skipped counts + build duration → `ci-metrics.json` |

## Action pins and Dependabot

Each third-party action uses a fixed SHA and has a version comment. One `github-actions` entry in
`.github/dependabot.yml` updates these actions. Its directories are `["/", "/.github/actions/*"]`. This pattern includes
new composite actions without a configuration change.

| Ecosystem | Grouping | Open PR limit |
|-----------|----------|---------------|
| Gradle (`/`) | Minor and patch updates grouped (`gradle-minor-patch`). Major updates ungrouped | 10 |
| GitHub Actions (root + composites) | Minor and patch updates grouped. Major updates ungrouped | 10 |

## Branch protection (`master`)

The **Master** repository ruleset protects the default branch. The rulesets API confirms these settings:

- Block force pushes, branch deletion, and direct pushes. Update the branch only through a pull request.
- Require one approval and a code-owner review. Dismiss stale reviews and resolve conversations. Permit only squash
  merges.
- Require **Status**, **Title**, **Commits**, and **Dependencies**. Require them to be current with the base branch.
- Block Qodana (`QDJVM`) alerts that have medium or higher security severity, or error severity.
- Permit maintainers to bypass the rules in an emergency.

[CODEOWNERS](../.github/CODEOWNERS) assigns `@LMLiam` as the default owner. It also assigns this owner to
`.github/workflows/`, `.github/actions/`, `.github/scripts/`, and related CI configuration. Thus, code-owner review
applies to automation changes.

## Required vs optional checks

Pull requests show many checks. Only the checks in the **Master** ruleset block a merge.

| Check | Merge gate | Notes |
|-------|:----------:|-------|
| **Status** | **Required** | Aggregates lint, build, vanilla, and dependencies. Green when skipped for docs-only or release-please changes |
| **Title** | **Required** | Conventional PR title (from `pr.yml`) |
| **Commits** | **Required** | Conventional commit subjects (from `pr.yml`) |
| **Dependencies** | **Required** | Dependency review |
| Lint / Build | No | Under the Status aggregator |
| PR feedback | No | Metrics comment only. Not part of Status |
| Vanilla conformance | No | Under the Status aggregator. Uses a path filter |
| Qodana / QDJVM | No* | QDJVM code-scanning alerts are ruleset-gated |
| CodeQL | No | SARIF to code scanning |
| Scorecard | No | Schedule / dispatch |
| Labels | No | Labelling only |

\*Qodana is not a required status check. The QDJVM code-scanning rule reports serious findings.

## Performance

| Mechanism | Where |
|-----------|--------|
| Configuration cache + local build cache | Configured in `gradle.properties`. CI restores it with `setup-gradle` |
| Dependency / wrapper caches | `setup-gradle` defaults in `.github/actions/setup-jdk-gradle` |
| Minecraft conformance fixtures | Uses `actions/cache`. The key comes from `targetMinecraftVersion` and `serverBundleSha1` |
| PR metrics baselines | Uses the `actions/cache` key `ci-baseline-<sha>` on a master push. Downloads an artefact as a fallback. Rebuilds the JAR only as the last option |

### Artefacts (Build job)

| Artefact | When |
|----------|------|
| Test results / HTML test reports | Always (including failed runs) |
| Kover coverage report | Always, including failed runs. Retained for 14 days |
| Module JARs (`module-jars`) | Always when present. Used for PR head metrics and as the base download fallback |
| CI metrics (`ci-metrics`) | On successful builds. It contains test counts and duration for the PR comment. |
| Dokka preview | Successful PRs only. Contains rendered KDoc HTML. Retained for 14 days. Treat as untrusted HTML |
| Full `build/libs` upload | Only on **job failure**, or on **push to `master`** |

## Re-running CI

- Select **Re-run failed jobs** or **Re-run all jobs** on an Actions run.
- CI and Scorecard support `workflow_dispatch`. CI accepts the optional `tasks` and `module` inputs.

## Related docs

- [RELEASING.md](./RELEASING.md)
- [vanilla-conformance.md](./vanilla-conformance.md)
- [DESIGN.md](./DESIGN.md)
