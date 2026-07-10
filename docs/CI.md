# Continuous integration

How GitHub Actions is organized for Kotventure, when heavy jobs run, and where shared pieces live.

For local development commands, see [CONTRIBUTING.md](../.github/CONTRIBUTING.md). For release automation, see
[RELEASING.md](./RELEASING.md). For Minecraft vanilla conformance, see
[vanilla-conformance.md](./vanilla-conformance.md).

## Workflow map

| Workflow | Triggers | Purpose |
|----------|----------|---------|
| **Build** | PR → `master`, push → `master`, `workflow_dispatch` | Parallel format/lint + Gradle verify when code paths change; required check always reports |
| **Qodana** | PR → `master` (path-filtered), weekly schedule, `workflow_dispatch` | Static analysis + SARIF to code scanning |
| **CodeQL** | PR/push → `master` (path-filtered), weekly schedule, `workflow_dispatch` | Code scanning for Actions and Java/Kotlin |
| **Vanilla Conformance** | Path-filtered PRs, weekly schedule, `workflow_dispatch` | MC-backed selector tests |
| **Dependency Security Review** | PR → `master` | `dependency-review-action` |
| **Conventional Titles** | `pull_request_target`, push `master` | PR title + commit subjects (`verb(area): …`) |
| **Labeler** | `pull_request_target` | Path → `area:*` labels |
| **Release Please** | push `master` | Opens/updates release PRs; tags/releases after merge |
| **OpenSSF Scorecard** | weekly schedule, `branch_protection_rule`, `workflow_dispatch` | Supply-chain scorecard + SARIF |
| **Heavy CI gate** | `workflow_call` only | Skip pure release-please PRs for Build, Qodana, and CodeQL |

## When workflows run

### Code paths

Shared definition: [`.github/code-paths-filter.yml`](../.github/code-paths-filter.yml) (`code:` group).

- `modules/**`, `gradle/**`, `buildSrc/**`
- `build.gradle`, `settings.gradle`, `gradle.properties`, `gradlew`, `gradlew.bat`
- `.editorconfig`, `.gitattributes`, `.gitignore`
- `qodana.yaml`, `jitpack.yml`, `release-please-config.json`
- `.github/workflows/**`, `.github/actions/**`, `.github/scripts/**`, `.github/dependabot.yml`,
  `.github/code-paths-filter.yml`

**Build** starts on every PR/push to `master`, then:

1. **Heavy CI gate** — skip pure release-please allow-list PRs.
2. **Path filter** — skip Gradle work when no `code` paths changed.
3. **Parallel jobs** (when code paths change):
   - **Format and lint** — one-declaration-per-file check, then `spotlessCheck` + `ktlintCheck`.
   - **Gradle build** — `build`, Dokka, Kover reports.
4. **`Build, Test, and Lint`** — required check; green when both jobs are skipped, fails if either fails.

Both jobs check out full git history so Spotless `ratchetFrom 'origin/master'` resolves.

**Qodana** uses the same path list on `pull_request` so docs-only PRs do not start it. Keep `qodana.yml`
`pull_request.paths` identical to the `code` list in `code-paths-filter.yml`.

Gradle does not run for docs-only / template-only changes. Markdown under `modules/**` is a code path.

### Push vs PR

| Event | Build workflow | Lint + Gradle jobs | Qodana | CodeQL |
|-------|:--------------:|:------------------:|:------:|:------:|
| PR (code paths) | ✓ | ✓ | ✓ | ✓ |
| PR (docs/process only) | ✓ | — | — | — |
| Push to `master` (code paths) | ✓ | ✓ | — | ✓ |
| Push to `master` (docs only) | ✓ | — | — | — |
| Weekly schedule | — | — | ✓ | ✓ |
| `workflow_dispatch` | ✓ | ✓ | ✓ | ✓ |

### Manual Build (`workflow_dispatch`)

Actions → **Build** → **Run workflow**. Always runs Format and lint + Gradle build (path filter skipped).

