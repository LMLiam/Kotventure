# Continuous integration

This document explains the GitHub Actions configuration for Kotventure.

For local development commands, see [CONTRIBUTING.md](../.github/CONTRIBUTING.md). For release automation, see
[RELEASING.md](./RELEASING.md). For Minecraft vanilla conformance, see
[vanilla-conformance.md](./vanilla-conformance.md).

## Workflow map

| Workflow | File | Triggers | Purpose |
|----------|------|----------|---------|
| **CI** | `ci.yml` | PR, push, `merge_group`, weekly schedule, `workflow_dispatch` | Gate, path filter, parallel lint (Kotlin + Actions), sharded build (`core | text | runtime` → Aggregate), Vanilla. Always reports the required status check |
| **JUnit publication** | `junit-publish.yml` | `workflow_run` for CI (`in_progress`, `completed`) | Uses default-branch code to publish the Build and Vanilla JUnit checks from bounded XML artefacts |
| **CodeQL** | `codeql.yml` | PR, push, `merge_group`, weekly schedule | CodeQL analysis (`actions` + `java-kotlin` matrix), parallel with CI |
| **CodeQL publication** | `codeql-publish.yml` | `workflow_run` for CodeQL (`in_progress`, `completed`) | Uses default-branch code to validate and upload PR and merge-group SARIF artefacts |
| **PR metrics publication** | `pr-metrics-publish.yml` | `workflow_run` after CI | Validates the CI result and publishes one metrics comment with default-branch code |
| **Qodana** | `qodana.yml` | `pull_request_target` (in parallel with PR CI) | Runs Qodana with read-only permissions, restores the cached Qodana IDE distribution, and creates one bounded SARIF artefact |
| **Qodana publication** | `qodana-publish.yml` | `workflow_run` after Qodana | Validates the current PR and publishes SARIF with default-branch code |
| **Qodana trusted** | `qodana-trusted.yml` | push `master`, weekly schedule, `workflow_dispatch` | Runs and publishes Qodana for trusted refs, parallel with CI, and warms the shared IDE-distribution cache |
| **PR** | `pr.yml` | `pull_request_target` | Title + commit validation, area labels |
| **Release provenance** | `release-provenance.yml` | `pull_request_target` | Verifies Release Please identity and release files from the default branch |
| **Release** | `release.yml` | push `master` | Opens or updates release PRs. Creates tags and releases after merge |
| **OpenSSF Scorecard** | `scorecard.yml` | weekly schedule, `branch_protection_rule`, `workflow_dispatch` | Supply-chain scorecard + SARIF |

## CI pipeline tiers

```
CI
│
├─ Triage ─────────────────────────────────────────────────────────
│   └─ Triage            (gate + path filter → code, vanilla, docs_only — single runner)
│
├─ Tier 1: Core (parallel, fast feedback — all gated on Triage) ──
│   ├─ Lint (Kotlin)     (spotlessCheck + ktlintCheck)
│   ├─ Lint (Actions)    (declaration check + CI tooling tests)
│   ├─ Build             (sharded: core | text (`minimessage` + `serializer`) | runtime (`coroutines` + `paper` + `test` + `test-snapshot` + `bom`) — each `koverBinaryReport` + test results)
│   ├─ Vanilla           (MC-backed selector tests, path-filtered)
│   └─ Dokka             (dokkaGenerate, parallel with Build)
│
├─ CodeQL (independent workflow `codeql.yml`, parallel with CI) ──
│   └─ CodeQL            (actions + java-kotlin matrix; own Gate + Detect changes jobs, not gated on Triage)
│
├─ Tier 2: Aggregate (after Build shards) ─────────────────────────
│   ├─ Aggregate         (consume shard Kover hand-off → koverXmlReport/koverHtmlReport/koverVerify, metrics, baseline cache — no test re-run)
│   │   └─ PR feedback   (read-only metrics computation and result artefact)
│   └─ (Vanilla/Dokka already completed in Tier 1)
│
├─ Policy (independent of tiers) ──────────────────────────────────
│   ├─ Dependencies      (dependency-review-action, PRs only)
│   ├─ Commits           (push-to-master subject validation)
│   ├─ Release provenance (default-branch metadata check)
│   └─ Release attestation (trusted release-please PRs)
│
└─ Status (required merge-gate check) ─────────────────────────────
    └─ Aggregates Tier 1 + Vanilla + Dependencies when applicable

Qodana security pipeline (parallel with CI)
├─ Qodana            (pull_request_target, read-only PR-head analysis or trusted attestation artefact)
├─ Qodana publication (default-branch validation and SARIF upload, workflow_run after Qodana)
└─ Qodana trusted    (push master, schedule, and manual-dispatch analysis, direct triggers)

PR metrics publication (workflow_run)
└─ Validates the completed CI run, result artefact, and current PR
   └─ Publishes one non-gating metrics comment with default-branch code

Trusted report publication (workflow_run)
├─ JUnit publication (in-progress observer + completed reconciliation)
│  ├─ Test Report (Build shards aggregated)
│  └─ Vanilla Conformance Report
└─ CodeQL publication (completed SARIF upload)
   ├─ CodeQL publication (actions)
   └─ CodeQL publication (java-kotlin)
```

