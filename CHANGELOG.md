# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)
once it reaches `1.0.0`. During pre-alpha (`0.0.x`), breaking changes may land in any release.

## [Unreleased]

### Added

- Project migrated to the `LMLiam` namespace (`io.github.lmliam.kotventure`).
- Open-source community files (contributing guide, security & support policies, Dependabot).
- [`docs/DESIGN.md`](docs/DESIGN.md) — full design and module architecture.
- `AGENTS.md` and `.agents/skills/` — guidance and reusable playbooks for AI coding agents (Codex-compatible `SKILL.md`
  format).
- `.gitattributes` for consistent line endings and language stats.
- Stricter Kotlin linting: ktlint official code style, no wildcard imports, and `explicitApi()` for library modules.
- Security workflows (SHA-pinned): Qodana Community for JVM (`qodana-jvm-community`), OpenSSF Scorecard, and Dependency
  Review.
- Default pull request template, alongside the existing per-type templates.
- Auto-generated release-notes config (`.github/release.yml`) grouped by `type:`/`area:` labels.
- PR auto-labeler (`.github/labeler.yml` + workflow) applying `area:*` labels by changed path.
- Dependabot grouping of minor/patch updates; Gradle parallel + build cache.
- Java 25 project baseline with Gradle Wrapper 9.5.1.
- `core` module with the first direct `component {}` text builder slice.
- Text component decoration support through `decorate(...)`, `bold()`, `italic()`, `underlined()`, `strikethrough()`,
  and `obfuscated()`.
- `serializer` module with `Component.toMiniMessage()` and `Component.toPlainText()` wrappers around Adventure's
  MiniMessage and plain text serializers.
- `test` module with the first public Kotest component matchers for dogfooding DSL output.
- `shouldHaveDecoration` and `shouldNotHaveDecoration` matchers for root component decoration assertions.
- Explicit `AdventureDsl` registry slots for MiniMessage tag providers, themes, animation drivers, and platform
  adapters.
- Regression tests covering the new component builder, registry behavior, and absence of SPI wiring in production
  sources.
- JitPack publishing support for the enabled modules, including `maven-publish` wiring, JDK 25 JitPack setup, and
  build-time publication metadata checks.
- `bom` module publishing `kotventure-bom` so consumers can align Kotventure artifacts and Adventure 5.1.1 with one
  platform import.

### Changed

- Upgraded the Adventure dependency baseline from 4.24.0 to 5.1.1 and aligned enabled modules through the Adventure BOM.

[Unreleased]: https://github.com/LMLiam/Kotventure/commits/master
