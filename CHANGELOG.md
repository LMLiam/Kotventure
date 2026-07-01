# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)
once it reaches `1.0.0`. During the `0.x` line, breaking changes may land in any release.

## [Unreleased]

### ✨ Features

* **core:** add all typed selector heads with capability-safe scopes
* **core:** add selector negation, entity type tags, and tag-presence filters
* **core:** add selector origin and bounding-volume arguments
* **core:** add typed selector rotation ranges with wrap-around support

## [0.4.1](https://github.com/LMLiam/Kotventure/compare/0.4.0...0.4.1) (2026-06-25)


### 🔧 Refactors

* **core:** phase 1 quality pass across modules, docs, and tooling ([#169](https://github.com/LMLiam/Kotventure/issues/169)) ([704fd9d](https://github.com/LMLiam/Kotventure/commit/704fd9dbd06425caa11b8c179287de867d28505f))

## [0.4.0](https://github.com/LMLiam/Kotventure/compare/0.3.0...0.4.0) (2026-06-16)


### ✨ Features

* **core:** add component utility extensions ([#162](https://github.com/LMLiam/Kotventure/issues/162)) ([283be31](https://github.com/LMLiam/Kotventure/commit/283be31283b8e787eba789623a30bf7b37934018))
* **serializer:** complete serializer extensions ([#160](https://github.com/LMLiam/Kotventure/issues/160)) ([6ea8938](https://github.com/LMLiam/Kotventure/commit/6ea89385a167e2678abc2bb6662a92f2aee829f8))

## [0.3.0](https://github.com/LMLiam/Kotventure/compare/0.2.0...0.3.0) (2026-06-16)


### ✨ Features

* **core:** shadow-colour and player-head DSL surfaces ([#159](https://github.com/LMLiam/Kotventure/issues/159)) ([41cc6df](https://github.com/LMLiam/Kotventure/commit/41cc6df8106d13715f25f5924e8f4088341fb8f0))
* **minimessage:** convert MiniMessage to DSL ([#149](https://github.com/LMLiam/Kotventure/issues/149)) ([2c91d97](https://github.com/LMLiam/Kotventure/commit/2c91d9702708b49b3d6200d55191144fa104c028))
* **minimessage:** mini→DSL converter — click & hover events (slice 2) ([#151](https://github.com/LMLiam/Kotventure/issues/151)) ([78fcf70](https://github.com/LMLiam/Kotventure/commit/78fcf708d4ab5a82154bf2a7fcfbbc14bb77ae8f))
* **minimessage:** mini→DSL converter — NBT/object/font/insertion/gradient (slice 4) ([#156](https://github.com/LMLiam/Kotventure/issues/156)) ([82ea8b0](https://github.com/LMLiam/Kotventure/commit/82ea8b009173b14b1223f9639efa1f16476f0add))
* **minimessage:** mini→DSL converter — structured components (slice 3) ([#155](https://github.com/LMLiam/Kotventure/issues/155)) ([e82db7d](https://github.com/LMLiam/Kotventure/commit/e82db7d82b2ac3cb7fb4497ead942580a7233f1b))

## [0.2.0](https://github.com/LMLiam/Kotventure/compare/0.1.0...0.2.0) (2026-06-15)

### ✨ Features

* **minimessage:** add typed
  placeholders ([#142](https://github.com/LMLiam/Kotventure/issues/142)) ([02ce043](https://github.com/LMLiam/Kotventure/commit/02ce04337ad870b4a7f09cbfd7e09a670a1a13b8))
* **minimessage:** add typed reusable message
  templates ([#144](https://github.com/LMLiam/Kotventure/issues/144)) ([722a5cd](https://github.com/LMLiam/Kotventure/commit/722a5cd92a9425e6e6e88b2e00abb1377fc8a5a1))
* **minimessage:** validate markup and
  placeholders ([#145](https://github.com/LMLiam/Kotventure/issues/145)) ([bd3d6be](https://github.com/LMLiam/Kotventure/commit/bd3d6be2230b64aaeaafe39cc4c49de73a1c4f10))

### 📝 Documentation

* **skills:** add detailed guidelines for agents and DSL
  workflows ([182a423](https://github.com/LMLiam/Kotventure/commit/182a4235bf20454eb351788c30871051b1fe228f))

## [0.1.0](https://github.com/LMLiam/Kotventure/compare/0.0.11...0.1.0) (2026-06-12)

### ✨ Features

* **minimessage:** add mini() passthrough and a tag-resolver
  DSL ([#138](https://github.com/LMLiam/Kotventure/issues/138)) ([5cd2a6b](https://github.com/LMLiam/Kotventure/commit/5cd2a6b03f6fe6385ea8a106ab3af3e59d75b6d4))

## [0.0.11](https://github.com/LMLiam/Kotventure/compare/0.0.10...0.0.11) (2026-06-12)

### ✨ Features

* **core:** add the component joining
  dsl ([#134](https://github.com/LMLiam/Kotventure/issues/134)) ([384e620](https://github.com/LMLiam/Kotventure/commit/384e620a4bbd9a5b39990009d28c8bee7d8487d7))

### 🔧 Refactors

* **core:** rename XScopeBuilder classes to
  XBuilder ([#137](https://github.com/LMLiam/Kotventure/issues/137)) ([a95b834](https://github.com/LMLiam/Kotventure/commit/a95b834959e21b3fe6075c29ded8228fc464095a))

### 📝 Documentation

* **skills:** clarify API design guidelines and enforce pre-planning
  checks ([ab5e4a9](https://github.com/LMLiam/Kotventure/commit/ab5e4a9fcecd4b81291144bbe8fb5ab5347b95e2))

## [0.0.10](https://github.com/LMLiam/Kotventure/compare/0.0.9...0.0.10) (2026-06-12)

### ✨ Features

* **core:** add design-system themes and a
  registry ([#130](https://github.com/LMLiam/Kotventure/issues/130)) ([e968324](https://github.com/LMLiam/Kotventure/commit/e9683249b1d2c9d51c4aa0138d299f144dd2d0b4))

### 🔧 Refactors

* **core:** internalize the registry behind feature
  facades ([#133](https://github.com/LMLiam/Kotventure/issues/133)) ([0b0cdb3](https://github.com/LMLiam/Kotventure/commit/0b0cdb3bec43dd9fdb0e961480ebcf8399d74acc))

## [0.0.9](https://github.com/LMLiam/Kotventure/compare/0.0.8...0.0.9) (2026-06-11)

### ✨ Features

* **core:** add click event
  dsl ([#125](https://github.com/LMLiam/Kotventure/issues/125)) ([a24b11f](https://github.com/LMLiam/Kotventure/commit/a24b11fe2660d138b16b3cf98816e879d0779402))
* **core:** add hover event
  dsl ([#128](https://github.com/LMLiam/Kotventure/issues/128)) ([e089e58](https://github.com/LMLiam/Kotventure/commit/e089e58bb6e4ace4722da4bca7d769098b4ab28f))
* **core:** replace direct click
  helpers ([#129](https://github.com/LMLiam/Kotventure/issues/129)) ([8090219](https://github.com/LMLiam/Kotventure/commit/8090219070789305bfe3b874292146069e1e10c3))

## [0.0.8](https://github.com/LMLiam/Kotventure/compare/0.0.7...0.0.8) (2026-06-11)

### ✨ Features

* **core:** add colour and gradient
  helpers ([#123](https://github.com/LMLiam/Kotventure/issues/123)) ([0482b7f](https://github.com/LMLiam/Kotventure/commit/0482b7f2c135419f1cc46ec2f78372bba5749b67))

## [0.0.7](https://github.com/LMLiam/Kotventure/compare/0.0.6...0.0.7) (2026-06-10)

### ✨ Features

* **core:** add object component
  dsl ([#120](https://github.com/LMLiam/Kotventure/issues/120)) ([48e4618](https://github.com/LMLiam/Kotventure/commit/48e46184b27f4513751e82f1efedc575046eb306))
* **core:** add reusable style
  dsl ([#122](https://github.com/LMLiam/Kotventure/issues/122)) ([b4231e4](https://github.com/LMLiam/Kotventure/commit/b4231e49d084948e4b967bc08ceb05f613ef4999))

## [0.0.6](https://github.com/LMLiam/Kotventure/compare/0.0.5...0.0.6) (2026-06-10)

### ✨ Features

* **core:** add adventure key
  helpers ([#118](https://github.com/LMLiam/Kotventure/issues/118)) ([e20e326](https://github.com/LMLiam/Kotventure/commit/e20e326ae0e5c644acc406a6727e61aa2b32d17c))
* **core:** add block nbt position
  helpers ([#116](https://github.com/LMLiam/Kotventure/issues/116)) ([e4465d4](https://github.com/LMLiam/Kotventure/commit/e4465d4798107ee3511d530e8095611ebb40f846))

## [0.0.5](https://github.com/LMLiam/Kotventure/compare/0.0.4...0.0.5) (2026-06-10)

### ✨ Features

* **core:** add block, entity and storage nbt
  components ([#112](https://github.com/LMLiam/Kotventure/issues/112)) ([ae5f438](https://github.com/LMLiam/Kotventure/commit/ae5f4382cc027002a174add91a715f79714b9b86))

## [0.0.4](https://github.com/LMLiam/Kotventure/compare/0.0.3...0.0.4) (2026-06-10)

### ✨ Features

* **core:** add keybind, score and selector
  components ([#110](https://github.com/LMLiam/Kotventure/issues/110)) ([3d12be9](https://github.com/LMLiam/Kotventure/commit/3d12be9df99c2fbf9df6d2068f1ba47f64d1bf74))

## [0.0.3](https://github.com/LMLiam/Kotventure/compare/0.0.2...0.0.3) (2026-06-09)

### ✨ Features

* **core:** add translatable component
  builder ([#108](https://github.com/LMLiam/Kotventure/issues/108)) ([2453b68](https://github.com/LMLiam/Kotventure/commit/2453b68c666e0d6fbb62d3b075301235650d3771))

## [0.0.2](https://github.com/LMLiam/Kotventure/compare/0.0.1...0.0.2) (2026-06-09)

### ✨ Features

* **core:** add serializer
  extensions ([#96](https://github.com/LMLiam/Kotventure/issues/96)) ([eb43bc1](https://github.com/LMLiam/Kotventure/commit/eb43bc1d03a2fc99eddef21fc598c17d071d9ed8))
* **core:** add text decoration
  dsl ([#94](https://github.com/LMLiam/Kotventure/issues/94)) ([ae00907](https://github.com/LMLiam/Kotventure/commit/ae009075207ac5cb286b90080653b680068478d3))
* **core:** complete text component
  builder ([#102](https://github.com/LMLiam/Kotventure/issues/102)) ([5a35ff7](https://github.com/LMLiam/Kotventure/commit/5a35ff74e959df9106811748cb8e0ed32f7ffbe9))

### 🐛 Fixes

* **repo:** retarget issue template triage
  label ([f7b4d8e](https://github.com/LMLiam/Kotventure/commit/f7b4d8e7c84d6e52f2481bf519291d5b6889e86e))

### 🔧 Refactors

* **core:** remove ServiceLoader SPI for a hybrid
  registry ([#87](https://github.com/LMLiam/Kotventure/issues/87)) ([727ddb7](https://github.com/LMLiam/Kotventure/commit/727ddb7223c9d0457d46115057785d51f56f1fdc))

### 📝 Documentation

* **docs:** add pre-alpha plan and
  roadmap ([#3](https://github.com/LMLiam/Kotventure/issues/3)) ([7c64292](https://github.com/LMLiam/Kotventure/commit/7c64292e8ac1e08367c5fc77b2c6258841c1a73e))
* **readme:** add Adventure API version
  badge ([777ad48](https://github.com/LMLiam/Kotventure/commit/777ad4899bc9c0778e87dbef6f44d064a3a3b2a0))
* **readme:** describe project
  goals ([3250a2a](https://github.com/LMLiam/Kotventure/commit/3250a2a48b47ed94efdcac2de5acb861f3a37f4a))
* **readme:** fix license
  link ([651e012](https://github.com/LMLiam/Kotventure/commit/651e01216fd40fd4bfc7a4fd72f2e95c78901d44))
* **readme:** link
  documentation ([4c18f9f](https://github.com/LMLiam/Kotventure/commit/4c18f9fa349bfcaabb57020da4c90589899d09fe))
* **readme:** sync Kotlin badge with
  latest ([be7dd24](https://github.com/LMLiam/Kotventure/commit/be7dd24f7768f68be7f97cbcc4878ee1f597919d))
* **repo:** add MIT
  license ([4b83302](https://github.com/LMLiam/Kotventure/commit/4b833021fc1844e6884cb82085c6a219456eabd6))
* **repo:** rename project to
  Kotventure ([122487f](https://github.com/LMLiam/Kotventure/commit/122487f0f59be14c2f54705280e3cb95a3c7aaf7))

## [Unreleased]

### Added

- `miniToDsl(input)` in `kotventure-minimessage` for converting MiniMessage text into Kotventure DSL source for plain
  text, recursive children, named/hex colours, standard text decorations, and click/hover events.
- Typed MiniMessage placeholders via `placeholder<T>(name)` and `resolve(...)`, covering component, string, numeric,
  and boolean values.
- MiniMessage passthrough parsing via `kotventure-minimessage`, including `mini(...)`, placeholder resolvers for parsed,
  unparsed, and component values, and `ComponentScope.mini(...)` integration for use inside the core DSL.
- Text DSL composition helpers: `append(Component)`, `newline()`, and inline `style { }` blocks for the current
  component.
- Translatable component DSL support for translation keys, fallback text, component arguments, boolean arguments,
  numeric
  arguments, root styling, and child components.
- Translatable component matchers for asserting keys, fallback text, and translation arguments in Kotest specs.
- Shared `ComponentScope` support for styling and child builders across every component DSL scope.
- Keybind, score, and selector component DSL support, including selector separators, root styling, and nested child
  builders.
- Keybind, score, and selector component matchers for dogfooding the new DSL output.
- Block, entity, and storage NBT component DSL support, including interpretation, separators, root styling, nested child
  builders, and NBT-specific test matchers.
- Block NBT position helper functions for absolute, relative, and string-parsed coordinates.
- Adventure Key helper functions for namespace/value pairs, infix namespace syntax, and string parsing.
- Object component DSL support through `display(...)` and `sprite(...)`, including fallback components, root styling,
  nested child builders, explicit renderer coverage, and serializer round-trip coverage.
- Object component matchers for asserting contents and fallback components in Kotest specs.
- Reusable style DSL support through `style { ... }`, tri-state decoration helpers, font keys, shift-click insertion,
  and matching component assertions for style attributes.
- Colour and gradient helpers for `hex("#RRGGBB")`, `rgb(...)`, normalized `hsv(...)`, named palette aliases,
  interpolation, and per-code-point gradient text.
- Click event DSL support for Adventure's typed click actions and server-side callbacks, including reusable `click { }`
  factories, scoped block application, file-URI-aware `open`, Kotlin duration callback options, and click-event
  component
  matchers.
- Hover event DSL support for typed text, item data component, and entity payloads, including reusable hover factories,
  clearing/prebuilt interop, MiniMessage round-trip coverage, and hover-event component matchers.
- Serializer helpers for legacy ampersand, legacy section, JSON, plain text, and MiniMessage round-trips through
  `kotventure-serializer`, including matching `String` parsing extensions.

### Changed

- Click action helpers now live only inside `click { ... }`; the direct `open(...)`, `openUrl(...)`, `openFile(...)`,
  `run(...)`, `suggest(...)`, `changePage(...)`, `copy(...)`, and `callback(...)` helpers on component/style scopes were
  removed before the pre-alpha event DSL settles.

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
