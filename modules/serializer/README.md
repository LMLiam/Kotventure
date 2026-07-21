# `kotventure-serializer`

This module provides extension functions for Adventure serialisers. One call converts a `Component` to or from a wire
format.
You do not need a serialiser singleton at the call site.

## Getting it

After you import the BOM, add this dependency. Refer to the [root README](../../README.md#getting-it).

```kotlin
dependencies {
    implementation("com.github.LMLiam.Kotventure:kotventure-serializer")
}
```

## The surface

```kotlin
val message = text("Welcome") { color(gold) }

val json = message.toJson()
val markup = message.toMiniMessage()
val plain = message.toPlainText()

val imported = "&6Welcome".asLegacyAmpersandComponent()
```

| Format       | Component → String    | String → Component                                                  |
|--------------|-----------------------|---------------------------------------------------------------------|
| JSON         | `toJson()`            | `asJsonComponent()`                                                 |
| Legacy (`&`) | `toLegacyAmpersand()` | `asLegacyAmpersandComponent()`                                      |
| Legacy (`§`) | `toLegacySection()`   | `asLegacySectionComponent()`                                        |
| Plain text   | `toPlainText()`       | —                                                                   |
| MiniMessage  | `toMiniMessage()`     | `mini(...)` in [`kotventure-minimessage`](../minimessage/README.md) |

Plain-text conversion works in one direction because it removes formatting. The `minimessage` module contains MiniMessage parsing.
Thus, this module remains a small serialiser adapter.
