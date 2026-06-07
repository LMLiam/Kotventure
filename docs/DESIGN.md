# KyoriAdventureDSL — Design

> **Status:** Living design document · **Stage:** Pre‑Alpha (`0.0.x`) · **Last updated:** 2026‑06‑07
>
> This document captures the agreed architecture, scope, and roadmap. It is the source of truth that the GitHub Epic and its sub‑issues are derived from. Syntax shown is **illustrative** and will be refined during implementation.

---

## 1. Vision & positioning

KyoriAdventureDSL is a **batteries‑included, multi‑platform Kotlin DSL for [Kyori Adventure](https://github.com/KyoriPowered/adventure)**. The goal is not merely "a nicer way to build a `Component`" — that ground is already covered — but to be the most complete and *correct* way for Kotlin plugin developers to produce and deliver player‑facing text and UX across Paper, Velocity, and Fabric.

Three pillars set it apart from existing efforts:

1. **Typed, validated MiniMessage** — reusable typed templates with required, type‑checked placeholders and load/build‑time validation of markup.
2. **A real component‑testing toolkit** — Kotest/JUnit matchers and snapshot testing for Adventure components. Nobody in this space offers this.
3. **Developer tooling** — render components to the terminal (ANSI), generate a typed message catalog from resource files (KSP), and validate MiniMessage bundles at build time (Gradle plugin).

On top of those, it aims to match the breadth of the best rival: full component coverage, styles/themes, the entire audience surface (titles, boss bars, books, sounds, tab list), pagination, animations, i18n, and idiomatic serializer access.

## 2. Prior art & differentiation

| Project | What it is | Gap we exploit |
|---|---|---|
| `net.kyori:adventure-extra-kotlin` | Official, minimal builder DSL + operators | Tiny surface; no MiniMessage tooling, testing, or platform UX |
| Pluto‑Studio/`adventure-kt` | The serious rival — component DSL, `mini()`, styles, titles, multi‑platform | No typed/validated MiniMessage, no testing toolkit, no codegen/ANSI tooling |
| HoshiKurama/`KyoriComponentDSL` | `buildComponent {}` | Stale (2021), narrow |
| KSpigot / KPaper | Broad Kotlin server libs that *include* a chat DSL | Not Adventure‑focused; tied to broader frameworks |

**Conclusion:** compete on *breadth + correctness tooling*, not on the basic component builder alone.

## 3. Design principles

- **Idiomatic first.** Plain extension functions, builders, and `@DslMarker` scopes. No runtime magic for the common path.
- **Small, well‑bounded units.** Each module has one clear purpose, a defined public surface, and is testable in isolation.
- **Correctness is a feature.** Type‑safety, validation, and first‑class tests are differentiators, not afterthoughts.
- **Pay for what you use.** A consumer pulling only `core` shouldn't drag in MiniMessage, coroutines, or platform code.
- **Dogfood.** Every module is tested using our own `test` matchers.

## 4. Architecture & module map

A **hybrid** structure: idiomatic DSL for ~95% of the surface, a small **explicit registry** for genuinely pluggable behaviour, and **KSP** for compile‑time codegen. The previous `ServiceLoader`/`@ServiceContract` factory indirection is **removed** — it is unnecessary ceremony for a DSL library.

| Module | Depends on | Purpose |
|---|---|---|
| `core` | `adventure-api` | Component/style/colour/gradient DSL, serializer extensions, theme engine, the registry, audience‑send DSL, animation **abstractions** |
| `minimessage` | `core`, `adventure-text-minimessage` | Typed tag/placeholder DSL, typed templates, validation, MiniMessage ⇄ DSL converter |
| `i18n` | `core`, `minimessage` | Translation registry + per‑player locale DSL |
| `test` | `core` (test‑scoped for consumers) | Kotest/JUnit matchers + snapshot testing |
| `ansi` | `core` | Render a `Component` to coloured terminal output |
| `coroutines` | `core`, `kotlinx-coroutines` | suspend click‑callbacks, async sending, animation scheduling |
| `annotations` + `ksp` | — | Typed message‑catalog codegen + compile‑time style validation |
| `paper` | `core` (+ Paper) | Scheduler/audience adapter, item **lore**/display‑name builders |
| `velocity` | `core` (+ Velocity) | Proxy scheduler/audience adapter |
| `fabric` | `core` (+ `adventure-platform-fabric`) | Fabric adapter |
| `gradle-plugin` | (own build) | Validate / pre‑compile MiniMessage resource bundles at build time |
| `bom` | — | Bill‑of‑materials so consumers pin one version |
| `e2e` | (all) | Cross‑module integration tests |

Modules are introduced **lazily, per phase** — not all scaffolded up front — to keep each change small.

### 4.1 The registry (extension points)

A single explicit `AdventureDsl` registry (plain Kotlin, no classpath scanning) holds the pluggable pieces:

- **Custom MiniMessage tags** (`TagResolver`s) registered by name.
- **Theme providers** — named design systems resolvable across the app.
- **Animation drivers** — how frames are scheduled (no‑op/test, coroutine, Paper scheduler, …).
- **Platform adapters** — `Scheduler` + `AudienceProvider` supplied by `paper`/`velocity`/`fabric` bundles, registered on init or passed explicitly.

This keeps the common path magic‑free while making the genuinely variable parts swappable.

**Animation layering** (arrives in Phase 3) spans three of these layers, so the split is deliberate: `core` defines the animation *abstractions* (frame model, ticker, and the driver interface); concrete **animation drivers** plug in via the registry entry above; and the `coroutines` module — together with the platform schedulers from the bundles — provides the runtime *scheduling and orchestration*. The composition flow is **abstractions → a driver registered here → the driver schedules/executes frames**.

## 5. Canonical DSL surface (illustrative)

```kotlin
// ── Construction ───────────────────────────────────────────────
val msg = component {
    text("Hello ") { color(AQUA); bold() }
    text("world") { color(hex("#FF00AA")) }
    newline()
    translatable("item.minecraft.diamond") { fallback("Diamond") }
    mini("<gradient:gold:red>Epic</gradient>")
}

// ── Themes (design system, registered once) ────────────────────
val Brand = theme {
    palette { primary = hex("#5865F2"); error = RED }
    style("header") { color(primary); bold() }
}
text("Title") styled Brand.header

// ── Sending (Audience extensions) ──────────────────────────────
player.message { text("hi") }
player.title {
    title { text("Welcome") }
    subtitle { mini("<gray>to the server") }
    times(fadeIn = 1.ticks, stay = 3.seconds, fadeOut = 1.ticks)
}

// ── Typed MiniMessage template + validation ────────────────────
val Welcome = miniTemplate("<gold>Welcome <player>, <count> new messages") {
    placeholder<Component>("player")
    placeholder<Int>("count")
}
player.message(Welcome { player = name; count = 3 })

// ── Typed catalog (KSP from messages.yml) ──────────────────────
Messages.welcome(player = name, count = 3)   // generated, validated placeholders

// ── Testing / preview ──────────────────────────────────────────
component shouldHaveColor AQUA
component shouldContainText "world"
component.shouldMatchSnapshot()
println(component.toAnsi())
```

## 6. MiniMessage strategy

Three layers, shipped incrementally:

1. **Passthrough** — `mini("<red>hi")` wrapping the parser, plus a tag‑resolver DSL.
2. **Typed placeholders & templates** — `miniTemplate(...) { placeholder<T>(...) }` producing reusable, type‑checked message factories.
3. **Validation** — detect malformed tags and missing/extra placeholders at load time (runtime API) and at **build time** (Gradle plugin over resource bundles).

A **MiniMessage ⇄ DSL converter** round‑trips between markup strings and DSL/Kotlin, aiding migration and learning.

## 7. Testing strategy

- **Kotest** throughout (already the project's framework).
- The `test` module exposes **matchers** (`shouldHaveColor`, `shouldContainText`, structural/child matchers, style assertions) and **snapshot testing** (serialize → diff against committed snapshots) so message regressions fail CI.
- Every other module **exercises** these matchers on its own output.
- `e2e` covers cross‑module integration.

## 8. Tooling

- **`ansi`** — render a `Component` (colours, decorations, gradients approximated) to ANSI for tests and logs, so developers can *see* output without a server.
- **`ksp`** — generate a typed message catalog from `messages.yml`/properties: typed accessors with **required, validated** placeholders; optional compile‑time style validation.
- **`gradle-plugin`** — fail the build on malformed MiniMessage or missing placeholders in resource bundles; optionally pre‑compile bundles.

## 9. Platforms

`core` depends only on `adventure-api`, so it works anywhere Adventure does. Thin bundles add platform adapters and conveniences:

- **`paper`** (first) — scheduler for animations, audience conveniences, item lore/name builders.
- **`velocity`** — proxy scheduler/audience.
- **`fabric`** — via `adventure-platform-fabric`.

## 10. Build, publishing & versioning

- **Build:** Gradle multi‑module, Kotlin 2.2, JVM toolchain 25, ktlint + Spotless, Kotest.
- **Publishing:** **JitPack** during pre‑alpha/alpha (zero infra, builds from git tags) → **Maven Central** (`io.github.lmliam` namespace, GPG‑signed) at beta/`1.0`.
- **BOM** module so consumers align versions across the many artifacts.
- **Versioning:** unstable `0.0.x` → `0.x` alpha → `0.9.x` beta (API freeze) → `1.0.0` (semver commitment). CI runs build/test/lint on PRs; tags publish.

> **Note:** adding/updating GitHub Actions workflows requires a token with the `workflow` scope (`gh auth refresh -s workflow`). CI‑on‑master is tracked as its own issue.

## 11. Phased roadmap

Each phase is a GitHub **milestone**. Sub‑issues are **fine‑grained vertical slices** — each independently shippable with its own tests — to keep changes small and incremental.

| Phase | Milestone | Focus |
|---|---|---|
| **0** | Pre‑Alpha `0.0.x` | Foundations: drop SPI, restructure, CI, JitPack, BOM stub. **First slice:** `component { text { color/decorate } }` + first matcher + `toMiniMessage()`. Tag `0.0.1`. |
| **1** | Alpha `0.1–0.3` | Core DSL: full components, styles/events/gradients, themes; MiniMessage typed templates + validation + converter; serializer extensions; test matchers + snapshots. |
| **2** | Alpha `0.4–0.6` | Audience & UX: send DSL (message/actionbar/title/book/sound/tablist), managed boss bars/titles, pagination, GUI/lore builders (Paper), coroutines. |
| **3** | Alpha `0.7–0.8` | Animation engine + built‑ins, i18n registry + locale DSL, typed message catalog (KSP), ANSI preview, Gradle build plugin. |
| **4** | Beta `0.9.x` | Velocity + Fabric bundles, compile‑time style validation (KSP), API freeze, perf pass, docs/cookbook, integration tests. |
| **5** | `1.0.0` | Maven Central signed publishing, semver commitment, sample plugin, migration guide, final docs. |

## 12. Feature → phase matrix

| Feature | Phase |
|---|---|
| Component test matchers | 0 (seed) → 1 (full) |
| Snapshot / golden testing | 1 |
| Compile‑time style validation | 4 |
| MiniMessage ⇄ DSL converter | 1 |
| Animations | 3 |
| Chat pagination | 2 |
| GUI / lore & item‑text builders | 2 |
| Managed boss bars / titles | 2 |
| Typed message catalog (codegen) | 3 |
| Translation registry + locale DSL | 3 |
| Design‑system themes | 1 |
| Colour & gradient helpers | 1 |
| ANSI terminal preview | 3 |
| Coroutine integration | 2 |
| Gradle build plugin | 3 |
| Serializer extensions | 0 (seed) → 1 (full) |

## 13. Open questions / future

- Optional **Sponge** / **BungeeCord** bundles post‑1.0.
- IDE inspections / detekt rules for the DSL (long‑term tooling).
- A hosted **playground** / cookbook docs site.
- Scoreboard/sidebar helpers (not Adventure core — evaluate as a Paper‑bundle extra).
