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

A batteries-included, type-safe Kotlin DSL for [Adventure](https://github.com/PaperMC/adventure) — components, styles,
titles, boss bars, books, sounds, selectors, NBT, and everything else a player sees. Kotventure wraps Adventure with an
idiomatic DSL and adds the correctness tooling the space lacks: typed MiniMessage templates, component test matchers,
snapshot testing, and load-time validation.

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

Same `Component`, same Adventure underneath — Kotventure never re-implements what Adventure does. It just makes the
call site read like Kotlin.

## The full picture

One scenario, end to end: a player joins your event server. A themed nameplate with hover and click, a typed
MiniMessage broadcast, a gradient title, a sound, and a self-advancing boss bar — all type-safe, all Adventure.

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

And because messages are code, they get tests. The `test` module ships Kotest matchers that assert on the component —
not its serialized form — and `test-snapshot` pins regressions to canonical JSON:

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

Every snippet on this page lives in the repository's `src/samples` source sets and compiles in CI against the real
API — the README cannot drift from the library.

## Feature tour

<!-- markdownlint-disable MD033 -->
<details>
<summary><strong>Components, styles &amp; colors</strong> — keybinds, translatables, gradients, themes</summary>

```kotlin
val hint = component {
    text("Press ") { color(gray) }
    keybind("key.sneak") { color(aqua) }
    text(" to sneak past the ")
    translatable("entity.minecraft.warden") { fallback("Warden") }
}

val banner = gradientText("Sky Games", hex("#55FFFF"), hex("#FFAA00"))
```

Styles are first-class: build them with `style { }`, apply them with `component styled heading`, and group them into
`Theme` objects whose properties are compile-checked *and* registrable for runtime lookup.

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

Templates declare placeholders as delegated properties — the property name *is* the tag, so a typo in `<player>` or a
missing binding is caught before the message ships. `JoinBroadcast.validate()` reports malformed tags and
missing/extra placeholders at load time, and `miniToDsl("<gold>Welcome!")` generates the equivalent Kotventure code
for you.

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

Selector scopes model exactly what the vanilla parser accepts per head; malformed combinations are compile errors or
fail fast with the offending argument named. Strings from configs still work through the strict, offset-reporting
`parseSelector(...)` bridge.

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

The same shape works for `message`, `actionBar`, `title`, `sound`, `bossBar` — including self-advancing timed boss
bars scheduled on a pluggable `Ticker` — plus signed-chat helpers.

</details>

<details>
<summary><strong>Serializers</strong> — legacy, JSON, plain text, MiniMessage</summary>

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

Modules land lazily, per phase. The table is the target architecture; see [`docs/DESIGN.md`](docs/DESIGN.md) for the
full design and the [Roadmap](docs/ROADMAP.md) for sequencing.

| Module                                                 | Purpose                                                                  | Status |
|--------------------------------------------------------|--------------------------------------------------------------------------|--------|
| [`core`](modules/core)                                 | Component / style / color DSL, selectors, NBT, themes, audience-send DSL | ✅      |
| [`minimessage`](modules/minimessage)                   | Typed MiniMessage templates, validation, MiniMessage ⇄ DSL converter     | ✅      |
| [`serializer`](modules/serializer)                     | Optional Adventure serializer extension functions                        | ✅      |
| [`test`](modules/test)                                 | Kotest component matchers                                                 | ✅      |
| [`test-snapshot`](modules/test-snapshot)               | Snapshot testing over canonical component JSON                           | ✅      |
| [`bom`](modules/bom)                                   | Bill of materials for aligning Kotventure and Adventure module versions  | ✅      |
| `i18n`                                                 | Translation registry + per-player locale DSL                             | 🔜     |
| `ansi`                                                 | Render a `Component` to colored terminal output                          | 🔜     |
| `coroutines`                                           | suspend click-callbacks, async sending, animation scheduling             | 🔜     |
| `paper` / `velocity` / `fabric`                        | Platform adapters & extras                                               | 🔜     |
| `ksp`                                                  | Typed message-catalog codegen + compile-time validation                  | 🔜     |
| `gradle-plugin`                                        | Validate / pre-compile MiniMessage resource bundles at build time        | 🔜     |

## Getting it

Tagged releases are published through [JitPack](https://jitpack.io/#LMLiam/Kotventure). Import the BOM once, then
depend on the modules you need without repeating versions:

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
    testImplementation("com.github.LMLiam.Kotventure:kotventure-test-snapshot")
}
```

Replace `<tag>` with a [released tag](https://github.com/LMLiam/Kotventure/releases) (e.g. `0.16.0`). The `test`
artifacts are test-scope only. The BOM re-exports Adventure's BOM at the baseline pinned in
[`gradle/libs.versions.toml`](gradle/libs.versions.toml) (currently 5.2.0).

New here? The [Getting Started guide](docs/GETTING-STARTED.md) walks from install to a tested component in five short
steps.

## Build & compatibility

Kotventure builds and tests with the Java 25 Gradle toolchain. Its Adventure baseline sets a Java 21+ minimum
for consumers. Release process and maintainer permissions live in [`docs/RELEASING.md`](docs/RELEASING.md).

## Contributing

Contributions are welcome — read the [Contributing Guide](.github/CONTRIBUTING.md). Good entry points are issues
labelled [`good first issue`](https://github.com/LMLiam/Kotventure/labels/good%20first%20issue). For vulnerabilities,
follow the [Security Policy](.github/SECURITY.md) rather than opening a public issue.

## License

Distributed under the MIT License — see [`LICENSE`](LICENSE.md).
