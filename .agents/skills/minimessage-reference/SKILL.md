---
name: minimessage-reference
description: Use when parsing, validating, templating, or converting MiniMessage markup — the mini() DSL, typed MiniTemplate placeholders, tag resolvers, validation diagnostics, miniToDsl conversion, or the raw Adventure MiniMessage API (deserialize, TagResolver, ParsingException, custom tags).
---

# MiniMessage reference

Covers both layers: **Kotventure's typed surface** (`minimessage` module,
`io.github.lmliam.kotventure.minimessage`) and the **raw Adventure API** it wraps. Use the
typed surface in project code; drop to raw Adventure only when implementing the wrapper
itself.

## Parsing — `mini`

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

## Typed templates — `MiniTemplate`

The compile-checked way to reuse markup (resolution-ladder rung 2 — see
`idiomatic-kotlin-dsl`). Declare placeholders as delegated properties so the property name
**is** the tag name; render with `invoke` + `bind`:

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
- `placeholder<T>("explicit-name")` exists for interop/legacy markup only — prefer the
  no-arg property form so property and tag can't drift.
- Rendering fails fast (`IllegalArgumentException`): invalid template, unbound placeholder,
  foreign placeholder, or double bind.
- Duplicate placeholder declarations and invalid tag names (`[!?#]?[a-z0-9_-]+`) fail at
  declaration time.

## Validation

```kotlin
template.validate()            // ValidationResult (cached per template)
validate(markup, placeholders) // free-form check
```

`ValidationResult.Success` / `.Failure` (with `isSuccess`/`isFailure`); `Failure` carries
`MiniMessageDiagnostic`s: `MalformedTag` (message + start/end index, offsets may be
`LOCATION_UNKNOWN`), `MissingPlaceholder`, `ExtraPlaceholder`, `ValidationEngineFailure`.
Validation runs Adventure's **strict** parser under the hood.

## Conversion — `miniToDsl`

`miniToDsl("<gold><bold>Hi")` → Kotlin source for the equivalent Kotventure DSL. Notes:

- `<gradient>` expands to per-character coloured children (rendering-exact; markup not
  reconstructed).
- Selector patterns go through the strict `core` parser and emit typed selector factories
  (canonicalized); invalid patterns throw with offsets.
- Throws `IllegalArgumentException` for shapes with no DSL surface (e.g. a player head
  without a single skin source, unsupported click/data-component payloads).

## Serializing back

`Component.toMiniMessage()` — from the `serializer` module.

## Raw Adventure API — `net.kyori.adventure.text.minimessage`

- `MiniMessage.miniMessage()` — shared default instance; `.deserialize(str, TagResolver...)`,
  `.serialize(component)`.
- `MiniMessage.builder()` — `.strict(true)` (error on unknown/unclosed tags), `.tags(...)`,
  `.editTags { }`; parse failures throw `ParsingException` (has position info).
- Resolvers: `Placeholder.parsed(name, str)` / `.unparsed(name, str)` /
  `.component(name, c)`; combine with `TagResolver.resolver(...)`; custom tags implement
  `TagResolver` — `resolve(name, ArgumentQueue, Context): Tag?` plus `has(name)` (live
  example: `RecordingTagResolver` in the module's validation package).
- Artifact `adventure-text-minimessage`; also available here: `adventure-nbt`
  (`CompoundBinaryTag`, `TagStringIO`) for hover-item data components.

Verify anything not listed against the Javadoc (https://jd.advntr.dev) or dependency sources
— never guess (see `adventure-reference`).
