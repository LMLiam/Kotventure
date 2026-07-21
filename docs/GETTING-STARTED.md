# Getting Started

Use these six steps to create, style, test, and send a component. Each example agrees with compiled code in `src/samples`.

## 1. Install

[JitPack](https://jitpack.io/#LMLiam/Kotventure) publishes tagged releases. Import the BOM once.
Then, add the necessary modules. The BOM keeps their versions consistent:

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

Replace `<tag>` with a [released tag](https://github.com/LMLiam/Kotventure/releases). Kotventure uses the Java 25 Gradle toolchain.
Your project needs Java 21 or newer because Adventure requires Java 21.

## 2. Build your first component

Use `text(...)` for one node or `component { }` for a tree. The receiver block adds styles and child components.
You do not need a builder chain or `.build()`:

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

The result is an Adventure `Component`. Give it to an applicable Adventure API.

## 3. Style it

Each text block has style slots such as `color(...)`, `bold()`, `click { }`, and `hover { }`.
Make a reusable style with `style { }`. Apply it with `style(...)` in a block or with the `styled` infix function:

```kotlin
val heading = style {
    color(gold)
    bold()
}

val title = text("Welcome") { style(heading) }
val highlighted = component { text("important") } styled heading
```

Colours are available as named values, hexadecimal values, RGB values, HSV values, and gradients:

```kotlin
val banner = gradientText("Sky Games", hex("#55FFFF"), hex("#FFAA00"))
```

Kotventure rejects malformed input. A duplicate slot in one block causes `IllegalStateException`.

## 4. Send it

Send DSLs are extensions on Adventure `Audience`. Use them for a player, console, or server.
Each surface has the same shape:

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

The same pattern applies to `actionBar`, `sound`, `bossBar`, `book`, and `tabList`.
Refer to the [core module README](../modules/core/README.md) for the map.

## 5. Add Typed MiniMessage

`mini(...)` parses MiniMessage markup. For a reusable message, declare a `MiniTemplate`.
Each placeholder is a delegated property. Thus, the compiler keeps the tag name and Kotlin symbol together:

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

Validate configuration markup during load with `validate(...)` or `template.validate()`.
Diagnostics identify malformed tags and missing or extra placeholders before the player sees the message.

## 6. Test it

Add tests for messages. The `test` module provides Kotest matchers that check the component.
The `test-snapshot` module stores complete messages as JSON snapshots:

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

Use raw Adventure values, such as `NamedTextColor.AQUA`, for expected values. Thus, assertions compare the DSL with Adventure.
Refer to the [test](../modules/test/README.md) and [test-snapshot](../modules/test-snapshot/README.md) README files.
They contain the matcher catalogue and snapshot record procedure.

## Where next

- The [README feature tour](../README.md#feature-tour) describes selectors, NBT, books, boss bars, and serialisers.
- Module READMEs: [core](../modules/core/README.md) · [minimessage](../modules/minimessage/README.md) ·
  [serializer](../modules/serializer/README.md) · [bom](../modules/bom/README.md)
- [`DESIGN.md`](DESIGN.md) describes the architecture and target DSL surface for each phase.
- [`ROADMAP.md`](ROADMAP.md) gives the phase sequence.
