# `kotventure-minimessage`

Typed, validated [MiniMessage](https://docs.advntr.dev/minimessage/index.html) on top of
[`kotventure-core`](../core/README.md): parse markup with `mini(...)`, declare reusable templates whose placeholders
are compile-checked Kotlin properties, validate markup at load time, and convert existing MiniMessage strings into
equivalent Kotventure DSL code.

## Getting it

With the BOM imported (see the [root README](../../README.md#getting-it)), add:

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

Subclass `MiniTemplate` and declare each placeholder as a delegated property — the property name *is* the tag name, so
the Kotlin symbol and the markup cannot drift. Rendering binds by property, checked at compile time:

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

A placeholder that is unbound, bound twice, or foreign to the template fails immediately with the placeholder named.

## Validation & conversion

- `validate(markup, placeholders)` / `template.validate()` return a `ValidationResult` whose diagnostics report
  malformed tags, declared placeholders missing from the markup, and undeclared custom tags — run it at config load,
  not at send time.
- `miniToDsl("<gold>Welcome <bold>back</bold>!")` emits the equivalent Kotventure DSL source, for migrating string
  assets into typed code.

## Docs

- [Getting Started guide](../../docs/GETTING-STARTED.md)
- KDoc on every public declaration, with compiled `@sample` snippets from [`src/samples`](src/samples)
