<!-- markdownlint-disable MD041 MD022 MD058 -->
[![adventure-api](https://img.shields.io/badge/dynamic/json?url=https://raw.githubusercontent.com/LMLiam/Kotventure/master/gradle/versions.json&label=adventure%20api&query=$.adventure-api&color=blue)](https://github.com/PaperMC/adventure/releases/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.md)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Status: Pre-Alpha](https://img.shields.io/badge/status-pre--alpha-orange.svg)](docs/ROADMAP.md)
<!-- markdownlint-enable MD041 MD022 MD058 -->

# Kotventure

[🖱️ Docs](https://github.com/LMLiam/Kotventure/tree/master/docs) · [🗺️ Roadmap](docs/ROADMAP.md) · [🏗️ Design](docs/DESIGN.md) · [🤝 Contributing](.github/CONTRIBUTING.md)

Kotventure is a Kotlin‑focused domain‑specific language designed to provide a fluent, expressive, and type‑safe way of building rich [Adventure](https://github.com/PaperMC/adventure) components — and everything around them.

This project aims to:
- Streamline the creation of complex Adventure component trees with a more ergonomic syntax
- Cover the **whole** player-facing surface — components, MiniMessage, titles, boss bars, books, sounds, audiences
- Bring serious **correctness tooling** to the space: component test matchers, snapshot testing, and load-time MiniMessage validation
- Offer an extensible foundation so plugin developers can introduce their own themes, styles, and behaviors
- Integrate cleanly into existing Adventure‑powered projects without boilerplate

> **Status:** Pre‑Alpha (`0.0.x`) — DSL surface, syntax, and extension points are still evolving. See the [Roadmap](docs/ROADMAP.md).
> **Examples:** Land incrementally as features stabilise; see [`docs/DESIGN.md`](docs/DESIGN.md) for the target syntax.

---

## 🛠 Built For
- Developers using **Adventure** in Kotlin — Paper, Velocity, and Fabric
- Teams who value **clear, safe, reproducible** text construction workflows
- Extensible architectures where downstream theming or plugin integration is a priority

---

## 📦 Modules (target architecture)
| Module | Purpose |
|--------|---------|
| `core` | Component / style / colour DSL, themes, audience-send DSL, serializer extensions |
| `minimessage` | Typed MiniMessage templates, validation, MiniMessage ⇄ DSL converter |
| `i18n` | Translation registry + per-player locale DSL |
| `test` | Kotest/JUnit component matchers + snapshot testing |
| `ansi` | Render a `Component` to coloured terminal output |
| `coroutines` | suspend click-callbacks, async sending, animation scheduling |
| `paper` / `velocity` / `fabric` | Platform adapters & extras |
| `ksp` | Typed message-catalog codegen + compile-time validation |
| `gradle-plugin` | Validate / pre-compile MiniMessage resource bundles at build time |

See [`docs/DESIGN.md`](docs/DESIGN.md) for the full design and the [Roadmap](docs/ROADMAP.md) for sequencing.

---

## 🚀 Getting It (early access)
Pre-alpha snapshots will be published via [JitPack](https://jitpack.io). Coordinates and a worked example land with the first tagged slice (`0.0.1`).

---

## 🤝 Contributing
Contributions are welcome! Please read the [Contributing Guide](.github/CONTRIBUTING.md). Good entry points are issues labelled [`good first issue`](https://github.com/LMLiam/Kotventure/labels/good%20first%20issue).

## 🔐 Security
Found a vulnerability? Please follow the [Security Policy](.github/SECURITY.md) rather than opening a public issue.

---

## 📄 License
Distributed under the MIT License — see [`LICENSE`](LICENSE.md) for details.
