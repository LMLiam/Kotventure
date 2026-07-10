# Serializers, keys, and platforms

Kotventure's `serializer` module wraps these as `Component.toJson()`, `String.asJsonComponent()`,
`toLegacyAmpersand()/toLegacySection()` + `asLegacy…Component()`, `toPlainText()`, and
`toMiniMessage()` — prefer those extensions in project code; the raw APIs below are for
implementing them.

## JSON — `adventure-text-serializer-gson`

- `GsonComponentSerializer.gson()` — `.serialize(Component)` / `.deserialize(String)`.
- Options via `GsonComponentSerializer.builder()` + `JSONOptions`
  (`net.kyori.adventure.text.serializer.json`).
- Legacy hover payloads need `NBTLegacyHoverEventSerializer`
  (`adventure-text-serializer-json-legacy-impl`) wired into the builder.

## Legacy & plain

- `LegacyComponentSerializer.legacyAmpersand()` / `.legacySection()` —
  `.serialize`/`.deserialize`; hex support via `.builder().hexColors()`.
- `PlainTextComponentSerializer.plainText()` — text-only flattening; events/decorations are
  dropped, non-text components render best-effort.

## MiniMessage

`MiniMessage.miniMessage().serialize(component)` round-trips markup; parsing and tag
resolvers are covered in the `minimessage-reference` skill.

## ANSI (future `ansi` module)

`ANSIComponentSerializer` from `adventure-text-serializer-ansi` — not yet a dependency;
add the catalog entry when the module lands.

## Keys — `net.kyori.adventure.key`

- `Key.key("namespace:value")` / `Key.key(namespace, value)` — throws `InvalidKeyException`
  on invalid characters; default namespace is `minecraft`.
- Accessors `.namespace()`, `.value()`, `.asString()`; `Keyed` marks types with a `.key()`.
- Kotventure wraps as `key(...)` in `core/key`.

## Translation

- `GlobalTranslator`, `TranslationStore`, `MiniMessageTranslationStore`;
  `TranslatableComponentRenderer` renders translatables against a locale — the base of the
  future `i18n` module.

## Platforms

- **Paper / Velocity:** Adventure is native — `Player`, `CommandSender`, and proxy sources
  implement `Audience` directly; no adapter needed.
- **Fabric:** `adventure-platform-fabric` → `FabricServerAudiences`.
- Version and artifact list: `gradle/libs.versions.toml`; `core` may only depend on
  `adventure-api` (AGENTS.md §4).
