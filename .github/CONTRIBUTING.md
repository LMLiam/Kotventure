# Contributing to KyoriAdventureDSL

Thanks for your interest in contributing! This project is in **pre-alpha**, so the surface area changes often — small, focused contributions are the most useful.

## Be respectful
Please keep all interactions constructive and considerate. Harassment or hostile behaviour is not welcome and may result in removal from the project's spaces.

## Ways to contribute
- **Pick up an issue.** Start with [`good first issue`](https://github.com/LMLiam/KyoriAdventureDSL/labels/good%20first%20issue) or [`help wanted`](https://github.com/LMLiam/KyoriAdventureDSL/labels/help%20wanted).
- **Discuss a design** before large work — open a [Discussion](https://github.com/LMLiam/KyoriAdventureDSL/discussions) or comment on the relevant issue. The overall plan lives in [`docs/DESIGN.md`](../docs/DESIGN.md) and [`docs/ROADMAP.md`](../docs/ROADMAP.md).
- **Report a bug** using the bug issue template.

## Development setup
- **JDK 23** (the build uses a Kotlin JVM toolchain of 23; Gradle will provision it via the Foojay resolver).
- Use the **Gradle wrapper** — no local Gradle install needed.

```bash
./gradlew build          # compile + test + lint everything
./gradlew test           # run the Kotest suites
./gradlew spotlessApply  # auto-format
./gradlew ktlintCheck    # lint check
```

## Project conventions
- **One issue → one small, vertically-sliced PR.** Each change should be independently shippable with its own tests. If a PR is growing large, split it.
- **Branch naming:** `type/issue-<n>/short-desc` — e.g. `feat/issue-12/translatable-component`.
- **Tests are required** for behavioural changes. We use [Kotest](https://kotest.io/); the `test` module provides component matchers — dogfood them.
- **Formatting** is enforced by ktlint + Spotless. Run `./gradlew spotlessApply` before pushing.
- **PR templates:** pick the template that matches your change (feature / bugfix / docs / chore). Link the issue with `Closes #<n>`.

## Commit & PR title format (enforced)

Both **pull request titles** and **every commit subject** must follow:

```text
verb(area): something
```

- All lowercase `verb` and `area`; a `(area)` scope is **required**, followed by `:`, a space, and a non-empty summary.
- Pattern: `^[a-z]+\([a-z0-9][a-z0-9-]*\): [^[:space:]].*$`
- This is enforced in CI by the **Conventional Titles** workflow (`.github/workflows/conventional-titles.yml`), which runs `.github/scripts/validate-conventional-title.sh`.

**Recommended `verb`s:** `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`, `build`, `perf`.

**Recommended `area`s** (match the module / label areas): `core`, `minimessage`, `i18n`, `test`, `ansi`, `coroutines`, `ksp`, `paper`, `velocity`, `fabric`, `gradle-plugin`, `bom`, `e2e`, `docs`, `ci`, `build`, `deps`, `repo`, `meta`.

Examples: `feat(minimessage): add typed placeholder DSL` · `fix(core): correct decoration reset` · `docs(repo): document title convention`.

## Labels & milestones
Issues are triaged with `type:*`, `priority:*`, and `status:*` labels and grouped into phase **milestones** (Pre-Alpha → 1.0). See the [Roadmap](../docs/ROADMAP.md) for what each phase contains.

## Review
A maintainer will review your PR. CI must pass (build, tests, lint) and the change should follow the design in `docs/DESIGN.md`. Be ready for iteration — it's normal and welcome.

Thank you! 💜
