# Continuous integration

How GitHub Actions is organized for Kotventure.

For local development commands, see [CONTRIBUTING.md](../.github/CONTRIBUTING.md). For release automation, see
[RELEASING.md](./RELEASING.md). For Minecraft vanilla conformance, see
[vanilla-conformance.md](./vanilla-conformance.md).

## Workflow map

| Workflow | File | Triggers | Purpose |
|----------|------|----------|---------|
| **CI** | `ci.yml` | PR, push, `merge_group`, weekly schedule, `workflow_dispatch` | Gate → path filter → parallel lint + build + analysis; required status check always reports |
| **PR** | `pr.yml` | `pull_request_target` | Title + commit validation, area labels |
| **Release** | `release.yml` | push `master` | Opens/updates release PRs; tags/releases after merge |
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
│       └─ PR feedback   (one metrics comment: coverage Δ + JAR sizes; non-gating)
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

The workflow listens for `merge_group` events; like schedule and dispatch runs, they skip the path
filter and always run the full pipeline (Build, Vanilla, Qodana, CodeQL).

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
| `merge_group` | ✓ | ✓ (path filter skipped) | ✓ | ✓ |
| Weekly schedule | ✓ | ✓ | ✓ | ✓ |
| `workflow_dispatch` | ✓ | ✓ | ✓ | ✓ |

### Manual CI (`workflow_dispatch`)

Actions → **CI** → **Run workflow**. Always runs (path filter skipped).

| Input | Default | Behaviour |
|-------|---------|-----------|
| `tasks` | empty | Default full set: `build dokkaGenerate koverXmlReport koverHtmlReport`. If only `module` is set: `:<module>:build` plus root verification (`koverVerify`, BOM/release checks, Kover reports, Dokka). |
| `module` | empty | Optional project name (`core`, `minimessage`, `bom`, …). Ignored when `tasks` is non-empty. |

Module names must match `[A-Za-z0-9_-]+`.

### Heavy CI gate (release-please)

Integrated into the CI workflow's `gate` job. Handles both PRs and push-to-master merge commits.

Skips heavy jobs when:

- **PR:** head branch starts with `release-please--` and changed files are release-only
- **Push:** commit message matches `chore(master): release` and changed files are release-only

Release-only files: `CHANGELOG.md`, `.release-please-manifest.json`.

`gradle/libs.versions.toml` is **not** skip-eligible (version catalog changes always run heavy CI).

When adding release-please `extra-files`, update the gate allow-list in `ci.yml`.

### Full builds

PR, push, `merge_group`, schedule, and dispatch all run a **full multi-project** default task set
(`build dokkaGenerate koverXmlReport koverHtmlReport`). Path filters only decide *whether* heavy CI
runs (code vs docs), not which modules compile. That keeps the coverage gate, BOM checks, and
dependent-module compiles on every code PR.

### Merge queue

CI is merge-queue ready: `ci.yml` and `pr.yml` both listen for `merge_group`. Queue batches skip the
path filter and run the full pipeline including Vanilla conformance (the queue is the last gate before
`master`); Title / Commits report success placeholders so required checks still exist.
Dependency-review remains PR-only; Status tolerates a non-failure skip for that job on non-PR events.

Enable the queue in the repo **Master** ruleset (**merge_queue**, squash) or under branch protection
when the feature is available for the account. Until then, normal PR squash-merge still works.

### PR metrics (coverage, patch coverage, sizes, API, tests)

After Build, the **PR feedback** job posts **one** bot comment (`<!-- pr-metrics -->`):

- A never-collapsed **verdict line** (✅/⚠️): total coverage + delta + gate headroom, patch
  coverage, aggregate JAR delta, test count delta, public-API delta
- **Patch coverage** — % of changed executable lines covered, from the PR's GitHub diff joined
  with Kover's per-line XML data; uncovered added lines listed as `file.kt:12–15` ranges
- Mermaid **delta-only** bar charts (PR − base) for coverage (pp) and JAR size (%), bars sorted
  by |Δ|; collapsed data tables carry absolute values (JAR table includes `.class` entry counts)
- **Public API delta** — added/removed `public` declarations counted from the diff (grep
  heuristic until the apiDump baseline lands); rendered as a collapsed diff block
- Collapsed **build stats**: test/skipped counts and indicative build wall time
- Warnings: JAR growth >10%, total coverage drop ≥0.5pp, coverage within 0.5pp of the Kover gate
  (threshold parsed from `gradle/coverage.gradle` at runtime)
- Footer links: workflow run, `dokka-preview` artifact, `gradle-test-results` artifact
- When nothing changed, the body collapses to the verdict line plus "No metric changes"

Baseline resolution order (prefer cache, avoid rebuilds):

1. **Actions cache** key `ci-baseline-<base-sha>` (written on successful `master` pushes; holds
   the Kover report, module jars, and `ci-metrics.json`)
