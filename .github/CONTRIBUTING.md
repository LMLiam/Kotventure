# Contributing to Kotventure

Thank you for your interest in Kotventure. This project is in **pre-alpha**, and its public API can change in each release.
Small, focused contributions are the most useful.

## Be respectful

Use constructive and considerate language. Do not harass other persons or use hostile behaviour.
Maintainers can remove a person who does not obey these rules from the project spaces.

## Ways to contribute

- **Pick up an issue.** Start with
  [`good first issue`](https://github.com/LMLiam/Kotventure/labels/good%20first%20issue) or
  [`help wanted`](https://github.com/LMLiam/Kotventure/labels/help%20wanted).
- **Discuss a design** before you start a large change. Open a [Discussion](https://github.com/LMLiam/Kotventure/discussions) or
  comment on the applicable issue. The project plan is in [`docs/DESIGN.md`](../docs/DESIGN.md) and
  [`docs/ROADMAP.md`](../docs/ROADMAP.md).
- **Report a bug** with the bug issue template.

## Development setup

- Install **JDK 25**. The build uses a Kotlin JVM toolchain of 25. Gradle gets it through the Foojay resolver.
- Use the **Gradle wrapper**. You do not need a local Gradle installation.

```bash
./gradlew build           # compile + test + lint + verify coverage
./gradlew test            # run the Kotest suites
./gradlew spotlessApply   # auto-format
./gradlew ktlintCheck     # lint check
./gradlew koverHtmlReport # generate the aggregated HTML coverage report
```

After `koverHtmlReport`, open `build/reports/kover/html/index.html` in a browser.
For macOS, use `open build/reports/kover/html/index.html`. Review line coverage for each module, package, and class.

[`gradle/libs.versions.toml`](../gradle/libs.versions.toml) contains dependency and plugin coordinates. Add shared
versions there first. Then, use catalog aliases or Gradle helper scripts that refer to the catalog.

For CI architecture, workflows, the release-please heavy-CI gate, and local composites, refer to [`docs/CI.md`](../docs/CI.md).

### Coverage threshold policy

[Kover](https://github.com/Kotlin/kotlinx-kover) measures code coverage. `check` includes `koverVerify`.
`./gradlew build` fails if the total line coverage is less than the baseline.

- **Current baseline:** [`gradle/coverage.gradle`](../gradle/coverage.gradle) sets line coverage to 85%.
- **Reason:** each behavioural change includes tests. The gate detects a large change that does not have sufficient tests.
- **Increase:** increase the threshold by 5% or 10% when coverage increases. Explain the change in the PR description.

## Project conventions

- **One issue → one small vertical PR.** Each change must include its tests and must be ready for release. Split a large PR.
- **Branch names:** use `type/issue-<n>/short-desc`, for example `feat/issue-12/translatable-component`.
- **Tests:** add tests for behavioural changes. Use [Kotest](https://kotest.io/) and the component matchers in the `test` module.
- **Format:** ktlint and Spotless enforce the format. Run `./gradlew spotlessApply` before you push.
- **PR templates:** select the template for your change (feature / bugfix / docs / chore). Link the issue with
  `Closes #<n>`.

## Commit & PR title format (enforced)

All **pull request titles** and **commit subjects** must use this format:

```text
verb(area): something
```

- Use lower-case letters for `verb` and `area`. Include the **required** `(area)`, `:`, a space, and a summary.
- Pattern: `^[a-z]+\([a-z0-9][a-z0-9-]*\): [^[:space:]].*$`
- The **PR** workflow validates PR titles and commit subjects. The **CI** workflow validates commits pushed to `master`.
  Both workflows run `.github/scripts/validate-conventional-title.sh`.

**Recommended `verb`s:** `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`, `build`, `perf`.

**Recommended `area`s** (use the module or label area): `core`, `minimessage`, `i18n`, `test`, `ansi`, `coroutines`,
`ksp`, `paper`, `velocity`, `fabric`, `gradle-plugin`, `bom`, `e2e`, `docs`, `ci`, `build`, `deps`, `repo`, `meta`.

Examples: `feat(minimessage): add typed placeholder DSL` · `fix(core): correct decoration reset` ·
`docs(repo): document title convention`.

## Labels & milestones

Maintainers apply `type:*`, `priority:*`, and `status:*` labels to issues. They also add issues to phase **milestones**
(Pre-Alpha → 1.0). Refer to the [Roadmap](../docs/ROADMAP.md) for the content of each phase.

## Review

A maintainer will review your PR. The build, tests, and lint checks must pass. The change must agree with `docs/DESIGN.md`.
A maintainer can request more changes during the review.

Thank you! 💜