| Input | Default | Behaviour |
|-------|---------|-----------|
| `tasks` | empty | Default full set: `build dokkaGenerate koverXmlReport koverHtmlReport`. If only `module` is set: `:<module>:build` (includes tests when the project has them). |
| `module` | empty | Optional project name (`core`, `minimessage`, `bom`, …). Ignored when `tasks` is non-empty (tasks run as typed). |

Module names must match `[A-Za-z0-9_-]+`. Manual runs use a separate concurrency group from push/PR so they do not cancel each other.

### Heavy CI gate (release-please)

Workflow: [`.github/workflows/heavy-ci-gate.yml`](../.github/workflows/heavy-ci-gate.yml).  
**Used by:** Build, Qodana, CodeQL. Dependency Review is not gated.

Skips Build’s lint/Gradle jobs, Qodana, and CodeQL when all of:

1. Event is a `pull_request`
2. Head branch starts with `release-please--`
3. Changed files are only:
   - `CHANGELOG.md`
   - `.release-please-manifest.json`
   - `gradle/libs.versions.toml`

On Build, the required check still reports success when the gate skips the lint and Gradle jobs.

If the PR has any other path change, Build/Qodana/CodeQL run when code paths match.

When adding release-please `extra-files`, update the gate allow-list in the same PR.

### CodeQL

Workflow: [`.github/workflows/codeql.yml`](../.github/workflows/codeql.yml).

- **Languages:** `actions` (build mode `none`) and `java-kotlin` (manual `./gradlew classes testClasses -x test`
  with `--rerun-tasks --no-build-cache` so the extractor sees a real compile).
- **Path filters** on PR/push match the Build/Qodana code path list; weekly schedule and `workflow_dispatch` always run.
- Results upload to GitHub code scanning (same surface as Qodana/Scorecard SARIF).

## Local composite actions

| Action | Path | Used by |
|--------|------|---------|
| **setup-jdk-gradle** | [`.github/actions/setup-jdk-gradle`](../.github/actions/setup-jdk-gradle/action.yml) | Build, Vanilla Conformance, CodeQL (`java-kotlin`) |
| **publish-junit-report** | [`.github/actions/publish-junit-report`](../.github/actions/publish-junit-report/action.yml) | Build, Vanilla Conformance |

Checkout stays per-workflow (Qodana custom `ref` / history; Build uses full history on both parallel jobs).

## Scripts

| Script | Role |
|--------|------|
| [`.github/scripts/validate-conventional-title.sh`](../.github/scripts/validate-conventional-title.sh) | Title/commit subject format |
| [`.github/scripts/check-one-declaration-per-file.sh`](../.github/scripts/check-one-declaration-per-file.sh) | One top-level class/interface/object per main-source file (AGENTS.md §5) |
| [`.github/scripts/normalize-qodana-sarif.sh`](../.github/scripts/normalize-qodana-sarif.sh) | Fix 0-based SARIF regions for GitHub code scanning |
| [`.github/scripts/write-gradle-job-summary.sh`](../.github/scripts/write-gradle-job-summary.sh) | Job summary: Java/Gradle/Kotlin versions + failed tasks |

Format and lint and Gradle build always write a job summary (toolchain + event; failed Gradle task names when the log contains `> Task … FAILED`).

## Action pins and Dependabot

Third-party actions are SHA-pinned with a version comment. Dependabot updates (`github-actions` in
[`.github/dependabot.yml`](../.github/dependabot.yml)):

- `/` — workflows
- `/.github/actions/setup-jdk-gradle`
- `/.github/actions/publish-junit-report`

New composites that pin third-party actions need a matching Dependabot directory.

| Ecosystem | Grouping | Open PR limit |
|-----------|----------|---------------|
| Gradle (`/`) | Minor + patch grouped (`gradle-minor-patch`); **majors ungrouped** (one PR each for review — Kotlin, Adventure, etc.) | 10 |
| GitHub Actions (root + composites) | Minor + patch grouped; majors ungrouped | 10 (root), 5 (composites) |