All heavy CI jobs (`Lint`, `Build` shards, `Vanilla`, `Dokka`) start in parallel after `Triage` (gated only on `triage.outputs.code`/`vanilla`), not serially. Vanilla was already parallel to the build before this change. CodeQL runs from its own `codeql.yml` workflow with its own gate and path filter, parallel to CI rather than gated on `Triage`. The Qodana security pipeline triggers on `pull_request_target` and runs in parallel with CI
from the default branch. A documentation-only pull request receives a trusted QDJVM attestation. The attestation does
not check out or analyse code. The scan may analyse code before CI finishes; results publish when the scan completes,
and the merge stays gated on the `Status` check. `Aggregate` consumes the shard coverage hand-off without re-running
tests. The `Status` job always starts. It evaluates every owned job result before it reports one required check.

### Status policy

`Status` is the required merge check for the CI jobs that it owns. It uses the event and path flags from `Triage` to
select one policy row. A required job must report `success`. An optional job may report `success` or `skipped`. A
`failure`, `cancelled`, missing, or unknown result fails `Status`. The evaluator reports every invalid result in one
message.

| Event and path | Required jobs | Jobs that may be skipped |
|----------------|---------------|--------------------------|
| Trusted Release Please pull request | `Triage` | Kotlin lint, Actions lint, Build, Aggregate, Dokka, Vanilla, Dependencies |
| Documentation-only pull request | `Triage` | Kotlin lint, Actions lint, Build, Aggregate, Dokka, Vanilla, Dependencies |
| Code pull request without Vanilla paths | `Triage`, both lint jobs, Build, Aggregate, Dokka, Dependencies | Vanilla |
| Code pull request with Vanilla paths | `Triage`, both lint jobs, Build, Aggregate, Dokka, Vanilla, Dependencies | None |
| Push without code paths | `Triage` | All other owned jobs |
| Push with code and without Vanilla paths | `Triage`, both lint jobs, Build, Aggregate, Dokka | Vanilla, Dependencies |
| Push with code and Vanilla paths | `Triage`, both lint jobs, Build, Aggregate, Dokka, Vanilla | Dependencies |
| Merge group, schedule, or manual run | `Triage`, both lint jobs, Build, Aggregate, Dokka, Vanilla | Dependencies |

The `Dependencies` job runs only for pull requests. GitHub skips it on other events because dependency review requires
a pull-request diff. `Status` permits that job to be skipped outside pull requests.

Each Build shard executes its tests under Kover and uploads a `kover-handoff-<shard>` artefact with the binary `.ic`
reports and the compiled classes. `Aggregate` restores that data and generates the XML/HTML reports and the `koverVerify`
gate from it. It does not re-run tests: the Aggregate Gradle invocation contains no test tasks.

### Trusted report publication

Pull-request-controlled `CI` and `CodeQL` jobs do not create checks, annotations, comments, or code-scanning results.
They keep their normal source jobs, build and analyse the pull-request revision, and upload only bounded hand-off
artefacts. The trusted `workflow_run` publishers check out the default branch and validate the source run, repository,
run attempt, current head, artefact binding, and report contents before they write.

`junit-publish.yml` listens for both `in_progress` and `completed` CI events. It registers `Test Report` and
`Vanilla Conformance Report` checks against the validated head. Build shards remain parallel. The publisher observes
only the three Build jobs and the Vanilla job, so it can publish before Aggregate, Dokka, PR feedback, or `Status`
finish. Build results aggregate into one `Test Report`. A check is `queued` while the source job waits, `in_progress`
while trusted code validates the artefact, and terminal when publication finishes. Source failures map to `failure`,
cancellation to `cancelled`, timeouts to `timed_out`, and proven non-applicability to `skipped`.

