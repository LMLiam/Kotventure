# Getting Started

From zero to a tested, styled, sent component in five steps. Every snippet below mirrors code that compiles in this
repository's `src/samples` source sets against the real API.

## 1. Install

Tagged releases are published through [JitPack](https://jitpack.io/#LMLiam/Kotventure). Import the BOM once, then add
the modules you need without repeating versions:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(platform("com.github.LMLiam.Kotventure:kotventure-bom:<tag>"))

    implementation("com.github.LMLiam.Kotventure:kotventure-core")
    implementation("com.github.LMLiam.Kotventure:kotventure-minimessage")

    testImplementation("com.github.LMLiam.Kotventure:kotventure-test")
    testImplementation("com.github.LMLiam.Kotventure:kotventure-test-snapshot")
}
```

Replace `<tag>` with a [released tag](https://github.com/LMLiam/Kotventure/releases). Kotventure targets Java 21+
consumers (following its Adventure baseline).

## 2. Build your first component

Everything starts with `text(...)` for a single node or `component { }` for a tree. The trailing block styles the
node and nests children — no builder chains, no `.build()`:

```kotlin
import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.text.text

val greeting = text("Welcome!") { color(aqua) }

val hint = component {
    text("Press ") { color(gray) }
    keybind("key.sneak") { color(aqua) }
    text(" to sneak")
}
```

The result is a plain Adventure `Component` — hand it to any API that speaks Adventure.

## 3. Style it

Style slots live directly in every text block: `color(...)`, `bold()`, `click { }`, `hover { }`. Reusable styles are
built once with `style { }` and applied with `style(...)` inside a block or the infix `styled` outside one:

```kotlin
val heading = style {
    color(gold)
    bold()
}

val title = text("Welcome") { style(heading) }
val highlighted = component { text("important") } styled heading
```

Colors come as named values (`aqua`, `gold`, …), `hex("#55FFAA")`, `rgb`/`hsv`, and gradients:

```kotlin
val banner = gradientText("Sky Games", hex("#55FFFF"), hex("#FFAA00"))
```

Setting the same slot twice in one block throws `IllegalStateException` — Kotventure rejects malformed input instead
of silently keeping the last write.

## 4. Send it

Send-DSLs are extensions on Adventure's `Audience`, so they work for a player, the console, or a whole server. Each
surface has the same shape:

```kotlin
audience.message {
    text("Welcome to the server") { color(gold) }
}

audience.title {
    title { text("Welcome") { color(gold) } }
    subtitle { text("to the server") }
    times {
        fadeIn(1.ticks)
        stay(3.seconds)
        fadeOut(1.ticks)
    }
}
```

The same pattern covers `actionBar`, `sound`, `bossBar`, `book`, and `tabList` — see the
[core module README](../modules/core/README.md) for the map.

## 5. Add MiniMessage — typed

`mini(...)` parses MiniMessage markup. For reusable messages, declare a `MiniTemplate`: each placeholder is a delegated
property, so the tag name and the Kotlin symbol cannot drift, and bindings are compile-checked:

```kotlin
val motd = mini("<gradient:#55FFFF:#FFAA00>Sky Games</gradient> <gray>— Season 5</gray>")

object JoinBroadcast :
    MiniTemplate("<gray>[<green>+</green>]</gray> <player> <gray>joined — <online> online</gray>") {
    val player by placeholder<Component>()
    val online by placeholder<Int>()
}

val line = JoinBroadcast {
    player bind nameplate
    online bind 42
}
```

Validate markup from configs at load time with `validate(...)` / `template.validate()` — diagnostics name malformed
tags and missing or extra placeholders before a player ever sees the message.

## 6. Test it

Messages are code now, so they get tests. The `test` module ships Kotest matchers that assert on the component itself,
and `test-snapshot` pins whole messages to reviewable JSON snapshots:

```kotlin
val nameplate = text("Alex") {
    color(aqua)
    bold()
    click { suggest("/friend add Alex") }
}

nameplate shouldHaveColor NamedTextColor.AQUA
nameplate.shouldBeBold()
nameplate shouldHaveClickAction ClickEvent.Action.SUGGEST_COMMAND
nameplate shouldMatchSnapshot "join-nameplate"
```

Expected values stay raw Adventure (`NamedTextColor.AQUA`) so your assertions verify against Adventure ground truth.
See the [test](../modules/test/README.md) and [test-snapshot](../modules/test-snapshot/README.md) READMEs for the full
matcher catalogue and snapshot recording workflow.

## Where next

- The [README feature tour](../README.md#feature-tour) — selectors, NBT, books, boss bars, serializers.
- Module READMEs: [core](../modules/core/README.md) · [minimessage](../modules/minimessage/README.md) ·
  [serializer](../modules/serializer/README.md) · [bom](../modules/bom/README.md)
- [`DESIGN.md`](DESIGN.md) — the architecture and the target DSL surface, phase by phase.
- [`ROADMAP.md`](ROADMAP.md) — what lands when.
