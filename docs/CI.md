# Continuous integration

How GitHub Actions is organized for Kotventure, when heavy jobs run, and where shared pieces live.

For local development commands, see [CONTRIBUTING.md](../.github/CONTRIBUTING.md). For release automation, see
[RELEASING.md](./RELEASING.md). For Minecraft vanilla conformance, see
[vanilla-conformance.md](./vanilla-conformance.md).

## Workflow map

| Workflow | Triggers | Purpose |
|----------|----------|---------|
| **Build** | PR → `master` and push → `master` | Gradle verify when code paths change; always reports the required check |
| **Qodana** | PR → `master` (**path-filtered**), weekly schedule, `workflow_dispatch` | Static analysis + SARIF to code scanning |
| **Vanilla Conformance** | Path-filtered PRs, weekly schedule, `workflow_dispatch` | MC-backed selector tests |
| **Dependency Security Review** | PR → `master` (every PR) | `dependency-review-action` (manifest-focused analysis) |
| **Conventional Titles** | `pull_request_target`, push `master` | PR title + commit subjects (`verb(area): …`) |
| **Labeler** | `pull_request_target` | Path → `area:*` labels |
| **Release Please** | push `master` | Opens/updates release PRs; tags/releases after merge |
| **OpenSSF Scorecard** | weekly schedule, `branch_protection_rule`, `workflow_dispatch` | Supply-chain scorecard + SARIF |
| **Heavy CI gate** | `workflow_call` only | Shared skip logic for pure release-please PRs |

## When workflows run

### Code path set

The shared code-path definition lives in [`.github/code-paths-filter.yml`](../.github/code-paths-filter.yml)
(`code:` group). It includes:

- `modules/**`, `gradle/**`, `buildSrc/**`
- Root Gradle entrypoints: `build.gradle`, `settings.gradle`, `gradle.properties`, `gradlew`, `gradlew.bat`
- Style roots: `.editorconfig`, `.gitattributes`, `.gitignore`
- `qodana.yaml`, `jitpack.yml`, `release-please-config.json`
- CI wiring: `.github/workflows/**`, `.github/actions/**`, `.github/scripts/**`, `.github/dependabot.yml`

**Build** always starts on every PR/push to `master`, then decides whether to run Gradle:

1. **Heavy CI gate** — skip pure release-please allow-list PRs.
2. **Path filter** — skip when no `code` paths changed (docs/templates/etc.).
3. **Required check** job `Build, Test, and Lint` — always reports **success** when Gradle was intentionally
   skipped, and **failure** when Gradle ran and failed. That keeps the required status check green for docs-only and
   pure release-please PRs without running a full build.

**Qodana** still uses `pull_request.paths` matching the same list so docs-only PRs do not start Qodana at all (it is not
the same required check as Build). Keep `qodana.yml` `paths` in lockstep with `.github/code-paths-filter.yml`.

**Typically no Gradle on:** pure `docs/**` changes, root process markdown, issue/PR templates, labeler config,
CODEOWNERS. Markdown under `modules/**` **does** count as a code path. Titles and Labeler still run.

### Push vs PR (Build / Qodana)

| Event | Build workflow | Gradle job | Qodana |
|-------|:--------------:|:----------:|:------:|
| PR (code paths) | ✓ | ✓ | ✓ |
| PR (docs/process only) | ✓ (required check green) | — | — |
| Push to `master` (code paths) | ✓ | ✓ | — |
| Push to `master` (docs only) | ✓ (required check green) | — | — |
| Weekly schedule | — | — | ✓ |
| `workflow_dispatch` | — | — | ✓ |

- **Build** still runs on pushes to `master` so direct commits and post-merge verification stay covered without a merge
  queue. Path detection avoids a full Gradle run for docs-only merges.
- **Qodana** does **not** run on push to `master`: PR coverage + weekly + manual is enough.
- **Scorecard** does **not** run on every master push: weekly + branch-protection + manual.