JUnit artefacts contain only test XML. The publisher accepts at most 200 files, 16 MiB per archive, 4 MiB per XML
file, 12 MiB total uncompressed XML, 100,000 test cases, and 50 annotations per Checks API update. It rejects
document type declarations, malformed XML, unsafe paths, duplicate entries, symlinks, expired artefacts, and stale
pull-request heads. It resolves source file and line annotations from Gradle failure stack frames when the XML does
not provide them. The checks are advisory. `Status` remains the required CI merge check.

`codeql-publish.yml` registers `CodeQL publication (actions)` and `CodeQL publication (java-kotlin)` on source-run
start. It validates one exact SARIF artefact per category on completion, uploads from a non-Git temporary directory,
and completes each visible check. PR and merge-group source jobs have no `security-events: write`; trusted push,
schedule, and manual CodeQL jobs retain direct publication. Documentation-only and trusted release pull requests
complete the publication checks as `skipped` because CodeQL is not applicable. The publication checks report
transport status only.

The workflow listens for `merge_group` events. Merge groups, schedules, and manual dispatches do not use the path
filter. They always start the full pipeline: Build (sharded → Aggregate with `koverVerify` at 85%), Vanilla, Dokka, and CodeQL (own workflow). The `Qodana trusted` workflow analyses a
push, schedule, or manual-dispatch ref with its own direct triggers, in parallel with CI. It does not run for a
merge-group ref because a merge group contains pull-request code.

## When workflows run

### Path classification

The `Triage` job classifies every current and previous pull-request file name. It
uses the documentation-only allowlist below. A pull request qualifies for the
documentation path only when it has at least one file and every file name
matches one of these patterns:

- `README.md`, `LICENSE.md`, or `AGENTS.md` at the repository root.
- Any file under `docs/`.
- `.github/CONTRIBUTING.md`, `.github/SUPPORT.md`, or
  `.github/pull_request_template.md`.
- Any file under `.github/PULL_REQUEST_TEMPLATE/` or
  `.github/ISSUE_TEMPLATE/`.
- `modules/<module>/README.md`.
- An `svg`, `png`, `jpg`, `jpeg`, `gif`, or `webp` file under `assets/`,
  including subdirectories.

The classifier checks both names for a rename. It sends an empty or incomplete
file list, an API error, an unknown path, and a rename with an unknown old or
new path to the full CI path. This rule is fail closed.

All other paths use the full CI path. This includes source files, module build
files, Gradle files, Qodana configuration, workflows, actions, scripts,
ownership files, dependency files, release files, and unknown paths.

The full CI path includes these code paths:

- `modules/**`, `gradle/**`, `buildSrc/**`
- `build.gradle`, `settings.gradle`, `gradle.properties`, `gradlew`, `gradlew.bat`
- `.editorconfig`, `.gitattributes`, `.gitignore`
- `qodana.yaml`, `jitpack.yml`, `release-please-config.json`
- `.github/workflows/**`, `.github/actions/**`, `.github/scripts/**`, `.github/dependabot.yml`, `.github/package.json`, `.github/package-lock.json`, `.github/tsconfig*.json`

### Push vs PR

| Event | CI workflow | Heavy jobs | Qodana | CodeQL |
|-------|:-----------:|:----------:|:------:|:------:|
| PR (code paths) | ✓ | ✓ | ✓ | ✓ |
| PR (approved docs-only paths) | ✓ | — | QDJVM documentation attestation | — |
| PR (trusted Release Please files only) | ✓ | — | QDJVM attestation | — |
| PR (untrusted release candidate) | ✓ | ✓ | ✓ | ✓ |
| Push to `master` (code paths) | ✓ | ✓ | Trusted Qodana | ✓ |
| Push to `master` (docs only) | ✓ | — | Trusted Qodana | — |
| `merge_group` | ✓ | ✓ (path filter skipped) | — | ✓ |
| Weekly schedule | ✓ | ✓ | Trusted Qodana | ✓ |
| `workflow_dispatch` | ✓ | ✓ | Trusted Qodana | ✓ |

### Pull-request activity types

The pull-request workflows listen for a subset of the `pull_request` activity types. `ci.yml`,
`codeql.yml`, `qodana.yml`, and `release-provenance.yml` listen for `opened`, `synchronize`, and
`reopened`. `pr.yml` listens for `opened`, `edited`, `synchronize`, `reopened`, and
`ready_for_review`.

