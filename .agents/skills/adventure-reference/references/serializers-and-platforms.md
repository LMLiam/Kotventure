# Serialisers, keys, and platforms

Kotventure's `serializer` module wraps these as `Component.toJson()`, `String.asJsonComponent()`,
`toLegacyAmpersand()/toLegacySection()` + `asLegacy…Component()`, `toPlainText()`, and
`toMiniMessage()`. Use these extensions in project code. Use the raw APIs below only to implement the extensions.

## JSON: `adventure-text-serializer-gson`

- `GsonComponentSerializer.gson()` supplies `.serialize(Component)` and `.deserialize(String)`.
- Options via `GsonComponentSerializer.builder()` + `JSONOptions`
  (`net.kyori.adventure.text.serializer.json`).
- Legacy hover payloads require `NBTLegacyHoverEventSerializer` from
  `adventure-text-serializer-json-legacy-impl` in the builder.

## Legacy & plain

- `LegacyComponentSerializer.legacyAmpersand()` and `.legacySection()` supply `.serialize` and `.deserialize`. Use
  `.builder().hexColors()` for hexadecimal colours.
- `PlainTextComponentSerializer.plainText()` produces plain text. It removes events and decorations. It gives an
  approximate representation of non-text components.

## MiniMessage

`MiniMessage.miniMessage().serialize(component)` converts markup in both directions. The `minimessage-reference` skill
describes parsing and tag resolvers.

## ANSI (future `ansi` module)

`ANSIComponentSerializer` is in `adventure-text-serializer-ansi`. It is not currently a dependency. Add the catalogue
entry when the module is added.

## Keys: `net.kyori.adventure.key`

- `Key.key("namespace:value")` and `Key.key(namespace, value)` throw `InvalidKeyException` for invalid characters. The
  default namespace is `minecraft`.
- Accessors are `.namespace()`, `.value()`, and `.asString()`. `Keyed` identifies types with a `.key()`.
- Kotventure wraps as `key(...)` in `core/key`.

## Translation

- The types are `GlobalTranslator`, `TranslationStore`, and `MiniMessageTranslationStore`.
  `TranslatableComponentRenderer` renders translatable components for a locale. These types are the basis of the
  future `i18n` module.

## Platforms

- **Paper / Velocity:** Adventure is native. `Player`, `CommandSender`, and proxy sources implement `Audience`
  directly. They do not require an adapter.
- **Fabric:** `adventure-platform-fabric` → `FabricServerAudiences`.
- `gradle/libs.versions.toml` contains the version and artefact list. The `core` module can depend only on
  `adventure-api`. Refer to section 4 of AGENTS.md.
