# Kotventure — Design

> This document captures the agreed architecture and scope that the GitHub Epic and its sub‑issues are derived from.
> Syntax shown is **illustrative** and will be refined during implementation.

---

## 1. Vision & positioning

Kotventure is a **batteries‑included, multi‑platform Kotlin DSL for [Adventure](https://github.com/PaperMC/adventure)**.
The goal is not merely "a nicer way to build a `Component`" — that ground is already covered — but to be the most
complete and *correct* way for Kotlin plugin developers to produce and deliver player‑facing text and UX across Paper,
Velocity, and Fabric.

Three pillars set it apart from existing efforts:

1. **Typed, validated MiniMessage** — reusable typed templates with required, type‑checked placeholders and
   load/build‑time validation of markup.
2. **A real component‑testing toolkit** — Kotest/JUnit matchers and snapshot testing for Adventure components. Nobody in
   this space offers this.
3. **Developer tooling** — render components to the terminal (ANSI), generate a typed message catalog from resource
   files (KSP), and validate MiniMessage bundles at build time (Gradle plugin).

On top of those, it aims to match the breadth of the best rival: full component coverage, styles/themes, the entire
audience surface (titles, boss bars, books, sounds, tab list), pagination, animations, i18n, and idiomatic serializer
access.

## 2. Prior art & differentiation

| Project                           | What it is                                                                  | Gap we exploit                                                              |
|-----------------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| Adventure's retired Kotlin extras | Former official Kotlin builders/operators                                   | Removed in Adventure 5.0; no MiniMessage tooling, testing, or platform UX   |
| Pluto‑Studio/`adventure-kt`       | The serious rival — component DSL, `mini()`, styles, titles, multi‑platform | No typed/validated MiniMessage, no testing toolkit, no codegen/ANSI tooling |
| HoshiKurama component DSL         | `buildComponent {}`                                                         | Stale (2021), narrow                                                        |
| KSpigot / KPaper                  | Broad Kotlin server libs that *include* a chat DSL                          | Not Adventure‑focused; tied to broader frameworks                           |

**Conclusion:** compete on *breadth + correctness tooling*, not on the basic component builder alone.

## 3. Design principles

- **Idiomatic first.** Plain extension functions, builders, and `@DslMarker` scopes. No runtime magic for the common
  path.
- **Small, well‑bounded units.** Each module has one clear purpose, a defined public surface, and is testable in
  isolation.
- **Correctness is a feature.** Type‑safety, validation, and first‑class tests are differentiators, not afterthoughts.
- **Reject malformed input; never normalize it away.** A builder slot that can only be meaningfully set once throws
  `IllegalStateException` (naming the argument) on a second assignment instead of last‑write‑wins; genuinely repeatable
  inputs accumulate in call order. Value validation (ranges, finiteness, blank names) uses `IllegalArgumentException`.
- **Pay for what you use.** A consumer pulling only `core` shouldn't drag in MiniMessage, coroutines, or platform code.
- **Dogfood.** Every module is tested using our own `test` matchers.

## 4. Architecture & module map

A **hybrid** structure: idiomatic DSL for ~95% of the surface, a small **explicit registry** for genuinely pluggable
behaviour, and **KSP** for compile‑time codegen. The previous `ServiceLoader`/`@ServiceContract` factory indirection is
**removed** — it is unnecessary ceremony for a DSL library.

| Module                | Depends on                             | Purpose                                                                                                        |
|-----------------------|----------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `core`                | `adventure-api`                        | Component/style/colour/gradient DSL, theme engine, the registry, audience‑send DSL, animation **abstractions** |
| `serializer`          | `adventure-api`, concrete serializers  | Optional `Component` serializer extensions such as MiniMessage and plain text                                  |
| `minimessage`         | `core`, `adventure-text-minimessage`   | Typed tag/placeholder DSL, typed templates, validation, MiniMessage ⇄ DSL converter                            |
| `i18n`                | `core`, `minimessage`                  | Translation registry + per‑player locale DSL                                                                   |
| `test`                | `core` (test‑scoped for consumers)     | Kotest/JUnit structural component matchers                                                                     |
| `test-snapshot`       | `adventure-api`, `serializer`          | Snapshot testing over canonical component JSON                                                                 |
| `ansi`                | `core`                                 | Render a `Component` to coloured terminal output                                                               |
| `coroutines`          | `core`, `kotlinx-coroutines`           | suspend click‑callbacks, async sending, animation scheduling                                                   |
| `annotations` + `ksp` | —                                      | Typed message‑catalog codegen + compile‑time style validation                                                  |
| `paper`               | `core` (+ Paper)                       | Scheduler/audience adapter, item **lore**/display‑name builders                                                |
| `velocity`            | `core` (+ Velocity)                    | Proxy scheduler/audience adapter                                                                               |
| `fabric`              | `core` (+ `adventure-platform-fabric`) | Fabric adapter                                                                                                 |
| `gradle-plugin`       | (own build)                            | Validate / pre‑compile MiniMessage resource bundles at build time                                              |
| `bom`                 | —                                      | Bill‑of‑materials so consumers pin one version                                                                 |
| `e2e`                 | (all)                                  | Cross‑module integration tests                                                                                 |

Modules are introduced **lazily, per phase** — not all scaffolded up front — to keep each change small.

### 4.1 Registries & extension points

When runtime lookup is genuinely needed, the owning feature exposes an explicit registry value as part of its public
API — never a hidden process-global registry or classpath scanning.

- **Theme providers** use a `ThemeRegistry` instance for dynamic lookup and interop, while direct Kotlin callers prefer
  compile-checked properties such as `Brand.header`.
- **Custom MiniMessage tags**, **animation drivers**, and **platform adapters** should follow the same pattern when
  their slices land: feature-owned explicit registration, not ambient global state.

This keeps the common path magic-free while making the genuinely variable parts swappable.

**Animation layering** (arrives in Phase 3) spans three of these layers, so the split is deliberate: `core` defines the
animation *abstractions* (frame model, ticker, and the driver interface); concrete **animation drivers** plug in via the
registry entry above; and the `coroutines` module — together with the platform schedulers from the bundles — provides
the runtime *scheduling and orchestration*. The composition flow is **abstractions → a driver registered here → the
driver schedules/executes frames**.

## 5. Canonical DSL surface (illustrative)

Negatable selector filters use prefix negation (`!tag("hidden")`). The former value-wrapped form
(`tag(!"hidden")`) has been removed so every filter, including structured filters, follows one syntax.

```kotlin
// ── Construction ───────────────────────────────────────────────
val msg = component {
    text("Hello ") { color(AQUA); bold() }
    "world" { color(hex("#FF00AA")) }        // string-literal sugar for text("world") { … }
    +"!"                                      // bare literal → plain text child
    newline()
    translatable("item.minecraft.diamond") { fallback("Diamond") }
    keybind("key.jump") { color(YELLOW) }
    score("Alex", "kills")
    selector(allPlayers()) { separator { content(", ") } }
    selector(
        entities {
            typeTag(key("minecraft", "raiders"))
            origin(64.y)
            volume(16.dx, 8.dy, 16.dz)
            !tag("hidden")
            nbt { "Health" eq 20.0f }
            !nbt { "Invisible" eq true }
            scores { "kills" eq atLeast(10) }
            !predicate(key("my_pack", "hidden"))
            advancements { key("minecraft", "story/smelt_iron") eq true }
        },
    )
    blockNbt(blockPos(1, 64, 1), "Items[0].id")
    entityNbt("@p", "CustomName") { interpret(true) }
    storageNbt(key("kotventure", "messages"), "motd") { interpret(true) }
    display(sprite(key("minecraft", "block/stone"))) { fallback { text("[stone]") } }
    display(head("Steve")) { fallback { text("[Steve]") } }
    mini("<gradient:gold:red>Epic</gradient>")
}

val parsedSelector = parseSelector("@e[type=minecraft:zombie,tag=!hidden]")

// ── Reusable styles ────────────────────────────────────────────
val headerStyle = style {
    color(GOLD)
    shadow(BLACK)
    bold()
    italic(false)
    underlined(null)
    font(key("minecraft", "uniform"))
    insertion("/help")
}

val title = component {
    text("Title") { style(headerStyle) }
}

// ── Themes (design system, registered once) ────────────────────
object Brand : Theme("brand") {
    val primary = hex("#5865F2")

    val header: Style by style { color(primary); bold() }
    val error: Style by style { color(RED) }
}
val themes = ThemeRegistry()
themes.register(Brand)                       // explicit startup wiring
text("Title") styled Brand.header            // compile-checked property
themes.theme("brand")?.style("header")       // dynamic interop lookup

// ── Sending (Audience extensions) ──────────────────────────────
player.message { text("hi") }
player.actionBar { text("+10 XP") }
player.title {
    title { text("Welcome") }
    subtitle { mini("<gray>to the server") }
    times {
        fadeIn(1.ticks)
        stay(3.seconds)
        fadeOut(1.ticks)
    }
}
val dragon = bossBar {
    name { text("Ender Dragon") { color(0x9B30FF) } }
    progress(0.25f)
    color(red)              // BossBar.Color — scope-bound, no enum import
    overlay(notched10)
    darkenScreen()
    playBossMusic()
}
player.show(dragon)
player.hide(dragon)
val raid = player.bossBar { name { text("Raid") } }   // build + show; keep for hide/updates

val rules = book {
    title { text("Server Rules") }
    author { text("Staff") }
    page { text("Be kind.") }
}
player.open(rules)
player.book {
    page { text("One-shot welcome") }
}
val alert = sound(key("minecraft:block.bell.use")) {   // build once, share
    source(music)           // Sound.Source — scope-bound, no enum import
    volume(2f)
    pitch(0.5f)
}
player.play(alert)
player.sound(key("minecraft:entity.pig.ambient")) {    // build + play one-shot
    emitter(self)           // or at(x, y, z) — emitter-relative playback
}
player.stopSound { source(music) }
player.stopSound { all() }

// Managed (timed) boss bars — context(ticker) once; `over` opts into lifecycle management
// val ticker = paperTicker(plugin)   // platform-provided; ManualTicker in tests
context(ticker) {
    val meteor = player.bossBar(over = 30.seconds) {
        name { remaining -> text("Meteor in ${remaining.inWholeSeconds}s") }
        color(red)
        overlay(notched10)
        progress(from = 1f, to = 0f)   // default countdown
        every(1.ticks)
        onFinish { /* natural completion */ }
    }
    meteor.pause(); meteor.resume(); meteor.cancel()
}


// ── Typed MiniMessage template + validation ────────────────────
val Welcome = miniTemplate("<gold>Welcome <player>, <count> new messages") {
    placeholder<Component>("player")
    placeholder<Int>("count")
}
player.message(Welcome { player = name; count = 3 })

// ── Typed catalog (KSP from messages.yml) ──────────────────────
Messages.welcome(player = name, count = 3)   // generated, validated placeholders

// ── Normalising & traversing ───────────────────────────────────
val tidy = msg.compact()                     // Adventure normalisation
msg.asSequence()                             // lazy Sequence<Component> → full stdlib over the tree
    .onEach { node -> log(node) }            // depth-first, pre-order visit
    .filterIsInstance<ObjectComponent>()     // object components are preserved, not dropped

// ── Testing / preview ──────────────────────────────────────────
component shouldHaveColor AQUA
component shouldContainText "world"
component shouldMatchSnapshot "welcome"
println(component.toAnsi())
println(component.toMiniMessage())
println(component.toPlainText())
```

`parseSelector(...)` is the single dynamic-string bridge into the selector DSL: it validates
selector source and returns the same typed, immutable `EntitySelector` model produced by the
target-specific DSL factories. Invalid or unknown syntax throws an `EntitySelectorParseException`
carrying the zero-based, selector-relative offset of the first failure, matching the fail-fast
contract of the rest of the DSL. There is no unchecked selector representation.

Both construction paths produce a typed `EntitySelectorArgument` list, and a single renderer turns
that model back into canonical selector source. The DSL scopes are the compile-time front end over
the model; `parseSelector(...)` is the strict runtime front end for dynamic strings. The model is
semantic, not lexical: argument order and repetition are retained, while quote choice, escapes,
omitted `minecraft:` namespaces, number spelling, redundant exact ranges (`5..5` renders as `5`),
and empty argument brackets re-render canonically. Duplicate-argument and cross-argument vanilla
semantics are pinned separately by the vanilla-conformance suite (#205).

Selector rendering is also checked independently against the checksum-pinned Java Edition parser;
see [Vanilla conformance](vanilla-conformance.md) for the isolated test setup and baseline
update process.

## 6. MiniMessage strategy

Three layers, shipped incrementally:

1. **Passthrough** — `mini("<red>hi")` wrapping the parser, plus a tag‑resolver DSL.
2. **Typed placeholders & templates** — `miniTemplate(...) { placeholder<T>(...) }` producing reusable, type‑checked
   message factories.
3. **Validation** — detect malformed tags and missing/extra placeholders at load time (runtime API) and at **build time
   ** (Gradle plugin over resource bundles).

A **MiniMessage ⇄ DSL converter** round‑trips between markup strings and DSL/Kotlin, aiding migration and learning.
Selector patterns convert through the strict `core` parser and emit the typed selector factories and arguments,
canonicalized; invalid patterns fail with the parser's offset-bearing exception.

## 7. Testing strategy

- **Kotest** throughout (already the project's framework).
- The `test` module exposes **matchers** (`shouldHaveColor`, `shouldContainText`, structural/child matchers, style
  assertions); `test-snapshot` adds whole-message golden-file assertions (serialize → diff against committed
  snapshots).
- Every other module **exercises** these matchers on its own output.
- `e2e` covers cross‑module integration.

## 8. Tooling

- **`ansi`** — render a `Component` (colours, decorations, gradients approximated) to ANSI for tests and logs, so
  developers can *see* output without a server.
- **`ksp`** — generate a typed message catalog from `messages.yml`/properties: typed accessors with **required,
  validated** placeholders; optional compile‑time style validation.
- **`gradle-plugin`** — fail the build on malformed MiniMessage or missing placeholders in resource bundles; optionally
  pre‑compile bundles.

## 9. Platforms

`core` depends only on `adventure-api`, so it works anywhere Adventure does. Thin bundles add platform adapters and
conveniences:

- **`paper`** (first) — scheduler for animations, audience conveniences, item lore/name builders.
- **`velocity`** — proxy scheduler/audience.
- **`fabric`** — via `adventure-platform-fabric`.

## 10. Build, publishing & versioning

- **Build:** Gradle multi‑module, Kotlin 2.4, JVM toolchain 25, ktlint + Spotless, Kotest.
- **Java compatibility:** Kotventure builds with the Java 25 toolchain. Adventure 5.x sets a Java 21+ consumer floor, so
  modules should keep public APIs wrapper/composition-based rather than extending Adventure component/style interfaces.
- **Adventure baseline:** Kotventure aligns Adventure artifacts through the Adventure 5.1.1 BOM. The core module wraps
  `adventure-api`; feature modules add serializer, MiniMessage, platform, or tooling artifacts only when their roadmap
  slice lands.
- **Publishing:** **JitPack** during pre‑alpha/alpha (zero infra, builds from git tags) → **Maven Central** (
  `io.github.lmliam` namespace, GPG‑signed) at beta/`1.0`.
- **BOM** module so consumers align versions across the many artifacts.
- **Versioning:** unstable `0.0.x` → `0.x` alpha → `0.9.x` beta (API freeze) → `1.0.0` (semver commitment). CI runs
  build/test/lint on PRs; tags publish.

> **Note:** adding/updating GitHub Actions workflows requires a token with the `workflow` scope (
`gh auth refresh -s workflow`). CI‑on‑master is tracked as its own issue.

### 10.1 Adventure 5.x compatibility

Adventure 5.1.1 is the compatibility baseline for all new roadmap slices. Treat PaperMC's official
[Adventure 4.x → 5.x migration guide](https://docs.papermc.io/adventure/migration/adventure-4.x/) as the checklist
when adding dependencies or public DSL types.

- Keep the project build on Java 25 while documenting Adventure's Java 21+ consumer minimum in release and setup docs.
- Use composition/delegation around Adventure builders and value types; do not extend sealed Adventure component,
  style, renderer, or event implementation types.
- Prefer current Adventure 5.x serializer, translation-store, renderer, component-builder, and click-event APIs in each
  feature slice. Removed Adventure 4.x modules or classes must not appear as dependencies or planned public API.
- Account for JSpecify nullness, SLF4J 2.0 expectations, and Adventure module metadata whenever they affect Kotlin
  source compatibility or consumer setup.

Roadmap issues checked for 5.x notes: serializers (#30), click events (#21), renderer/object-component handling
(#71/#81), translation (#16/#48), NBT components (#18), and component builders (#8/#15).

## 11. Phased roadmap

Each phase is a GitHub **milestone**. Sub‑issues are **fine‑grained vertical slices** — each independently shippable
with its own tests — to keep changes small and incremental.

| Phase | Milestone         | Focus                                                                                                                                                                 |
|-------|-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **0** | Pre‑Alpha `0.0.x` | Foundations: drop SPI, restructure, CI, JitPack, BOM stub. **First slice:** `component { text { color/decorate } }` + first matcher + `toMiniMessage()`. Tag `0.0.1`. |
| **1** | Alpha `0.1–0.3`   | Core DSL: full components, styles/events/gradients, themes; MiniMessage typed templates + validation + converter; serializer extensions; test matchers + snapshots.   |
| **2** | Alpha `0.4–0.6`   | Audience & UX: send DSL (message/actionbar/title/book/sound/tablist), managed boss bars/titles, pagination, GUI/lore builders (Paper), coroutines.                    |
| **3** | Alpha `0.7–0.8`   | Animation engine + built‑ins, i18n registry + locale DSL, typed message catalog (KSP), ANSI preview, Gradle build plugin.                                             |
| **4** | Beta `0.9.x`      | Velocity + Fabric bundles, compile‑time style validation (KSP), API freeze, perf pass, docs/cookbook, integration tests.                                              |
| **5** | `1.0.0`           | Maven Central signed publishing, semver commitment, sample plugin, migration guide, final docs.                                                                       |

## 12. Feature → phase matrix

| Feature                           | Phase                             |
|-----------------------------------|-----------------------------------|
| Component test matchers           | 0 (seed) → 1 (full)               |
| Snapshot / golden testing         | 1                                 |
| Compile‑time style validation     | 4                                 |
| MiniMessage ⇄ DSL converter       | 1                                 |
| Animations                        | 3                                 |
| Chat pagination                   | 2                                 |
| GUI / lore & item‑text builders   | 2                                 |
| Managed boss bars / titles        | 2                                 |
| Typed message catalog (codegen)   | 3                                 |
| Translation registry + locale DSL | 3                                 |
| Design‑system themes              | 1                                 |
| Colour & gradient helpers         | 1                                 |
| ANSI terminal preview             | 3                                 |
| Coroutine integration             | 2                                 |
| Gradle build plugin               | 3                                 |
| Serializer extensions             | 0 (seed, `serializer`) → 1 (full) |

## 13. Open questions / future

- Optional **Sponge** / **BungeeCord** bundles post‑1.0.
- IDE inspections / detekt rules for the DSL (long‑term tooling).
- A hosted **playground** / cookbook docs site.
- Scoreboard/sidebar helpers (not Adventure core — evaluate as a Paper‑bundle extra).