GitHub delivers `synchronize` when the head branch moves, including force-pushes. It delivers `edited`
when pull-request metadata changes: the title, body, or base branch. `pr.yml` listens for `edited` so a
title fix re-validates the required `Title` check without a new push.

### Manual CI (`workflow_dispatch`)

Select Actions → **CI** → **Run workflow**. A manual workflow always starts and does not use the path filter.

| Input | Default | Behaviour |
|-------|---------|-----------|
| `tasks` | empty | The sharded matrix task set (each shard runs `koverBinaryReport` + `jar`), with `dokkaGenerate` in the parallel Dokka job and coverage verification in Aggregate. If only `module` is set: `:<module>:build` plus root verification (`:koverVerify`, BOM/release checks, Kover reports, Dokka). |
| `module` | empty | Optional project name (`core`, `minimessage`, `bom`, …). Ignored when `tasks` is non-empty. |

Module names must match `[A-Za-z0-9_-]+`.

### Heavy CI gate (Release Please)

The `Triage` job skips resource-intensive jobs only for a trusted Release Please
pull request. The `release-provenance.yml` workflow supplies the independent
check from the default branch. It does not check out the pull request.

The trusted decision requires all of these conditions:

- The event is a pull-request event for `LMLiam/Kotventure`.
- The base branch is `master`.
- The head repository is `LMLiam/Kotventure`.
- The head branch is exactly `release-please--branches--master`.
- The author is `release-please-kotventure[bot]` with GitHub type `Bot`.
- The event sender is `release-please-kotventure[bot]` with GitHub type `Bot`.
- Current and previous file names are in the release-file allowlist.
- The trusted provenance job for the current head SHA completed successfully.

The release files are `CHANGELOG.md`, `.release-please-manifest.json`, and
`gradle/libs.versions.toml`.

The `release-provenance.yml` workflow is the canonical authority for the
release-only result. The `ci.yml` gate repeats the current-event identity and
file checks before it accepts that result. Keep these values aligned:

- The workflow identifier is `release-provenance.yml`.
- The trusted job name is `Trusted release provenance`.
- The repository, base branch, head branch, bot login, GitHub types, and event
  sender must match the policy above.
- The current and previous file names must match the release-file allowlist.

Do not move this contract into a local script or composite action used by the
`pull_request` workflow. That code can come from the pull request. If the two
checks differ, the gate runs full CI.

Titles, labels, commit messages, branch prefixes, and file lists do not prove
Release Please identity. A release-like branch or a pure release-file change
that does not pass the provenance check forces the normal CI path. This also
forces full path-filtered CI for a human or fork lookalike.

The gate no longer skips CI for a push to `master` based on a commit message.
Every push uses the normal CI path.

When a later release-worthy change creates a Release Please pull request, verify
that the pull request is authored by `release-please-kotventure[bot]` before
relying on the release-only CI path.

When you add Release Please `extra-files`, update the allowlist in both
`ci.yml` and `release-provenance.yml`.

For a trusted pure Release Please PR, the `Qodana` workflow creates a zero-result
SARIF record with the `QDJVM` tool name. It does not run Qodana. The trusted
publication workflow validates the current Release Please provenance before it
uploads the record. An untrusted release candidate uses the normal Qodana scan.

For a normal documentation-only pull request, the `Qodana` workflow creates a
zero-result SARIF record with the `QDJVM` tool name. It does not check out or
analyse pull-request code. The trusted publication workflow validates the
current documentation paths before it uploads the record. The result is tied
to the pull-request head commit.

For a code pull request, the `Qodana` workflow checks out the default-branch
workflow code and the pull-request head into separate paths. It supplies the
default-branch `qodana.yaml` file to Qodana. The scan job has only read
permissions. It does not receive `QODANA_TOKEN`. It disables Qodana annotations,
pull-request comments, fix pushes, and result upload. It stores one SARIF file
as a bounded artefact. It restores the Gradle caches, the Qodana inspection cache, and the cached
Qodana IDE distribution read-only. GitHub gives `pull_request_target` runs
read-only access to default-branch caches, so this workflow never writes
cache entries; the trusted workflow keeps them warm.