### Heavy CI gate (release-please)

Reusable workflow: [`.github/workflows/heavy-ci-gate.yml`](../.github/workflows/heavy-ci-gate.yml).

**Used by:** Build, Qodana.

Dependency Security Review is **not** gated: it is cheap and should still run when release-please touches
`gradle/libs.versions.toml` (even for a project-version bump, the full file is on the allow-list).

#### When the gate skips the Gradle / Qodana jobs

All of the following:

1. Event is a `pull_request`, and
2. Head branch starts with `release-please--`, and
3. The PR changes **only** these files (allow-list):
   - `CHANGELOG.md`
   - `.release-please-manifest.json`
   - `gradle/libs.versions.toml`

That set matches what release-please is configured to touch (`release-please-config.json` + the manifest).

On Build, the required check job still reports success when the gate skips Gradle.

#### When heavy work still runs on a release-please branch

If the PR includes **any other path** (manual commits that fix code, workflows, etc.), the gate sets `run=true` and
Gradle / Qodana execute as usual when code paths also match.

**Not gated:** Dependency Security Review, Conventional Titles, Labeler, Vanilla Conformance (path-filtered;
release-please PRs do not match those paths).

#### Keeping the allow-list in sync

If you add `extra-files` (or otherwise expand what release-please edits), update the allow-list in
`heavy-ci-gate.yml` **in the same PR** as the config change.

Author-based “is this a bot?” detection is intentionally **not** used: release-please may attribute commits to a human
token.

## Local composite actions

| Action | Path | Used by |
|--------|------|---------|
| **setup-jdk-gradle** | [`.github/actions/setup-jdk-gradle`](../.github/actions/setup-jdk-gradle/action.yml) | Build, Vanilla Conformance |
| **publish-junit-report** | [`.github/actions/publish-junit-report`](../.github/actions/publish-junit-report/action.yml) | Build, Vanilla Conformance |

- **Checkout stays explicit** in each workflow (Qodana needs a custom `ref` and full history; Build uses `fetch-depth: 0`
  for the Gradle job; Vanilla uses the default depth).
- Prefer these composites when adding another Gradle-backed job so JDK/Gradle and JUnit report pins stay centralized.

## Scripts

| Script | Role |
|--------|------|
| [`.github/scripts/validate-conventional-title.sh`](../.github/scripts/validate-conventional-title.sh) | Title/commit subject format |
| [`.github/scripts/normalize-qodana-sarif.sh`](../.github/scripts/normalize-qodana-sarif.sh) | Fix 0-based SARIF regions for GitHub code scanning |

## Action pins and Dependabot

Third-party actions are **SHA-pinned** with a version comment (e.g. `# v7.0.0`). Dependabot’s `github-actions` ecosystem
opens weekly grouped PRs for minor/patch bumps (see [`.github/dependabot.yml`](../.github/dependabot.yml)).

Dependabot only scans `github-actions` entries for the directories listed in that file. Root `directory: "/"` covers
workflows; **composite pins** are covered by separate entries for:

- `/.github/actions/setup-jdk-gradle`
- `/.github/actions/publish-junit-report`

When adding a new composite that `uses:` third-party actions, add a matching Dependabot directory so pins stay
auto-updatable.

## Re-running CI

- Use **Re-run failed jobs** / **Re-run all jobs** on an existing Actions run for the PR or push.
- Build has no `workflow_dispatch`. Empty commits still start the Build workflow (no top-level path filter on `on:`),
  but the Gradle job stays skipped unless code paths changed; the required check reports success in that case.
- Qodana, Vanilla Conformance, and Scorecard support `workflow_dispatch` for manual runs.

## Related docs

- [RELEASING.md](./RELEASING.md) — release-please token, version policy, branch protection notes
- [vanilla-conformance.md](./vanilla-conformance.md) — local and CI conformance runs
- [DESIGN.md](./DESIGN.md) — product architecture (not CI wiring)