## Branch protection (`master`)

Repository ruleset **Master** (default branch):

- Block force-push, branch deletion, and direct pushes (updates only via PR).
- Pull requests: one approving review, code-owner review, dismiss stale reviews, resolve conversations, squash only.
- Required status checks (must pass before merge) — see [Required vs optional checks](#required-vs-optional-checks).
- Code scanning gate for Qodana (`QDJVM`) alerts at medium-or-higher security / errors severity.
- Maintainer bypass remains configured for emergency overrides.

[CODEOWNERS](../.github/CODEOWNERS) assigns `@LMLiam` as default owner and explicitly for `.github/workflows/`,
`.github/actions/`, `.github/scripts/`, and related CI config so code-owner review covers automation changes.

## Required vs optional checks

PRs show many checks; only a subset is merge-blocking via the **Master** ruleset.

| Check | Merge gate | Notes |
|-------|:----------:|-------|
| **Build, Test, and Lint** | **Required** | Always reports; green when lint/Gradle are skipped (docs-only / pure release-please allow-list) |
| **Validate Pull Request Title** | **Required** | Conventional PR title |
| **Validate Commit Subjects** | **Required** | Conventional commit subjects on the PR |
| **Review Dependency Changes** | **Required** | Dependency review; not heavy-CI-gated |
| Format and lint / Gradle build | No | Nested under the Build aggregator |
| Qodana / QDJVM | No* | Workflow is informational as a status check; **QDJVM** code-scanning alerts are ruleset-gated (medium-or-higher security / errors) |
| CodeQL (`Analyze (…)` ) | No | SARIF to code scanning |
| Vanilla conformance | No | Path-filtered |
| Labeler / Apply area labels | No | Labelling only |
| Scorecard | No | Schedule / dispatch |

\*Qodana is **not** a required status check and is not marked `continue-on-error`. Failures remain visible on the PR without blocking merge by themselves; serious findings still surface through the QDJVM code-scanning ruleset rule.

## Dependency review

Workflow: [`.github/workflows/dependency-review.yml`](../.github/workflows/dependency-review.yml). Runs on every PR to
`master` (not heavy-CI-gated). Fails on **moderate** or higher severity advisories for scopes
`runtime`, `development`, and `unknown`.

## Performance

| Mechanism | Where |
|-----------|--------|
| Configuration cache + local build cache | `gradle.properties` (`org.gradle.configuration-cache`, `org.gradle.caching`); CI restores Gradle caches via `setup-gradle` in `.github/actions/setup-jdk-gradle` |
| Dependency / wrapper caches | `setup-gradle` defaults in `.github/actions/setup-jdk-gradle` |
| Minecraft conformance fixtures | `actions/cache` on `modules/core/build/vanilla-conformance`; cache key is derived at runtime from `targetMinecraftVersion` and `serverBundleSha1` in `gradle/vanilla-conformance.gradle`. Restored bundles are SHA-1 re-checked before Gradle runs. |

### Artifacts (Gradle build job)

| Artifact | When |
|----------|------|
| Test results / HTML test reports | Always (including failed runs) |
| Kover coverage report | Always (including failed runs) |
| Module jars under `build/libs` | Only on **job failure**, or on **push to `master`** |

## Re-running CI

- **Re-run failed jobs** / **Re-run all jobs** on an Actions run.
- **Build**, Qodana, CodeQL, Vanilla Conformance, and Scorecard support `workflow_dispatch` (Build accepts optional `tasks` / `module` inputs).
- Open the failed **Format and lint** or **Gradle build** job for the job summary (toolchain + failed tasks).

## Related docs

- [RELEASING.md](./RELEASING.md)
- [vanilla-conformance.md](./vanilla-conformance.md)
- [DESIGN.md](./DESIGN.md)