The `Qodana publication` workflow checks out the default branch. It resolves
the current pull request by head SHA and validates it, the changed paths, the
Qodana run attempt, head SHA, base SHA, artefact name, artefact archive, and
SARIF structure. It then normalises the SARIF with default-branch code. The upload uses a verified non-Git artefact
directory, the validated pull-request head SHA, the stable
`.github/workflows/ci.yml:qodana` analysis key, and the `Kotventure/qodana`
category.
No pull-request code runs in the publication workflow. A failed Qodana analysis
or a rejected source simply publishes nothing; the job-derived checks on the
pull request surface the failure.

The `Qodana trusted` workflow handles push, schedule, and manual-dispatch
refs with direct triggers, in parallel with CI. A job-level guard restricts it
to the repository default branch, so a manual dispatch on another branch is
skipped. It checks out the trusted source commit and uploads its result
directly. As a trusted trigger it may write default-branch caches, so it also
saves the Qodana IDE distribution that the pull-request workflow restores
read-only. Its `security-events: write` permission does not apply to
pull-request or merge-group analysis. The job-derived checks appear on the
analysed commit; no separate registration or report job creates a manual
check.

The `Master` ruleset requires one applicable QDJVM result for each pull request:
documentation-only pull requests use the documentation attestation, code pull
requests use the real Qodana result, and trusted Release Please pull requests
use the release attestation. The attestation SARIF declares one rule and
reports zero alerts. This lets GitHub treat the tool as configured.

### Full builds

Pull requests, pushes, merge groups, schedules, and manual dispatches run the complete pipeline. The Build matrix shards
the work: `core` (`:core:koverBinaryReport` + `:core:jar`), `text` (`minimessage` + `serializer`), and `runtime`
(`coroutines` + `paper` + `test` + `test-snapshot` + `bom`). Dokka runs in its own parallel job. Aggregate consumes the
shard coverage hand-off and enforces the 85% `koverVerify` gate. Path filters control whether the full CI pipeline
starts. They do not control which modules compile. Thus, each code pull request includes the coverage gate, BOM checks,
and dependent module compilation.

### Merge queue

The CI configuration supports a merge queue. Both `ci.yml` and `pr.yml` listen for `merge_group`. Queue batches do not
use the path filter. They start the full pipeline, which includes Vanilla conformance. The Title and Commits jobs report
successful placeholders to keep their required checks present. The Dependencies job is skipped because dependency review
requires a pull-request diff. `Status` accepts that skipped result for merge groups.

When the account supports the feature, enable the queue in the **Master** ruleset or in branch protection. Configure
`merge_queue` and squash merges. Until then, use the usual pull-request squash merge.

### PR metrics (coverage, patch coverage, sizes, API, tests)

After Build, the **PR feedback** job computes one bounded JSON result. The separate **PR metrics publication** workflow
validates that result and posts **one** bot comment (`<!-- pr-metrics -->`) with code from the default branch. The
publication workflow does not execute pull-request code. It also creates the `PR metrics publication` check on the
validated pull-request head. It completes the check with the publication result.

The comment contains:

- A visible **verdict line** with total coverage, gate margin, patch coverage, aggregate JAR change, test count change,
  and public API change.
- **Patch coverage** from the pull-request diff and the Kover line data. The result contains aggregate counts only.
- Mermaid bar charts that show the changes in coverage and JAR size. The bars use the absolute change for their order.
  Collapsed tables contain the absolute values and `.class` entry counts.
- A **public API change** count for added and removed `public` declarations. A grep heuristic supplies this value until
  an apiDump baseline exists. A collapsed diff block shows the declarations.
- Collapsed **build statistics** with test counts, skipped-test counts, and approximate build time.
- Warnings for JAR growth greater than 10 percent, a coverage decrease of at least 0.5 percentage points, and coverage
  within 0.5 percentage points of the Kover gate. The publisher reads the gate from the default branch.
- Links to the workflow run, `dokka-preview` artefact, and `gradle-test-results` artefact.
- Only the verdict line and "No metric changes" when no metric changed.

The job searches for a baseline in this order:

1. The **Actions cache** key `ci-baseline-<base-sha>`. A successful push to `master` writes this key. It contains the
   Kover report, module JARs, and `ci-metrics.json`.
2. The `coverage-report`, `module-jars`, and `ci-metrics` **artefacts** from a successful CI run for the base commit.
3. A JAR-only Gradle build of the base SHA. If no base report is available, the coverage value stays absolute.

