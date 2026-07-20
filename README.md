<!-- markdownlint-disable MD033 MD041 -->
<div align="center">

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="assets/logo-dark.svg">
  <img src="assets/logo-light.svg" alt="Kotventure" width="420">
</picture>

<p><strong>The Kotlin way to write Adventure.</strong></p>

<p>
  <a href="https://github.com/LMLiam/Kotventure/actions/workflows/ci.yml"><img src="https://github.com/LMLiam/Kotventure/actions/workflows/ci.yml/badge.svg?branch=master" alt="CI"></a>
  <a href="https://github.com/LMLiam/Kotventure/releases"><img src="https://img.shields.io/github/v/release/LMLiam/Kotventure?label=release&color=8A2BE2" alt="Latest release"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.4-7F52FF.svg?logo=kotlin&logoColor=white" alt="Kotlin 2.4"></a>
  <a href="https://github.com/PaperMC/adventure"><img src="https://img.shields.io/badge/Adventure-5.2.0-FFAA00.svg" alt="Adventure 5.2.0"></a>
  <a href="https://jitpack.io/#LMLiam/Kotventure"><img src="https://jitpack.io/v/LMLiam/Kotventure.svg" alt="JitPack"></a>
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT"></a>
</p>

<p>
  <a href="docs/GETTING-STARTED.md">Getting Started</a> ·
  <a href="#the-modules">Modules</a> ·
  <a href="https://github.com/LMLiam/Kotventure/tree/master/docs">Docs</a> ·
  <a href="docs/ROADMAP.md">Roadmap</a> ·
  <a href=".github/CONTRIBUTING.md">Contributing</a>
</p>

</div>
<!-- markdownlint-enable MD033 MD041 -->

