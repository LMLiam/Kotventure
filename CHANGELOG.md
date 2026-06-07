# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)
once it reaches `1.0.0`. During pre-alpha (`0.0.x`), breaking changes may land in any release.

## [Unreleased]

### Added

- Project migrated to the `LMLiam` namespace (`io.github.lmliam.kyoriadventuredsl`).
- Open-source community files (contributing guide, security & support policies, Dependabot).
- [`docs/DESIGN.md`](docs/DESIGN.md) — full design and module architecture.
- `AGENTS.md` and `.agents/skills/` — guidance and reusable playbooks for AI coding agents (Codex-compatible `SKILL.md` format).
- `.gitattributes` for consistent line endings and language stats.
- Stricter Kotlin linting: ktlint official code style, no wildcard imports, and `explicitApi()` for library modules.
- Security workflows (SHA-pinned): CodeQL (`java-kotlin`), OpenSSF Scorecard, and Dependency Review.
- Default pull request template, alongside the existing per-type templates.
- Auto-generated release-notes config (`.github/release.yml`) grouped by `type:`/`area:` labels.
- PR auto-labeler (`.github/labeler.yml` + workflow) applying `area:*` labels by changed path.
- Dependabot grouping of minor/patch updates; Gradle parallel + build cache.
- Java 25 project baseline with Gradle Wrapper 9.5.1.

[Unreleased]: https://github.com/LMLiam/KyoriAdventureDSL/commits/master