The `.github/actions/pr-metrics-comment` action computes the result. Its `action.yml` file calls plain Node modules in
`lib/`. These modules parse patches, coverage, JARs, and ZIP files. The result contract has separate contract,
validation, serialisation, and deserialisation modules. The publisher validates source identity, run attempt, current
PR head and base, exact artefact identity, file shape, size, and result provenance. It downloads the ZIP archive with
trusted code and checks the central directory before it extracts the result. It rejects missing, duplicate, expired,
stale, oversized, malformed, or unexpected data. Renderers are in `lib/sections/`. Tests in `test/` and
`.github/scripts/pr-metrics-publisher.test.js` use `node:test`. The Lint job starts these tests.

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
| Default workflow permissions | Set the repository default to `contents: read` |
| Release provenance workflow | Uses `contents: read` and `pull-requests: read`. Does not check out pull-request code |
| CI gate | Uses `actions: read`, `contents: read`, and `pull-requests: read` |
| Qodana scan | Triggers on `pull_request_target` from the default branch and runs in parallel with CI. The source-resolution and analysis jobs use only `actions: read`, `contents: read`, and `pull-requests: read`. They have no write permission, no `QODANA_TOKEN`, and no Qodana GitHub side effects. They restore the Gradle and Qodana caches read-only |
| Qodana publication | Uses `actions: read`, `contents: read`, `pull-requests: read`, and `security-events: write`. It runs default-branch code, validates the artefacts, and uploads SARIF to code scanning |
| Qodana trusted | Uses `actions: read`, `contents: read`, and `security-events: write`. Runs only on push, schedule, and manual-dispatch refs for the repository default branch. Has no `checks: write` |
| Release workflow | Uses an installation token from `release-please-kotventure`. Its `GITHUB_TOKEN` has no permissions |
| Build and Vanilla jobs | Use `actions: read` and `contents: read`. They upload bounded JUnit and existing test artefacts. They cannot create Checks API results |
| JUnit publication | Runs default-branch code with `actions: read`, `checks: write`, `contents: read`, and `pull-requests: read`. It validates source jobs and XML before publishing the two non-gating checks |
| CodeQL PR and merge-group jobs | Use `actions: read` and `contents: read`. They save post-processed SARIF and cannot upload to code scanning |
| CodeQL publication | Runs default-branch code with `actions: read`, `checks: write`, `contents: read`, `pull-requests: read`, and `security-events: write`. It validates each SARIF artefact before upload |
| PR feedback job | Uses `actions: read`, `pull-requests: read`, and `contents: read`. Computes a bounded result artefact and cannot write to pull requests. Uses the cache or artefacts before a base JAR-only build. Clears `GITHUB_TOKEN` for Gradle |
| PR metrics publication | Runs default-branch code after CI. Uses `actions: read`, `checks: write`, `contents: read`, and `pull-requests: write`. Validates the source workflow, run attempt, current PR head and base, exact artefact, and result provenance before it creates the check or posts the comment |
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
| **pr-metrics-comment** | `.github/actions/pr-metrics-comment` | CI (PR feedback): computes one bounded metrics result artefact |

Before Spotless and ktlint, Lint starts two additional checks. The declaration script permits one top-level type in
each main-source file. The `ci-status` and `pr-metrics-comment` tests use `node --test`.

PR feedback does not control a merge because it uses `continue-on-error`. A failure does not fail Build or Status. The
repository automation tests use `node --test`.

## Scripts

