# Kotventure — Design

> This document specifies the agreed architecture and scope. The GitHub epic and its subissues use this document.
> The syntax examples are **illustrative**. The implementation can refine them.

---

## 1. Vision & positioning

Kotventure is a **complete, multiplatform Kotlin DSL for [Adventure](https://github.com/PaperMC/adventure)**. It gives
Kotlin plugin developers a correct method to make and send player-facing text and user interfaces. It supports Paper,
Velocity, and Fabric.

Three primary features distinguish it from other libraries:

1. **Typed, validated MiniMessage** supplies reusable typed templates. The templates have required, type-checked
   placeholders and markup validation.
2. **A component test toolkit** supplies Kotest and JUnit matchers. It also supplies snapshot tests for Adventure
   components.
3. **Developer tools** render components in a terminal with ANSI. They generate a typed message catalogue from
   resource files with KSP. They also validate MiniMessage bundles during a Gradle build.

Kotventure also supports all component types, styles, themes, and audience operations. Audience operations include
titles, boss bars, books, sounds, and tab lists. The target scope also includes pagination, animation,
internationalisation, and idiomatic serialiser access.

## 2. Prior art & differentiation

| Project                           | Description                                                                 | Difference                                                                  |
|-----------------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| Adventure's retired Kotlin extras | Former official Kotlin builders/operators                                   | Adventure 5.0 removed it; it has no MiniMessage tools, tests, or platform UI |
| Pluto‑Studio/`adventure-kt`       | Component DSL, `mini()`, styles, titles, and multiplatform support          | No typed MiniMessage, test toolkit, code generator, or ANSI tools           |
| HoshiKurama component DSL         | `buildComponent {}`                                                         | Stale (2021), narrow                                                        |
| KSpigot / KPaper                  | General Kotlin server libraries that include a chat DSL                    | Not specific to Adventure; part of larger frameworks                        |

**Conclusion:** provide a broad scope and tools that help users make correct software. Do not compete only on a basic
component builder.

## 3. Design principles

- **Use idiomatic Kotlin.** Use extension functions, builders, and `@DslMarker` scopes. Do not use runtime mechanisms
  for a usual operation.
- **Use small units with clear limits.** Give each module one purpose and a defined public API. Test each module
  independently.
- **Make correct use easy.** Use type safety, validation, and direct tests.
- **Reject malformed input.** Do not normalise malformed input. If a builder slot accepts one value, a second
  assignment throws `IllegalStateException` and identifies the argument. Inputs that permit repetition accumulate in
  call order. Use `IllegalArgumentException` for invalid values such as ranges, finite numbers, and blank names.
- **Include only required dependencies.** A consumer that uses only `core` does not receive MiniMessage, coroutines,
  or platform code.
- **Use Kotventure in its tests.** Test each module with the matchers from the `test` module.

## 4. Architecture & module map

The architecture uses three mechanisms. An idiomatic DSL supplies most of the API. Small, **explicit registries**
supply runtime extension points. **KSP** generates code at compile time. The project does not use the previous
`ServiceLoader` and `@ServiceContract` factory indirection.

| Module                | Depends on                             | Purpose                                                                                                        |
|-----------------------|----------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `core`                | `adventure-api`                        | Component/style/colour/gradient DSL, theme engine, the registry, audience‑send DSL, animation **abstractions** |
| `serializer`          | `adventure-api`, concrete serialisers  | Optional `Component` serialiser extensions such as MiniMessage and plain text                                  |
| `minimessage`         | `core`, `adventure-text-minimessage`   | Typed tag/placeholder DSL, typed templates, validation, MiniMessage ⇄ DSL converter                            |
| `i18n`                | `core`, `minimessage`                  | Translation registry + per‑player locale DSL                                                                   |
| `test`                | `core` (test‑scoped for consumers)     | Kotest/JUnit structural component matchers                                                                     |
| `test-snapshot`       | `adventure-api`, `serializer`          | Snapshot testing over canonical component JSON                                                                 |
| `ansi`                | `core`                                 | Render a `Component` to coloured terminal output                                                               |
| `coroutines`          | `core`, `kotlinx-coroutines`           | suspend click‑callbacks, async sending, animation scheduling                                                   |
| `annotations` + `ksp` | —                                      | Typed message‑catalogue codegen + compile‑time style validation                                                |
| `paper`               | `core` (+ Paper)                       | Scheduler/audience adapter, item creation and **lore**/display‑name builders                                  |
| `velocity`            | `core` (+ Velocity)                    | Proxy scheduler/audience adapter                                                                               |
| `fabric`              | `core` (+ `adventure-platform-fabric`) | Fabric adapter                                                                                                 |
| `gradle-plugin`       | (own build)                            | Validate / pre‑compile MiniMessage resource bundles at build time                                              |
| `bom`                 | —                                      | Bill‑of‑materials so consumers pin one version                                                                 |
| `e2e`                 | (all)                                  | Cross‑module integration tests                                                                                 |

Add a module only when its roadmap phase requires it. This rule keeps each change small.

### 4.1 Registries & extension points

When a feature requires runtime lookup, that feature supplies an explicit registry in its public API. Do not use a
hidden process-wide registry or scan the class path.

- **Theme providers** use a `ThemeRegistry` instance for dynamic lookup and interoperability. Direct Kotlin callers
  use compiler-checked properties such as `Brand.header`.
- **Custom MiniMessage tags**, **animation drivers**, and **platform adapters** must use the same pattern. The feature
  owns an explicit registry. It does not use ambient global state.

This design keeps usual operations direct and permits the replacement of variable parts.

**Animation layers** arrive in Phase 3. The `core` module defines the frame model, ticker, and driver interface.
Concrete **animation drivers** use the applicable registry. The `coroutines` module and platform schedulers control
the runtime schedule. The composition flow is **abstractions → registered driver → scheduled frame execution**.

## 5. Canonical DSL surface (illustrative)

Selector filters that permit negation use a prefix (`!tag("hidden")`). The project removed the former
`tag(!"hidden")` form. Thus, all filters use one syntax.

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
// themes.replace(BrandV2, default = true)   // hot-reload under the same name
// themes.unregister(Brand)                  // preferred: remove by object reference
// themes.unregister("brand")                // string bridge for dynamic/interop names
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
    emitter(self)           // follows the recipient; or at(x, y, z) — world position
}
player.stopSound { source(music) }
player.stopSound { all() }
player.tabList {
    header { text("Welcome") }
    footer { text("play.example.org") }
}

