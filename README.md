<!-- markdownlint-disable MD041 MD022 MD058 -->
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.md)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Status: Alpha](https://img.shields.io/badge/status-alpha-orange.svg)](docs/ROADMAP.md)
<!-- markdownlint-enable MD041 MD022 MD058 -->

# Kotventure

[🖱️ Docs](https://github.com/LMLiam/Kotventure/tree/master/docs) · [🗺️ Roadmap](docs/ROADMAP.md) · [🏗️ Design](docs/DESIGN.md) · [🤝 Contributing](.github/CONTRIBUTING.md)

A fluent, type-safe Kotlin DSL for building [Adventure](https://github.com/PaperMC/adventure) components — and
everything around them. Kotventure wraps Adventure with an idiomatic DSL and adds the correctness tooling the space
lacks: typed MiniMessage, component test matchers, snapshot testing, and load-time validation.

> **Alpha.** The DSL surface may still change before `1.0.0`. See the [Roadmap](docs/ROADMAP.md), and
> [`docs/DESIGN.md`](docs/DESIGN.md) for the target syntax across the whole player-facing surface.

```kotlin
val message = component {
    text("Hello ") {
        color(NamedTextColor.AQUA)
        bold()
    }
    text("[new]") { color(NamedTextColor.GREEN) }
    text(" Website") {
        click { openUrl("https://example.com") }
    }
}
```

## Why Kotventure

- **Ergonomic component trees** — a composable builder for components, styles, colours, and events.
- **Typed MiniMessage** — `mini(...)` parsing, typed `placeholder<T>(name)` resolvers, and reusable `MiniTemplate`s
  with compile-checked bindings (`player bind value`).
- **Correctness tooling** — Kotest component matchers, snapshot assertions over canonical JSON, and load-time
  MiniMessage validation that reports malformed tags and missing/extra placeholders.
- **Serializer helpers** — `Component`/`String` extensions for Adventure's legacy, JSON, plain-text, and MiniMessage
  formats.
- **Extensible by design** — feature-owned explicit registries (e.g. themes) rather than hidden global state.

## Modules

Modules land lazily, per phase. The table is the target architecture; see [`docs/DESIGN.md`](docs/DESIGN.md) for the
full design and the [Roadmap](docs/ROADMAP.md) for sequencing.

| Module                          | Purpose                                                                 | Status |
|---------------------------------|-------------------------------------------------------------------------|--------|
| `core`                          | Component / style / colour DSL, themes, audience-send DSL                | ✅      |
| `minimessage`                   | Typed MiniMessage templates, validation, MiniMessage ⇄ DSL converter    | ✅      |
| `serializer`                    | Optional Adventure serializer extension functions                       | ✅      |
| `test`                          | Kotest/JUnit component matchers                                          | ✅      |
| `test-snapshot`                 | Snapshot testing over canonical component JSON                          | ✅      |
| `i18n`                          | Translation registry + per-player locale DSL                            | 🔜     |
| `ansi`                          | Render a `Component` to coloured terminal output                        | 🔜     |
| `coroutines`                    | suspend click-callbacks, async sending, animation scheduling            | 🔜     |
| `paper` / `velocity` / `fabric` | Platform adapters & extras                                              | 🔜     |
| `ksp`                           | Typed message-catalog codegen + compile-time validation                 | 🔜     |
| `gradle-plugin`                 | Validate / pre-compile MiniMessage resource bundles at build time       | 🔜     |
| `bom`                           | Bill of materials for aligning Kotventure and Adventure module versions | ✅      |

## Getting it (early access)

Tagged releases are published through [JitPack](https://jitpack.io). Import the BOM once, then depend on the modules
you need without repeating versions:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(platform("com.github.LMLiam.Kotventure:kotventure-bom:<tag>"))

    implementation("com.github.LMLiam.Kotventure:kotventure-core")
    implementation("com.github.LMLiam.Kotventure:kotventure-minimessage")
    implementation("com.github.LMLiam.Kotventure:kotventure-serializer")

    testImplementation("com.github.LMLiam.Kotventure:kotventure-test")
    testImplementation("com.github.LMLiam.Kotventure:kotventure-test-snapshot") // optional snapshot support
}
```

Replace `<tag>` with a released tag (e.g. `0.0.1`). The `test` artifacts are test-scope only. The BOM aligns
Kotventure's Adventure baseline at 5.1.1.

## Build & compatibility

Kotventure builds and tests with the Java 25 Gradle toolchain. Its Adventure baseline (5.1.1) sets a Java 21+ minimum
for consumers. Release process and maintainer permissions live in [`docs/RELEASING.md`](docs/RELEASING.md).

## Contributing

Contributions are welcome — read the [Contributing Guide](.github/CONTRIBUTING.md). Good entry points are issues
labelled [`good first issue`](https://github.com/LMLiam/Kotventure/labels/good%20first%20issue). For vulnerabilities,
follow the [Security Policy](.github/SECURITY.md) rather than opening a public issue.

## License

Distributed under the MIT License — see [`LICENSE`](LICENSE.md).