| Script | Role |
|--------|------|
| `ci-gate.ts` | Gate decision: pure-release, trusted provenance, `release-provenance.yml` polling (`decideGate`) |
| `ci-status.ts` | Evaluates owned CI job results against the event and path policy |
| `validate-conventional-title.sh` | Title/commit subject format |
| `check-one-declaration-per-file.sh` | One top-level class/interface/object per main-source file |
| `normalize-qodana-sarif.sh` | Fix 0-based SARIF regions and remove scanner-owned automation metadata before upload |
| `write-gradle-job-summary.sh` | Job summary: Java/Gradle/Kotlin versions + failed tasks |
| `vanilla-fixture-cache-key.sh` | Compute MC fixture cache key |
| `download-base-metrics.sh` | PR feedback: fetch base coverage/jars/metrics from the base commit's CI run |
| `build-base-jars.sh` | PR feedback: last-resort jar-only Gradle build of the base SHA |
| `collect-ci-metrics.sh` | Aggregate: test/skipped counts + longest shard and Aggregate coverage durations → `ci-metrics.json` |
| `pr-metrics-publisher.ts` | Trusted workflow_run publisher: validates the source run and renders the metrics comment |
| `pr-metrics-publisher-storage.ts` | Bounded result artifact download and validation |
| `junit-publisher.ts` | Trusted in-progress observer and completed JUnit publisher |
| `junit-publisher-storage.ts` | Exact JUnit artefact selection, bounded archive extraction, and XML loading |
| `junit-parser.ts` | Bounded JUnit XML parsing, aggregation, summaries, and annotations |
| `codeql-publisher.ts` | Trusted CodeQL check registration, SARIF hand-off, upload coordination, and completion |
| `codeql-validation.ts` | CodeQL workflow, artefact, and SARIF validation |
| `codeql-publisher-storage.ts` | Bounded CodeQL SARIF artefact download |
| `workflow-trust-policy.ts` | YAML 1.2 structural trust-boundary checks for repository workflows |
| `shared/artifact-archive.ts` | Bounded single-entry and multi-entry ZIP validation |
| `shared/artifact-download.ts` | Bounded GitHub artefact archive download |
| `qodana-source.ts` | Trusted `pull_request_target` source resolution and path classification for Qodana |
| `qodana-publisher.ts` | Trusted Qodana publication source validation |
| `qodana-publisher-archive.ts` | Bounded single-file SARIF archive extraction |
| `qodana-publisher-storage.ts` | Bounded artifact download and SARIF validation |
| `qodana-attestation.ts` | Create the trusted QDJVM attestation |
| `workflow-run-check.ts` | Creates, validates, and completes source-bound workflow checks |

The `.github/package.json` manifest and its committed `package-lock.json` hold the npm
dependencies of the CI tooling. Every job that runs the tooling installs them with
`npm ci --ignore-scripts --no-audit --no-fund` from `.github` and then builds the
TypeScript tooling with `npm run build`; the trusted publishers and the Qodana jobs
install against the default-branch lockfile. Release provenance, the `pr.yml` jobs, and
the Qodana code-scan path remain dependency-free.

### Node tooling

`npm run build` compiles the TypeScript sources in `.github/scripts/` and
`.github/actions/pr-metrics-comment/` in place with `tsc -p tsconfig.json`. The
`tsconfig.json` uses NodeNext module resolution and full strict mode and emits no source
maps. `npm run typecheck` runs `tsc --noEmit` to check types without emitting files.
Node 22 is pinned and the npm store is cached through `actions/setup-node` in every job
that installs the tooling.

Every job that runs the tooling installs with `npm ci` and builds before it references a
compiled script, so a fresh checkout — which contains no generated `.js` — always builds
before any script runs. The generated `.js` files are gitignored and never committed.
Lint (Actions) enforces this: no `.js` may be tracked under `.github/scripts/` or
`.github/actions/pr-metrics-comment/`, and `git check-ignore` must confirm a
representative output file is ignored.

## Action pins and Dependabot

Each third-party action uses a fixed SHA and has a version comment. One `github-actions` entry in
`.github/dependabot.yml` updates these actions. Its directories are `["/", "/.github/actions/*"]`. This pattern includes
new composite actions without a configuration change.

| Ecosystem | Grouping | Open PR limit |
|-----------|----------|---------------|
| Gradle (`/`) | Minor and patch updates grouped (`gradle-minor-patch`). Major updates ungrouped | 10 |
| GitHub Actions (root + composites) | Minor and patch updates grouped. Major updates ungrouped | 10 |
| npm (`/.github`) | Minor and patch updates grouped (`npm-minor-patch`). Major updates ungrouped | 10 |

## Branch protection (`master`)

The **Master** repository ruleset protects the default branch. The rulesets API confirms these settings:

- Block force pushes, branch deletion, and direct pushes. Update the branch only through a pull request.
- Require one approval and a code-owner review. Dismiss stale reviews and resolve conversations. Permit only squash
  merges.
- Require **Status**, **Title**, **Commits**, **Dependencies**, and **Maintainer approval**. Require them to be current
  with the base branch.
- Block Qodana (`QDJVM`) alerts that have medium or higher security severity, or error severity. A pure release PR uses
  the release attestation after the gate verifies that no source files changed.
- Permit maintainers to bypass the rules in an emergency.

[CODEOWNERS](../.github/CODEOWNERS) assigns `@LMLiam` as the default owner. It also assigns this owner to
`.github/workflows/`, `.github/actions/`, `.github/scripts/`, and related CI configuration. Thus, code-owner review
applies to automation changes.

