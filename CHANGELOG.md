# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)
once it reaches `1.0.0`. During pre-alpha (`0.0.x`), breaking changes may land in any release.

## [0.0.2](https://github.com/LMLiam/Kotventure/compare/0.0.1...0.0.2) (2026-06-09)


### ✨ Features

* **core:** add serializer extensions ([#96](https://github.com/LMLiam/Kotventure/issues/96)) ([eb43bc1](https://github.com/LMLiam/Kotventure/commit/eb43bc1d03a2fc99eddef21fc598c17d071d9ed8))
* **core:** add text decoration dsl ([#94](https://github.com/LMLiam/Kotventure/issues/94)) ([ae00907](https://github.com/LMLiam/Kotventure/commit/ae009075207ac5cb286b90080653b680068478d3))
* **core:** complete text component builder ([#102](https://github.com/LMLiam/Kotventure/issues/102)) ([5a35ff7](https://github.com/LMLiam/Kotventure/commit/5a35ff74e959df9106811748cb8e0ed32f7ffbe9))


### 🐛 Fixes

* **repo:** retarget issue template triage label ([f7b4d8e](https://github.com/LMLiam/Kotventure/commit/f7b4d8e7c84d6e52f2481bf519291d5b6889e86e))


### 🔧 Refactors

* **core:** remove ServiceLoader SPI for a hybrid registry ([#87](https://github.com/LMLiam/Kotventure/issues/87)) ([727ddb7](https://github.com/LMLiam/Kotventure/commit/727ddb7223c9d0457d46115057785d51f56f1fdc))


### 📝 Documentation

* **docs:** add pre-alpha plan and roadmap ([#3](https://github.com/LMLiam/Kotventure/issues/3)) ([7c64292](https://github.com/LMLiam/Kotventure/commit/7c64292e8ac1e08367c5fc77b2c6258841c1a73e))
* **readme:** add Adventure API version badge ([777ad48](https://github.com/LMLiam/Kotventure/commit/777ad4899bc9c0778e87dbef6f44d064a3a3b2a0))
* **readme:** describe project goals ([3250a2a](https://github.com/LMLiam/Kotventure/commit/3250a2a48b47ed94efdcac2de5acb861f3a37f4a))
* **readme:** fix license link ([651e012](https://github.com/LMLiam/Kotventure/commit/651e01216fd40fd4bfc7a4fd72f2e95c78901d44))
* **readme:** link documentation ([4c18f9f](https://github.com/LMLiam/Kotventure/commit/4c18f9fa349bfcaabb57020da4c90589899d09fe))
* **readme:** sync Kotlin badge with latest ([be7dd24](https://github.com/LMLiam/Kotventure/commit/be7dd24f7768f68be7f97cbcc4878ee1f597919d))
* **repo:** add MIT license ([4b83302](https://github.com/LMLiam/Kotventure/commit/4b833021fc1844e6884cb82085c6a219456eabd6))
* **repo:** rename project to Kotventure ([122487f](https://github.com/LMLiam/Kotventure/commit/122487f0f59be14c2f54705280e3cb95a3c7aaf7))

## [Unreleased]

### Added

- Text DSL composition helpers: `append(Component)`, `newline()`, and inline `style { }` blocks for the current
  component.
- Translatable component DSL support for translation keys, fallback text, component arguments, boolean arguments, numeric
  arguments, root styling, and child components.
- Translatable component matchers for asserting keys, fallback text, and translation arguments in Kotest specs.

## [0.0.1] - 2026-06-08

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
- Kotventure builds with the Java 25 Gradle toolchain; Adventure 5.x sets a Java 21+ consumer minimum.

[Unreleased]: https://github.com/LMLiam/Kotventure/compare/0.0.1...HEAD
[0.0.1]: https://github.com/LMLiam/Kotventure/releases/tag/0.0.1