2. **Artifacts** from a successful CI run for the base commit (`coverage-report`, `module-jars`,
   `ci-metrics`)
3. **Fallback:** jar-only Gradle build of the base SHA (coverage stays absolute if no base report)

The comment is built by `.github/actions/pr-metrics-comment` — a thin `action.yml` entry over plain
Node modules in `lib/` (patch/coverage/jar/zip parsing, `lib/sections/` renderers, comment upsert),
unit tested with `node:test` in `test/`. The Lint job runs those tests.

### Build Scans

**Off by default.** Enabling `build-scan: true` on `gradle-job` (or `build-scan-publish: true` on
`setup-jdk-gradle`) injects Develocity and publishes a public scan — but it also **wrecks local
build-cache hit rates** (observed: ~55 cache hits / 26s → ~1 cache hit / ~4 minutes on the same task
set). Prefer leaving scans off for PR/push CI.

To capture a scan occasionally (for example a manual diagnostic run), pass
`build-scan: true` into `gradle-job`; that agrees to Gradle’s terms via `setup-gradle` and appends
`--scan`. For a private Develocity server, configure Develocity URL / access key on `setup-gradle`
instead of public scans.

### Trust and permissions

| Surface | Behaviour |
|---------|-----------|
| Default workflow permissions | `contents: read` |
| Build job | `checks: write` + `contents: read` — no PR write; Gradle runs with `GITHUB_TOKEN` cleared |
| PR feedback job | `actions: read` + `pull-requests: write` + `contents: read` — posts metrics comment; prefers cache/artifacts for base jars/coverage, falls back to a base jar-only Gradle build with `GITHUB_TOKEN` cleared |
| Build scans | Off by default (cache-friendly); opt-in via `build-scan: true` |
| Dokka preview artifact | Untrusted HTML from the PR build; open locally with care; 14-day retention; not published as Pages |

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
| **gradle-job** | `.github/actions/gradle-job` | CI (Lint, Build) — JDK/Gradle setup + run tasks + Build Scan + job summary |
| **setup-jdk-gradle** | `.github/actions/setup-jdk-gradle` | gradle-job, Vanilla, CodeQL, PR feedback fallback — JDK + Gradle caches + scan TOS |
| **publish-junit-report** | `.github/actions/publish-junit-report` | CI (Build, Vanilla) — JUnit XML → Checks annotations |
| **pr-metrics-comment** | `.github/actions/pr-metrics-comment` | CI (PR feedback) — single coverage + JAR size comment |

Lint also runs `.github/scripts/check-one-declaration-per-file.sh` (one top-level type per main-source
file) and the `pr-metrics-comment` unit tests (`node --test`) before Spotless/ktlint.

PR feedback is non-gating (`continue-on-error`); failures there do not fail Build or Status.

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

Third-party actions are SHA-pinned with a version comment. Dependabot updates them via a single
`github-actions` entry in `.github/dependabot.yml` with `directories: ["/", "/.github/actions/*"]` —
new composites are covered automatically, no config change needed.

| Ecosystem | Grouping | Open PR limit |
|-----------|----------|---------------|
| Gradle (`/`) | Minor + patch grouped (`gradle-minor-patch`); **majors ungrouped** | 10 |
| GitHub Actions (root + composites) | Minor + patch grouped; majors ungrouped | 10 |

## Branch protection (`master`)

Repository ruleset **Master** (default branch), verified via the rulesets API:

- Block force-push, branch deletion, and direct pushes (updates only via PR).
- Pull requests: one approving review, code-owner review, dismiss stale reviews, resolve conversations, squash only.
- Required status checks (strict, must be up to date with base): **Status**, **Title**, **Commits**, **Dependencies**.
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
| PR feedback | No | Metrics comment only; not part of Status |
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
| PR metrics baselines | `actions/cache` key `ci-baseline-<sha>` on master push; artifact download fallback; jar rebuild last |

### Artifacts (Build job)

| Artifact | When |
|----------|------|
| Test results / HTML test reports | Always (including failed runs) |
| Kover coverage report | Always (including failed runs); 14-day retention |
| Module jars (`module-jars`) | Always when present; used as PR head metrics + base download fallback |
| CI metrics (`ci-metrics`) | On successful builds — test counts + duration for the PR comment |
| Dokka preview | PRs only (on success) — rendered KDoc HTML, 14-day retention; treat as untrusted HTML |
| Full `build/libs` upload | Only on **job failure**, or on **push to `master`** |

## Re-running CI

- **Re-run failed jobs** / **Re-run all jobs** on an Actions run.
- CI and Scorecard support `workflow_dispatch` (CI accepts optional `tasks` / `module` inputs).

## Related docs

- [RELEASING.md](./RELEASING.md)
- [vanilla-conformance.md](./vanilla-conformance.md)
- [DESIGN.md](./DESIGN.md)