## Release branch protection

Configure an active `Protect Release Please branches` ruleset for
`refs/heads/release-please--branches--*`. It must block branch creation,
updates, deletion, and non-fast-forward updates.

The ruleset must have one bypass actor. The actor is the
`release-please-kotventure` GitHub App Integration. Administrators, repository
roles, teams, users, `github-actions[bot]`, and unrelated Apps must not bypass
this ruleset.

Treat this ruleset as a deployment prerequisite for the release-only CI skip.
Do not merge or enable the workflow changes until GitHub confirms the active
ruleset, its ref pattern, and its sole App Integration bypass. Keep full CI
enabled until this verification is complete.

Install the App only on `LMLiam/Kotventure`. Give it Metadata read, Contents
read/write, Issues read/write, and Pull requests read/write. Store the App ID
in `RELEASE_PLEASE_APP_ID`. Store the private key in
`RELEASE_PLEASE_APP_PRIVATE_KEY`. Do not store a private key in the repository.

## Required vs optional checks

Pull requests show many checks. Only the checks in the **Master** ruleset block a merge.

| Check | Merge gate | Notes |
|-------|:----------:|-------|
| **Status** | **Required** | Evaluates lint, build, Aggregate coverage, Dokka, Vanilla, and Dependencies against the event and path policy |
| **Title** | **Required** | Conventional PR title (from `pr.yml`) |
| **Commits** | **Required** | Conventional commit subjects (from `pr.yml`) |
| **Dependencies** | **Required** | Dependency review for pull requests. The job is skipped on other events. |
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
| Kover coverage hand-off | Shards upload `kover-handoff-<shard>`; Aggregate restores them and runs `:koverXmlReport :koverHtmlReport :koverVerify -Pkover.externalBinariesDir=modules`. No test re-run in Aggregate |
| Qodana caches | The Qodana analyse jobs restore the Gradle caches read-only (`cache-read-only: true`) and restore and save the Qodana inspection cache (`use-caches: true`). The action installs the qodana CLI executable into the runner tool cache (`RUNNER_TOOL_CACHE/qodana/<version>/<arch>`); the IDE distribution (`qodana-jvm-community`) is a separate download that the `cache-dir` override pins inside `${{ runner.temp }}/qodana`, cached under `qodana-ide-<os>-v2026.2.0`. Only trusted triggers (push, schedule, dispatch) may write default-branch caches, so the trusted workflow saves the IDE distribution and pull-request-triggered runs restore it with `actions/cache/restore` |
| npm tooling cache | Tooling jobs restore the npm store with `actions/setup-node` (`cache: npm`, keyed on `.github/package-lock.json`, Node 22 pinned). `npm ci` verifies lockfile integrity hashes, so a cached store cannot install wrong content |

### Artefacts

Each Build shard uploads test results (`gradle-test-results-<shard>`, always, including failed runs), a Kover hand-off
(`kover-handoff-<shard>`, always), module JARs (`module-jars-<shard>`, always when present), and a duration file
(`gradle-duration-<shard>`).

| Artefact | When |
|----------|------|
| Test results / HTML test reports | Always (including failed runs), per shard |
| Kover hand-off (`kover-handoff-<shard>`) | Always, per shard. Binary `.ic` reports + compiled classes |
| Module JARs (`module-jars`) | Re-uploaded from the shards by Aggregate. Used for PR head metrics and as the base download fallback |
| Coverage report (`coverage-report`) | From Aggregate. Generated from the shard hand-off without re-running tests. Retained for 14 days |
| CI metrics (`ci-metrics`) | From Aggregate. Test counts, skipped count, and build duration (longest shard + Aggregate coverage stage) |
| PR metrics result (`pr-metrics-result-<run-id>-<run-attempt>`) | Successful PR feedback computation. Contains only bounded, typed metric data. Retained for one day |
| Dokka preview | Uploaded from the Dokka job on successful PRs only. Contains rendered KDoc HTML. Retained for 14 days. Treat as untrusted HTML |

## Re-running CI

- Select **Re-run failed jobs** or **Re-run all jobs** on an Actions run.
- CI and Scorecard support `workflow_dispatch`. CI accepts the optional `tasks` and `module` inputs.

## Related docs

- [RELEASING.md](./RELEASING.md)
- [vanilla-conformance.md](./vanilla-conformance.md)
- [DESIGN.md](./DESIGN.md)
