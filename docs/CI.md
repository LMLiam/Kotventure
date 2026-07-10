# Continuous integration

How GitHub Actions is organized for Kotventure, when heavy jobs run, and where shared pieces live.

For local development commands, see [CONTRIBUTING.md](../.github/CONTRIBUTING.md). For release automation, see
[RELEASING.md](./RELEASING.md). For Minecraft vanilla conformance, see
[vanilla-conformance.md](./vanilla-conformance.md).

## Workflow map

| Workflow | Triggers | Purpose |
|----------|----------|---------|
| **Build** | PR → `master`, push → `master` | Parallel format/lint + Gradle verify when code paths change; required check always reports |
| **Qodana** | PR → `master` (path-filtered), weekly schedule, `workflow_dispatch` | Static analysis + SARIF to code scanning |
| **Vanilla Conformance** | Path-filtered PRs, weekly schedule, `workflow_dispatch` | MC-backed selector tests |
| **Dependency Security Review** | PR → `master` | `dependency-review-action` |
| **Conventional Titles** | `pull_request_target`, push `master` | PR title + commit subjects (`verb(area): …`) |
| **Labeler** | `pull_request_target` | Path → `area:*` labels |
| **Release Please** | push `master` | Opens/updates release PRs; tags/releases after merge |
| **OpenSSF Scorecard** | weekly schedule, `branch_protection_rule`, `workflow_dispatch` | Supply-chain scorecard + SARIF |
| **Heavy CI gate** | `workflow_call` only | Skip pure release-please PRs for Build and Qodana |

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
   - **Format and lint** — `spotlessCheck` + `ktlintCheck`.
   - **Gradle build** — `build`, Dokka, Kover reports.
4. **`Build, Test, and Lint`** — required check; green when both jobs are skipped, fails if either fails.

Both jobs check out full git history so Spotless `ratchetFrom 'origin/master'` can resolve during Gradle configuration (the Gradle job still configures Spotless even though it does not run format checks).

**Qodana** uses the same path list on `pull_request` so docs-only PRs do not start it. Keep `qodana.yml`
`pull_request.paths` identical to the `code` list in `code-paths-filter.yml`.

Gradle does not run for docs-only / template-only changes. Markdown under `modules/**` is a code path.

### Push vs PR

| Event | Build workflow | Lint + Gradle jobs | Qodana |
|-------|:--------------:|:------------------:|:------:|
| PR (code paths) | ✓ | ✓ | ✓ |
| PR (docs/process only) | ✓ | — | — |
| Push to `master` (code paths) | ✓ | ✓ | — |
| Push to `master` (docs only) | ✓ | — | — |
| Weekly schedule | — | — | ✓ |
| `workflow_dispatch` | — | — | ✓ |

### Heavy CI gate (release-please)

Workflow: [`.github/workflows/heavy-ci-gate.yml`](../.github/workflows/heavy-ci-gate.yml).  
**Used by:** Build, Qodana. Dependency Review is not gated.

Skips Build’s lint/Gradle jobs and Qodana when all of:

1. Event is a `pull_request`
2. Head branch starts with `release-please--`
3. Changed files are only:
   - `CHANGELOG.md`
   - `.release-please-manifest.json`
   - `gradle/libs.versions.toml`

On Build, the required check still reports success when the gate skips the lint and Gradle jobs.

If the PR has any other path change, Build/Qodana run when code paths match.

When adding release-please `extra-files`, update the gate allow-list in the same PR.

## Local composite actions

| Action | Path | Used by |
|--------|------|---------|
| **setup-jdk-gradle** | [`.github/actions/setup-jdk-gradle`](../.github/actions/setup-jdk-gradle/action.yml) | Build, Vanilla Conformance |
| **publish-junit-report** | [`.github/actions/publish-junit-report`](../.github/actions/publish-junit-report/action.yml) | Build, Vanilla Conformance |

Checkout stays per-workflow (Qodana custom `ref` / history; Build uses full history on both parallel jobs).

## Scripts

| Script | Role |
|--------|------|
| [`.github/scripts/validate-conventional-title.sh`](../.github/scripts/validate-conventional-title.sh) | Title/commit subject format |
| [`.github/scripts/normalize-qodana-sarif.sh`](../.github/scripts/normalize-qodana-sarif.sh) | Fix 0-based SARIF regions for GitHub code scanning |

## Action pins and Dependabot

Third-party actions are SHA-pinned with a version comment. Dependabot updates (`github-actions` in
[`.github/dependabot.yml`](../.github/dependabot.yml)):

- `/` — workflows
- `/.github/actions/setup-jdk-gradle`
- `/.github/actions/publish-junit-report`

New composites that pin third-party actions need a matching Dependabot directory.

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
- Qodana, Vanilla Conformance, and Scorecard also support `workflow_dispatch`.

## Related docs

- [RELEASING.md](./RELEASING.md)
- [vanilla-conformance.md](./vanilla-conformance.md)
- [DESIGN.md](./DESIGN.md)
