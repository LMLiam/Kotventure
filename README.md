<!-- markdownlint-disable MD041 MD022 MD058 -->
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.md)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Status: Pre-Alpha](https://img.shields.io/badge/status-pre--alpha-orange.svg)](docs/ROADMAP.md)
<!-- markdownlint-enable MD041 MD022 MD058 -->

# Kotventure

[🖱️ Docs](https://github.com/LMLiam/Kotventure/tree/master/docs) · [🗺️ Roadmap](docs/ROADMAP.md) · [🏗️ Design](docs/DESIGN.md) · [🤝 Contributing](.github/CONTRIBUTING.md)

Kotventure is a Kotlin‑focused domain‑specific language designed to provide a fluent, expressive, and type‑safe way of
building rich [Adventure](https://github.com/PaperMC/adventure) components — and everything around them.

This project aims to:

- Streamline the creation of complex Adventure component trees with a more ergonomic syntax
- Cover the **whole** player-facing surface — components, MiniMessage, titles, boss bars, books, sounds, audiences
- Bring serious **correctness tooling** to the space: component test matchers, snapshot testing, and load-time
  MiniMessage validation
- Offer an extensible foundation so plugin developers can introduce their own themes, styles, and behaviors
- Integrate cleanly into existing Adventure‑powered projects without boilerplate

> **Status:** Pre‑Alpha (`0.0.x`) — DSL surface, syntax, and extension points are still evolving. See
> the [Roadmap](docs/ROADMAP.md).
> **Examples:** Land incrementally as features stabilise; see [`docs/DESIGN.md`](docs/DESIGN.md) for the target syntax.

---

## 🛠 Built For

- Developers using **Adventure** in Kotlin — Paper, Velocity, and Fabric
- Teams who value **clear, safe, reproducible** text construction workflows
- Extensible architectures where downstream theming or plugin integration is a priority

---

## 📦 Modules (target architecture)

| Module                          | Purpose                                                                          |
|---------------------------------|----------------------------------------------------------------------------------|
| `core`                          | Component / style / colour DSL, themes, audience-send DSL                        |
| `serializer`                    | Optional Adventure serializer extension functions                                |
| `minimessage`                   | Typed MiniMessage templates, validation, MiniMessage ⇄ DSL converter             |
| `i18n`                          | Translation registry + per-player locale DSL                                     |
| `test`                          | Kotest/JUnit component matchers + snapshot testing                               |
| `ansi`                          | Render a `Component` to coloured terminal output                                 |
| `coroutines`                    | suspend click-callbacks, async sending, animation scheduling                     |
| `paper` / `velocity` / `fabric` | Platform adapters & extras                                                       |
| `ksp`                           | Typed message-catalog codegen + compile-time validation                          |
| `gradle-plugin`                 | Validate / pre-compile MiniMessage resource bundles at build time                |
| `bom`                           | Bill of materials for aligning Kotventure and Adventure module versions          |

See [`docs/DESIGN.md`](docs/DESIGN.md) for the full design and the [Roadmap](docs/ROADMAP.md) for sequencing.

---

## ✅ Implemented So Far

The current build enables the first lazy modules:

- `kotventure-core` — the plain component builder and explicit startup registry
- `kotventure-minimessage` — `mini(...)` parsing plus typed placeholders via `placeholder<T>(name)` and
  `resolve(placeholder, value)`, alongside the `parsed`, `unparsed`, and `component` resolvers; typed reusable
  templates via `MiniTemplate` with compile-checked `bind(placeholder, value)` at the call site
- `kotventure-serializer` — `Component.toMiniMessage()` and `Component.toPlainText()` wrappers around Adventure
  serializers
- `kotventure-test` — Kotest component matchers consumed test-scoped by library modules
- `kotventure-bom` — a Gradle/Maven BOM aligning enabled Kotventure artifacts and Adventure 5.1.1 dependencies

The `core` module exposes a composable component tree builder:

```kotlin
val headerStyle = style {
    color(NamedTextColor.GOLD)
    bold()
    italic(false)
    font(key("minecraft", "uniform"))
    insertion("/help")
}

val badge = Component.text("[new]", NamedTextColor.GREEN)

val message = component {
    text("Title") {
        style(headerStyle)
    }
    text("Hello ") {
        style {
            color(NamedTextColor.AQUA)
            bold()
            underlined(null)
        }
    }
    newline()
    text {
        content("world")
        decorate(TextDecoration.UNDERLINED)
    }
    append(badge)
}
```

Click events wrap Adventure's typed `ClickEvent` factories directly. Use `click { ... }` to select one action, either
as a reusable event factory or inside component and style scopes:

```kotlin
val linkClick = click {
    open("https://example.com")
}

val linkStyle = style {
    click(linkClick)
}

val actions = component {
    text("Website") {
        style(linkStyle)
    }
    text(" copy invite") {
        click {
            copy("play.example.com")
        }
    }
    text(" claim reward") {
        click {
            callback(uses = 1, lifetime = 5.minutes) { audience ->
                audience.sendMessage(component { text("Reward claimed") })
            }
        }
    }
}
```

Inside `click { ... }`, `open(...)` creates an open-file event for usable `file:` URIs and an open-URL event otherwise;
`openUrl(...)` and `openFile(...)` remain available when you want the action to be explicit. Use `click(event)` for a
prebuilt Adventure click event and `click(null)` to clear one. Click events are available on reusable styles because
Adventure models click events as part of `Style`; Kotventure keeps that shape instead of introducing a separate link
wrapper.

Hover events use the same component/style scope model and wrap Adventure's typed `HoverEvent` payloads:

```kotlin
val itemHover = hover {
    item(
        key = key("minecraft", "diamond_sword"),
        count = 1,
    )
}

val playerId = UUID.fromString("3f5f1f4e-29cb-4c98-93f0-3c7f4b52ddee")

val hoverMessage = component {
    text("Need help?") {
        hover {
            text("Open the guide") {
                color(NamedTextColor.AQUA)
            }
        }
    }
    text(" sword") {
        hover(itemHover)
    }
    text(" player") {
        hover {
            entity(
                type = key("minecraft", "player"),
                id = playerId,
            ) {
                text("Alex")
            }
        }
    }
}
```

Use `hover(null)` to clear a hover event, or pass a prebuilt Adventure `HoverEventSource` when you already have one.
Item hovers accept typed Adventure data components with `Map<Key, DataComponentValue>`.

Colour helpers wrap Adventure `TextColor` factories directly and keep `core` free of serializer dependencies. Lower-case
named colours such as `red`, `blue`, `gold`, and `aqua` are available from `io.github.lmliam.kotventure.core.color`;
`NamedTextColor.*` works too when you prefer qualified Adventure constants.

```kotlin
val brand = hex("#5865F2")
val success = rgb(85, 255, 85)
val warning = hsv(0.12f, 1f, 1f)
val midpoint = interpolate(0.5f, red, blue)
val accent = namedColorOrThrow("dark_purple")

val gradientMessage = component {
    text("Brand ") {
        color(brand)
    }
    text("launch") {
        gradient(gold, red, aqua)
    }
}
```

It also includes a `TranslatableComponent` builder for client-side translation keys, fallbacks, and typed Adventure
arguments:

```kotlin
val itemCount = translatable("item.count") {
    fallback("You have an item stack")
    arg(Component.text("Diamond", NamedTextColor.AQUA))
    arg(3)
}
```

Smaller Adventure component kinds are available directly and inside any component scope:

```kotlin
val jumpHint = keybind("key.jump") {
    color(NamedTextColor.YELLOW)
    text(" to jump")
}

val killCount = score("Alex", "kills")

val onlinePlayers = selector("@a") {
    separator {
        content(", ")
        color(NamedTextColor.GRAY)
    }
}

val blockLoot = blockNbt(BlockNBTComponent.Pos.fromString("1 64 1"), "Items[0].id")

val playerName = entityNbt("@p", "CustomName") {
    interpret(true)
}

val storedTitle = storageNbt(key("kotventure", "messages"), "welcome.title") {
    interpret(true)
    separator(Component.text(", "))
}

val stoneIcon = display(sprite(key("minecraft", "block/stone"))) {
    fallback {
        text("[stone]")
    }
}
```

MiniMessage passthrough parsing is available as an opt-in module so `core` stays free of MiniMessage dependencies:

```kotlin
val player = placeholder<Component>("player")
val count = placeholder<Int>("count")
val channel = placeholder<String>("channel")

val alex = component {
    text("Alex") {
        color(NamedTextColor.AQUA)
    }
}

val announcement = mini("<gold>[Server]</gold> <player> joined") {
    resolve(player, alex)
}

val invites = mini("<gray>[<channel>]</gray> <player> has <count> invites") {
    resolve(channel, "chat")
    resolve(player, alex)
    resolve(count, 3)
}

val rich = mini("<prefix> <player>") {
    parsed("prefix", "<gray>[chat]</gray>") // explicit bridge for markup-bearing strings
    resolve(player, alex)
}

val nested = component {
    text("Notice: ")
    mini("<gold><player></gold> joined") {
        resolve(player, alex)
    }
}

val converted = miniToDsl("<red><bold>Hello")
// component {
//     text("Hello") {
//         color(NamedTextColor.RED)
//         bold()
//     }
// }
```

For messages that are rendered many times with different values, declare a typed template once and reuse it. Each
placeholder is a compile-checked `val`; the string argument passed to `placeholder(...)` defines the MiniMessage tag,
the `val` gives scoped typed access for `bind(placeholder, value)`, and missing bindings fail with a helpful message
listing what was omitted:

```kotlin
import io.github.lmliam.kotventure.minimessage.MiniTemplate
import io.github.lmliam.kotventure.minimessage.bind
import io.github.lmliam.kotventure.minimessage.invoke
import net.kyori.adventure.text.Component

object WelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages") {
    val player = placeholder<Component>("player")
    val count = placeholder<Int>("count")
}

val forAlex = WelcomeTemplate {
    bind(player, Component.text("Alex"))
    bind(count, 3)
}
val forSam = WelcomeTemplate {
    bind(player, Component.text("Sam"))
    bind(count, 0)
}

// Compile error — wrong value type:
//   WelcomeTemplate { bind(count, "three") }   // String is not Int

// Use-time error — forgot a placeholder:
//   WelcomeTemplate { bind(player, Component.text("Alex")) }
//   → IllegalArgumentException: Template is missing required placeholder(s): [count].
```

`validate(input, placeholders)` checks a MiniMessage string against a declared set of placeholders and returns a
structured `ValidationResult` — no exception escapes to the caller. It runs two passes: a strict-mode parse that catches
malformed or unclosed tags, and a recording parse that cross-checks which placeholder tags appear in the input. Note that
strict mode requires all standard child-allowing tags (such as `<gold>`) to be explicitly closed; an unclosed standard
tag is reported as a `MalformedTag` diagnostic.

```kotlin
import io.github.lmliam.kotventure.minimessage.placeholder
import io.github.lmliam.kotventure.minimessage.validate
import io.github.lmliam.kotventure.minimessage.validation.MiniMessageDiagnostic
import io.github.lmliam.kotventure.minimessage.validation.ValidationResult
import net.kyori.adventure.text.Component

val player = placeholder<Component>("player")
val count = placeholder<Int>("count")

// Success — well-formed input, all placeholders present, standard tags closed
val result = validate(
    input = "<gold>Welcome <player></gold>, you have <count> messages",
    placeholders = listOf(player, count),
)
// result == ValidationResult.Success

// MissingPlaceholder — 'count' declared in placeholders but absent from input
val missingResult = validate(
    input = "<gold>Welcome <player></gold>",
    placeholders = listOf(player, count),
)
// missingResult == ValidationResult.Failure(
//   diagnostics = [MissingPlaceholder("count")]
// )

// MalformedTag — '<gold>' opened but not closed (strict mode)
val malformedResult = validate(
    input = "<gold>Hello world",
    placeholders = listOf(player),
)
// malformedResult == ValidationResult.Failure(
//   diagnostics = [
//     MalformedTag("...", startIndex, endIndex),  // unclosed <gold>
//     MissingPlaceholder("player"),               // declared but absent from input
//   ]
// )

// ExtraPlaceholder — '<mystery>' tag in input has no placeholder entry
val extraResult = validate(
    input = "<gold>Hello <player></gold> <mystery>",
    placeholders = listOf(player),
)
// extraResult == ValidationResult.Failure(
//   diagnostics = [ExtraPlaceholder("mystery")]
// )

// Inspect diagnostics exhaustively
val diagnosticMessages = when (val r = missingResult) {
    is ValidationResult.Success -> emptyList()
    is ValidationResult.Failure -> r.diagnostics.map { diag ->
        when (diag) {
            is MiniMessageDiagnostic.MalformedTag ->
                "Malformed tag at [${diag.startIndex}–${diag.endIndex}]: ${diag.message}"
            is MiniMessageDiagnostic.MissingPlaceholder ->
                "Missing placeholder: <${diag.name}>"
            is MiniMessageDiagnostic.ExtraPlaceholder ->
                "Extra placeholder: <${diag.name}>"
        }
    }
}
```

`MiniTemplate.validate()` is a convenience extension that validates a template's own MiniMessage string against its
declared placeholders without needing to pass them manually:

```kotlin
import io.github.lmliam.kotventure.minimessage.MiniTemplate
import io.github.lmliam.kotventure.minimessage.placeholder
import io.github.lmliam.kotventure.minimessage.validate
import net.kyori.adventure.text.Component

object WelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
    val player = placeholder<Component>("player")
    val count = placeholder<Int>("count")
}

val templateResult = WelcomeTemplate.validate()
// templateResult == ValidationResult.Success
```

Diagnostic ordering in `ValidationResult.Failure.diagnostics`: malformed-tag diagnostics appear first, then missing
placeholders in declaration order, then extra placeholders in the order they were encountered in the input.
`result.isSuccess` and `result.isFailure` are available as concise boolean checks.

Join a list of components using `Iterable<Component>.join { }`, with optional separator, `lastSeparator`, `prefix`, and
`suffix` knobs that each accept a string-plus-styling block or a prebuilt component:

```kotlin
val players = listOf(text("Alex"), text("Steve"), text("Notch"))

val online = players.join {
    separator(", ") { color(NamedTextColor.GRAY) }
    lastSeparator(" and ") { color(NamedTextColor.GRAY) }
    prefix("Online: ") { bold() }
    suffix(".")
}
// → "Online: Alex, Steve and Notch."

val plain = players.join()            // no separators — plain concatenation

val dot = Component.text(" • ", NamedTextColor.DARK_GRAY)
val dotted = players.join { separator(dot) }   // prebuilt separator
```

Use `display` for Adventure object components such as sprites where the client can render structured non-text
content. Provide a fallback when the component might appear in places that do not support object components, such
as server MOTDs or plain-text logs. Call `stoneIcon.renderObjectFallbacks()` before handing the component to renderer
paths that should replace configured object fallbacks with regular components.

Design-system themes are Kotlin objects extending `Theme`. Palette values are ordinary properties and semantic styles
are delegated properties built with the style DSL, so `Brand.header` is compile-checked while the property name doubles
as the key for dynamic lookup. Standalone components come from the top-level `text(...)` factory and `styled`:

```kotlin
object Brand : Theme("brand") {
    val primary = hex("#5865F2")

    val header: Style by style {
        color(primary)
        bold()
        italic(false)
    }

    val danger: Style by style {
        color(red)
        bold()
    }

    val callout: Style by style("call-out") {
        color(primary)
        underlined()
    }
}

Brand.register()                                   // explicit startup wiring; register(default = true) marks the default

val title = text("Welcome") styled Brand.header    // compile-checked semantic styles

val message = component {
    text("Careful") {
        style(Brand.danger)
    }
}

val dynamic = theme("brand")?.style("header")      // dynamic interop lookup
val fallback = defaultTheme()?.style("header")
```

Declaring a theme never registers it; call `register()` during startup. Use `style("call-out") { ... }` when the
dynamic key should differ from the property name, and declare palette properties before the styles that use them.

Kotventure keeps typed extension registrations — MiniMessage tag providers, theme providers (including an optional
default theme), animation drivers, and the active platform adapter — in an internal registry. Each feature package
exposes its own small facade: a `register()` extension on the extension-point type plus a lookup such as
`theme(name)`, `defaultTheme()`, `miniMessageTag(name)`, `animationDriver(name)`, or `platformAdapter()`.
Registration is explicit at startup; there is no classpath scanning.

Serializer helpers live in `kotventure-serializer` so `kotventure-core` can stay limited to `adventure-api` while
callers opt into concrete Adventure serializers:

```kotlin
val mini = message.toMiniMessage()
val plain = message.toPlainText()
```

`kotventure-test` starts the testing toolkit with structural component matchers such as `shouldContainText`,
`shouldHaveColor`, `shouldHaveDecoration`, `shouldNotHaveDecoration`, `shouldHaveChildCount`, and translatable-specific
assertions for keys, fallbacks, and arguments. It also includes keybind, score, selector, object component, block, entity,
and storage NBT assertions for the smaller component DSLs, plus click-event and hover-event assertions for all
components.

---

## 🚀 Getting It (early access)

Tagged pre-alpha releases are available through [JitPack](https://jitpack.io). Add JitPack after your primary
repositories, import the BOM once, then depend on the modules you need without repeating versions.

Gradle Kotlin DSL:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(platform("com.github.LMLiam.Kotventure:kotventure-bom:<tag>"))

    implementation("com.github.LMLiam.Kotventure:kotventure-core")
    implementation("com.github.LMLiam.Kotventure:kotventure-serializer")

    testImplementation("com.github.LMLiam.Kotventure:kotventure-test")
}
```

Gradle Groovy DSL:

```groovy
repositories {
    mavenCentral()
    maven { url = uri('https://jitpack.io') }
}

dependencies {
    implementation platform('com.github.LMLiam.Kotventure:kotventure-bom:<tag>')

    implementation 'com.github.LMLiam.Kotventure:kotventure-core'
    implementation 'com.github.LMLiam.Kotventure:kotventure-serializer'

    testImplementation 'com.github.LMLiam.Kotventure:kotventure-test'
}
```

Replace `<tag>` with a released tag such as `0.0.1`. The `kotventure-test` artifact is intended for test scope only.
The BOM also aligns Kotventure's Adventure baseline at 5.1.1.

## 🧰 Build & Compatibility

Kotventure builds and tests with the Java 25 Gradle toolchain. Its Adventure baseline is 5.1.1, which sets a Java 21+
minimum for consumers using Kotventure artifacts.

Release automation and maintainer permissions are documented in [`docs/RELEASING.md`](docs/RELEASING.md).

---

## 🤝 Contributing

Contributions are welcome! Please read the [Contributing Guide](.github/CONTRIBUTING.md). Good entry points are issues
labelled [`good first issue`](https://github.com/LMLiam/Kotventure/labels/good%20first%20issue).

## 🔐 Security

Found a vulnerability? Please follow the [Security Policy](.github/SECURITY.md) rather than opening a public issue.

---

## 📄 License

Distributed under the MIT License — see [`LICENSE`](LICENSE.md) for details.
