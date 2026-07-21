---
name: minimessage-reference
description: >-
  Use this skill for MiniMessage parsing, validation, templates, tag resolvers, diagnostics, or conversion. It covers
  the Kotventure DSL and the raw Adventure API.
---

# MiniMessage reference

This skill covers the typed Kotventure API in `io.github.lmliam.kotventure.minimessage` and the raw Adventure API. Use
the typed API in project code. Use the raw Adventure API only to implement the wrapper.

## Parsing: `mini`

```kotlin
mini("<gold>Hello")                                  // Component
mini("<gold>Hello <who>") {                          // with placeholder resolvers
    parsed("who", "<bold>world")                     // markup-bearing string
    unparsed("who", "<literal>")                     // literal text
    component("who", existingComponent)              // ComponentLike
    component("who") { text("built") { } }           // Kotventure DSL block
    resolve(typedPlaceholder, value)                 // typed descriptor (below)
}
component { mini("<gray>inline chunk") }             // splice into the component DSL
```

## Typed templates: `MiniTemplate`

Use compiler-checked templates to reuse markup. This is level 2 of the resolution ladder in `idiomatic-kotlin-dsl`.
Declare placeholders as delegated properties so that the property name is the tag name. Render with `invoke` and
`bind`:

```kotlin
object Welcome : MiniTemplate("<gold>Welcome <player>, <count> new messages") {
    val player by placeholder<Component>()
    val count by placeholder<Int>()
}

val message = Welcome {
    player bind component { text("Alex") }
    count bind 3
}
```

- Supported placeholder value families: `ComponentLike`, `String`, `Number`, `Boolean`.
  String/number/boolean bind as literal text.
- Use `placeholder<T>("explicit-name")` only for interoperability or legacy markup. Prefer the no-argument property
  form so that the property and tag stay consistent.
- Rendering fails fast (`IllegalArgumentException`): invalid template, unbound placeholder,
  foreign placeholder, or double bind.
- Duplicate placeholder declarations and invalid tag names (`[!?#]?[a-z0-9_-]+`) fail at
  declaration time.

## Validation

```kotlin
template.validate()            // ValidationResult (cached per template)
validate(markup, placeholders) // free-form check
```

`ValidationResult` has `Success` and `Failure`, with `isSuccess` and `isFailure`. A `Failure` contains
`MiniMessageDiagnostic` values. These values are `MalformedTag`, `MissingPlaceholder`, `ExtraPlaceholder`, and
`ValidationEngineFailure`. A `MalformedTag` has a message and start and end indexes. An offset can be
`LOCATION_UNKNOWN`. Validation uses Adventure's **strict** parser.

## Conversion: `miniToDsl`

`miniToDsl("<gold><bold>Hi")` → Kotlin source for the equivalent Kotventure DSL. Notes:

- `<gradient>` expands to one coloured child for each character. The output renders accurately but does not reproduce
  the source markup.
- The strict `core` parser processes selector patterns and emits typed selector factories in canonical form. Invalid
  patterns cause an exception that gives the error offset.
- Throws `IllegalArgumentException` for shapes with no DSL surface. Examples are a player head
  without one skin source and unsupported click or data-component payloads.

## Serialising back

The `serializer` module supplies `Component.toMiniMessage()`.

## Raw Adventure API: `net.kyori.adventure.text.minimessage`

- `MiniMessage.miniMessage()` gives the shared default instance. It supplies `.deserialize(str, TagResolver...)` and
  `.serialize(component)`.
- `MiniMessage.builder()` supplies `.strict(true)`, `.tags(...)`, and `.editTags { }`. Strict mode rejects unknown or
  unclosed tags. Parse failures throw `ParsingException`, which contains position information.
- Resolvers: `Placeholder.parsed(name, str)` / `.unparsed(name, str)` /
  `.component(name, c)`. Combine them with `TagResolver.resolver(...)`. Custom tags implement `TagResolver` with
  `resolve(name, ArgumentQueue, Context): Tag?` and `has(name)`. `RecordingTagResolver` in the validation package is an
  example.
- The artefact is `adventure-text-minimessage`. The module can also use `adventure-nbt`
  (`CompoundBinaryTag`, `TagStringIO`) for hover-item data components.

Use the Javadoc or dependency sources to verify an item that is not listed. Do not guess. Also refer to
`adventure-reference`.