// Managed (timed) boss bars — context(ticker) once; `over` opts into lifecycle management
// val ticker = plugin.ticker()   // platform-provided (kotventure-paper); ManualTicker in tests
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
object Welcome : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
    val player by placeholder<Component>()
    val count by placeholder<Int>()
}
player.message(
    Welcome {
        player bind name
        count bind 3
    },
)

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

`parseSelector(...)` is the only dynamic string bridge into the selector DSL. It validates the selector source. It
returns the typed, immutable `EntitySelector` model that the target-specific DSL factories produce. Invalid or unknown
syntax throws `EntitySelectorParseException`. The exception gives the zero-based offset of the first failure in the
selector. The DSL does not have an unchecked selector representation.

Both construction methods produce a typed list of `EntitySelectorArgument` values. One renderer changes that model
back to canonical selector source. The DSL scopes are the compile-time interface to the model. `parseSelector(...)` is
the strict runtime interface for dynamic strings. The model stores meaning and not lexical form. It retains argument
order and repetitions. It renders quote choices, escapes, omitted `minecraft:` namespaces, number forms, redundant
exact ranges, and empty argument brackets in a canonical form. For example, `5..5` becomes `5`. The vanilla
conformance suite tests duplicate arguments and interactions between arguments (#205).

The tests also compare selector output with a checksum-pinned Java Edition parser. For the isolated test configuration
and baseline update procedure, refer to [Vanilla conformance](vanilla-conformance.md).

## 6. MiniMessage strategy

The project supplies three MiniMessage layers in sequence:

1. **Passthrough** supplies `mini("<red>hi")` as a wrapper for the parser. It also supplies a tag resolver DSL.
2. **Typed placeholders and templates** use `object : MiniTemplate("…") { val x by placeholder<T>() }`. This syntax
   produces reusable, type-checked message factories. The property name is the tag. Use
   `placeholder<T>("tag")` when the names are different.
3. **Validation** detects malformed tags and incorrect placeholder sets. The runtime API validates at load time. The
   Gradle plugin validates resource bundles at build time.

A **MiniMessage ⇄ DSL converter** changes markup strings to Kotlin DSL and back. This tool helps migration and
learning. The strict `core` parser converts selector patterns and emits typed selector factories and canonical
arguments. Invalid patterns cause an exception that gives the applicable offset.

## 7. Testing strategy

- Use **Kotest** in all modules.
- The `test` module supplies matchers such as `shouldHaveColor` and `shouldContainText`. It also supplies structural,
  child, and style matchers.
- The `test-snapshot` module serialises a complete message and compares it with an approved snapshot file.
- Each other module uses these matchers to test its output.
- The `e2e` module tests integration between modules.

## 8. Tooling

- **`ansi`** renders a `Component` as ANSI for tests and logs. It supports colours, decorations, and approximate
  gradients. Developers can examine output without a server.
- **`ksp`** generates a typed message catalogue from `messages.yml` or property files. The generated accessors have
  required, validated placeholders. It can also validate styles at compile time.
- **`gradle-plugin`** stops a build when resource bundles contain malformed MiniMessage or absent placeholders. It can
  also precompile bundles.

## 9. Platforms

`core` depends only on `adventure-api`. Thus, it works on all Adventure platforms. Small bundles add platform adapters
and convenience functions:

- **`paper`** supplies an animation scheduler, audience functions, and item lore and name builders.
- **`velocity`** supplies a proxy scheduler and audience adapter.
- **`fabric`** uses `adventure-platform-fabric`.

## 10. Build, publishing & versioning

- **Build:** Gradle multi‑module, Kotlin 2.4, JVM toolchain 25, ktlint + Spotless, Kotest.
- **Java compatibility:** Kotventure builds with the Java 25 toolchain. Adventure 5.x requires Java 21 or a later
  version for consumers. Public APIs must wrap or compose Adventure types. They must not extend Adventure component or
  style interfaces.
- **Adventure baseline:** Kotventure aligns Adventure artefacts with the Adventure BOM in
  [`gradle/libs.versions.toml`](../gradle/libs.versions.toml). The current version is 5.2.0. The `core` module wraps
  `adventure-api`. Feature modules add serialiser, MiniMessage, platform, or tool artefacts only when the roadmap
  requires them.
- **Publication:** During the pre-alpha and alpha stages, JitPack builds from Git tags. At beta or version `1.0`, Maven
  Central uses the `io.github.lmliam` namespace and GPG signatures.
- The **BOM** module aligns versions across all artefacts.
- **Versions:** `0.0.x` is unstable, `0.x` is alpha, and `0.9.x` is beta with a frozen API. Version `1.0.0` starts the
  semantic-versioning commitment. CI builds, tests, and lints pull requests. Tags start publication.

> **Note:** To add or update a GitHub Actions workflow, use a token with the `workflow` scope. Use
> `gh auth refresh -s workflow` to add the scope. A separate issue tracks CI on `master`.

### 10.1 Adventure 5.x compatibility

The Adventure version in [`gradle/libs.versions.toml`](../gradle/libs.versions.toml) is the compatibility baseline for
all new roadmap work. Use PaperMC's official
[Adventure 4.x → 5.x migration guide](https://docs.papermc.io/adventure/migration/adventure-4.x/) when you add a
dependency or public DSL type.

- Keep the project build on Java 25. State the Adventure minimum of Java 21 in release and setup documents.
- Use composition or delegation with Adventure builders and value types. Do not extend sealed Adventure component,
  style, renderer, or event implementation types.
- Use the current Adventure 5.x serialiser, translation store, renderer, component builder, and click event APIs. Do
  not add removed Adventure 4.x modules or classes to dependencies or planned public APIs.
- Consider JSpecify nullness, SLF4J 2.0 requirements, and Adventure module metadata when they affect Kotlin source
  compatibility or consumer configuration.

Roadmap issues with 5.x notes are serialisers (#30), click events (#21), renderer and object-component operations
(#71/#81), translation (#16/#48), NBT components (#18), and component builders (#8/#15).

## 11. Phased roadmap

Each phase is a GitHub **milestone**. Each subissue is a small vertical slice with its own tests. The project can
release each slice independently.

| Phase | Milestone         | Focus                                                                                                                                                                 |
|-------|-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **0** | Pre‑Alpha `0.0.x` | Foundations: drop SPI, restructure, CI, JitPack, BOM stub. **First slice:** `component { text { color/decorate } }` + first matcher + `toMiniMessage()`. Tag `0.0.1`. |
| **1** | Alpha `0.1–0.3`   | Core DSL: full components, styles/events/gradients, themes; MiniMessage typed templates + validation + converter; serializer extensions; test matchers + snapshots.   |
| **2** | Alpha `0.4–0.6`   | Audience & UX: send DSL (message/actionbar/title/book/sound/tablist), managed boss bars/titles, pagination, GUI/lore builders (Paper), coroutines.                    |
| **3** | Alpha `0.7–0.8`   | Animation engine + built‑ins, i18n registry + locale DSL, typed message catalogue (KSP), ANSI preview, Gradle build plugin.                                           |
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
| Typed message catalogue (codegen) | 3                                 |
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
