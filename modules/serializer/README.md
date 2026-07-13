# `kotventure-serializer`

Extension functions over Adventure's serializers, so converting a `Component` to and from wire formats is one call —
no serializer singletons at the call site.

## Getting it

With the BOM imported (see the [root README](../../README.md#getting-it)), add:

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

| Format | Component → String | String → Component |
|--------|--------------------|--------------------|
| JSON | `toJson()` | `asJsonComponent()` |
| Legacy (`&`) | `toLegacyAmpersand()` | `asLegacyAmpersandComponent()` |
| Legacy (`§`) | `toLegacySection()` | `asLegacySectionComponent()` |
| Plain text | `toPlainText()` | — |
| MiniMessage | `toMiniMessage()` | `mini(...)` in [`kotventure-minimessage`](../minimessage/README.md) |

Plain text is deliberately one-way (formatting is lost), and MiniMessage parsing lives in the `minimessage` module so
this one stays a thin serializer shim.
