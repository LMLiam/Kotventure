# `kotventure-minimessage`

This module adds typed and validated [MiniMessage](https://docs.advntr.dev/minimessage/index.html) to
[`kotventure-core`](../core/README.md). Use `mini(...)` to parse markup. Declare reusable templates with compile-checked Kotlin properties.
Validate markup during configuration load. Convert MiniMessage strings to equivalent Kotventure DSL code.

## Getting it

After you import the BOM, add this dependency. Refer to the [root README](../../README.md#getting-it).

```kotlin
dependencies {
    implementation("com.github.LMLiam.Kotventure:kotventure-minimessage")
}
```

## Parsing

```kotlin
val motd = mini("<gradient:#55FFFF:#FFAA00>Sky Games</gradient> <gray>— Season 5</gray>")

val streak = mini("<gold><wins></gold> win streak, <player>!") {
    unparsed("player", "Alex")
    parsed("wins", "<bold>12</bold>")
}
```

## Typed templates

Extend `MiniTemplate` and declare each placeholder as a delegated property. The property name is also the tag name.
This keeps the Kotlin symbol and markup together. Bind each value through its property with a compile-time check:

```kotlin
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

An unbound, duplicate, or foreign placeholder causes an immediate failure. The error identifies the placeholder.

## Validation & conversion

- `validate(markup, placeholders)` and `template.validate()` return a `ValidationResult`.
  Its diagnostics identify malformed tags, missing declared placeholders, and undeclared custom tags. Run validation during configuration load.
- `miniToDsl("<gold>Welcome <bold>back</bold>!")` emits equivalent Kotventure DSL source. Use it to move string assets to typed code.

## Docs

- [Getting Started guide](../../docs/GETTING-STARTED.md)
- KDoc for each public declaration, with compiled `@sample` snippets from [`src/samples`](src/samples)