Kotventure is a complete, type-safe Kotlin DSL for [Adventure](https://github.com/PaperMC/adventure). It supports
components, styles, titles, boss bars, books, sounds, selectors, NBT, and the other content that a player sees.
Kotventure also supplies typed MiniMessage templates, component test matchers, snapshot tests, and load-time
validation.

> **Alpha.** The DSL surface may still change before `1.0.0`. See the [Roadmap](docs/ROADMAP.md), and
> [`docs/DESIGN.md`](docs/DESIGN.md) for the target syntax across the whole player-facing surface.

## At a glance

<!-- markdownlint-disable MD033 -->
<table>
<tr>
<th>Raw Adventure</th>
<th>Kotventure</th>
</tr>
<tr>
<td>

```kotlin
val warning =
    Component
        .text()
        .content("Watch out!")
        .color(NamedTextColor.RED)
        .decorate(TextDecoration.BOLD)
        .clickEvent(ClickEvent.runCommand("/duck"))
        .hoverEvent(HoverEvent.showText(Component.text("Quack")))
        .build()
```

</td>
<td>

```kotlin
val warning = text("Watch out!") {
    color(red)
    bold()
    click { run("/duck") }
    hover { text("Quack") }
}
```

</td>
</tr>
</table>
<!-- markdownlint-enable MD033 -->

Kotventure produces the same `Component` as Adventure. It does not reimplement Adventure. It gives the call site an
idiomatic Kotlin form.

## The full picture

This example shows what occurs when a player joins an event server. It sends a themed nameplate, a typed MiniMessage
broadcast, a gradient title, a sound, and an automatic boss bar. All items are type-safe Adventure objects.

```kotlin
object EventTheme : Theme("event") {
    val accent = hex("#55FFAA")

    val header: Style by style {
        color(accent)
        bold()
    }
}

object JoinBroadcast :
    MiniTemplate("<gray>[<green>+</green>]</gray> <player> <gray>joined — <online> online</gray>") {
    val player by placeholder<Component>()
    val online by placeholder<Int>()
}

fun onJoin(joiner: Audience, everyone: Audience, name: String, onlineCount: Int, ticker: Ticker) {
    val nameplate =
        text(name) {
            style(EventTheme.header)
            hover { text("Add $name as a friend") }
            click { suggest("/friend add $name") }
        }

    everyone.message {
        append(
            JoinBroadcast {
                player bind nameplate
                online bind onlineCount
            },
        )
    }

    joiner.title {
        title { text("Sky Games") { gradient(aqua, green, gold) } }
        subtitle { text("Season 5 — Capture the Core") { color(gray) } }
        times {
            fadeIn(10.ticks)
            stay(3.seconds)
            fadeOut(10.ticks)
        }
    }

    joiner.sound(key("minecraft:ui.toast.challenge_complete")) {
        volume(0.8f)
        pitch(1.2f)
    }

    context(ticker) {
        joiner.bossBar(over = 60.seconds) {
            name { remaining -> text("Round starts in ${remaining.inWholeSeconds}s") }
            color(green)
            overlay(notched10)
            progress(from = 0f, to = 1f)
            every(1.ticks)
        }
    }
}
```

Messages are code and require tests. The `test` module supplies Kotest matchers that examine the component and not its
serialised form. The `test-snapshot` module records regressions in canonical JSON:

```kotlin
val nameplate =
    text("Alex") {
        color(aqua)
        bold()
        click { suggest("/friend add Alex") }
    }

nameplate shouldHaveColor NamedTextColor.AQUA
nameplate.shouldBeBold()
nameplate shouldHaveClickAction ClickEvent.Action.SUGGEST_COMMAND
nameplate shouldMatchSnapshot "join-nameplate"
```

Each snippet on this page is in a `src/samples` source set. CI compiles the snippets against the public API. Thus, the
README stays consistent with the library.

## Feature tour

<!-- markdownlint-disable MD033 -->
<details>
<summary><strong>Components, styles &amp; colours</strong> — keybinds, translatables, gradients, themes</summary>

```kotlin
val hint = component {
    text("Press ") { color(gray) }
    keybind("key.sneak") { color(aqua) }
    text(" to sneak past the ")
    translatable("entity.minecraft.warden") { fallback("Warden") }
}

val banner = gradientText("Sky Games", hex("#55FFFF"), hex("#FFAA00"))
```

Use `style { }` to build styles. Use `component styled heading` to apply them. You can put styles in `Theme` objects.
The compiler checks the theme properties, and you can register the objects for runtime lookup.

</details>

<details>
<summary><strong>Typed MiniMessage</strong> — parsing, placeholders, templates, validation, mini→DSL conversion</summary>

```kotlin
val motd = mini("<gradient:#55FFFF:#FFAA00>Sky Games</gradient> <gray>— Season 5</gray>")

val streak = mini("<gold><wins></gold> win streak, <player>!") {
    unparsed("player", "Alex")
    parsed("wins", "<bold>12</bold>")
}
```

Templates declare placeholders as delegated properties. The property name is the tag. The compiler finds an
incorrect `<player>` tag or an absent binding before the application sends the message. At load time,
`JoinBroadcast.validate()` reports malformed tags and incorrect placeholder sets. The
`miniToDsl("<gold>Welcome!")` function generates the equivalent Kotventure code.

</details>

<details>
<summary><strong>Selectors, scores &amp; NBT</strong> — typed vanilla selectors instead of strings</summary>

```kotlin
val champions = allPlayers {
    tag("champion")
    scores { "wins" eq atLeast(3) }
}

val scoreboardLine = component {
    selector(champions)
    text(" — ")
    score("@s", "wins")
}

val health = entityNbt(self(), nbtPath("Health"))
```

Selector scopes model the syntax that the vanilla parser accepts for each head. The compiler rejects malformed
combinations when possible. Otherwise, the operation stops immediately and identifies the incorrect argument. For
configuration strings, use the strict `parseSelector(...)` bridge. Its errors include the applicable offset.

</details>

<details>
<summary><strong>Books, boss bars, titles &amp; tab lists</strong> — one send-DSL for every audience surface</summary>

```kotlin
player.book {
    title { text("Event Guide") }
    author { text("The Architects") }
    page { text("Chapter 1 — Capture the Core") }
}

player.tabList {
    header { text("Sky Games") { color(gold) } }
    footer { text("play.example.com") }
}
```

The same form works for `message`, `actionBar`, `title`, `sound`, and `bossBar`. A `Ticker` can schedule automatic boss
bars. The API also supplies signed-chat helpers.

</details>

<details>
<summary><strong>Serialisers</strong> — legacy, JSON, plain text, MiniMessage</summary>

```kotlin
val message = text("Welcome") { color(gold) }

val json = message.toJson()
val markup = message.toMiniMessage()
val plain = message.toPlainText()

val imported = "&6Welcome".asLegacyAmpersandComponent()
```

</details>
<!-- markdownlint-enable MD033 -->

## The modules

The project adds modules only when a phase requires them. This table shows the target architecture. For more
information, refer to [`docs/DESIGN.md`](docs/DESIGN.md) and the [Roadmap](docs/ROADMAP.md).

| Module                                                 | Purpose                                                                  | Status |
|--------------------------------------------------------|--------------------------------------------------------------------------|--------|
| [`core`](modules/core)                                 | Component / style / colour DSL, selectors, NBT, themes, audience-send DSL | ✅      |
| [`minimessage`](modules/minimessage)                   | Typed MiniMessage templates, validation, MiniMessage ⇄ DSL converter     | ✅      |
| [`serializer`](modules/serializer)                     | Optional Adventure serializer extension functions                        | ✅      |
| [`test`](modules/test)                                 | Kotest component matchers                                                 | ✅      |
| [`test-snapshot`](modules/test-snapshot)               | Snapshot tests of canonical component JSON                               | ✅      |
| [`bom`](modules/bom)                                   | Bill of materials that aligns Kotventure and Adventure module versions   | ✅      |
| [`paper`](modules/paper)                               | Paper platform bundle: `Ticker` adapter over the Bukkit scheduler        | ✅      |
| `i18n`                                                 | Translation registry + per-player locale DSL                             | 🔜     |
| `ansi`                                                 | Render a `Component` as coloured terminal output                          | 🔜     |
| `coroutines`                                           | Suspend click callbacks, asynchronous sends, and animation schedules     | 🔜     |
| `velocity` / `fabric`                                  | Platform adapters and additional features                                | 🔜     |
| `ksp`                                                  | Typed message-catalogue codegen + compile-time validation                | 🔜     |
| `gradle-plugin`                                        | Validate / pre-compile MiniMessage resource bundles at build time        | 🔜     |

## Getting it

JitPack publishes each tagged release. Import the BOM one time. Then, add the modules that you require without
repeated versions:

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
    implementation("com.github.LMLiam.Kotventure:kotventure-paper")   // on Paper servers

    testImplementation("com.github.LMLiam.Kotventure:kotventure-test")
    testImplementation("com.github.LMLiam.Kotventure:kotventure-test-snapshot")
}
```

Replace `<tag>` with a [released tag](https://github.com/LMLiam/Kotventure/releases) (e.g. `0.16.0`). The `test`
artefacts are test-scope only. The BOM re-exports Adventure's BOM at the baseline pinned in
[`gradle/libs.versions.toml`](gradle/libs.versions.toml) (currently 5.2.0).

If you are new to Kotventure, use the [Getting Started guide](docs/GETTING-STARTED.md). It gives five short steps from
installation to a tested component.

## Build & compatibility

Kotventure uses the Java 25 Gradle toolchain for builds and tests. The Adventure baseline requires Java 21 or a later
version for consumers. For the release process and maintainer permissions, refer to
[`docs/RELEASING.md`](docs/RELEASING.md).

## Contributing

We welcome contributions. Read the [Contributing Guide](.github/CONTRIBUTING.md). For a simple first task, select an
issue with the [`good first issue`](https://github.com/LMLiam/Kotventure/labels/good%20first%20issue) label. To report a
vulnerability, follow the [Security Policy](.github/SECURITY.md). Do not open a public issue.

## License

The MIT License applies. Refer to [`LICENSE`](LICENSE.md).
