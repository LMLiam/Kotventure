---
name: kyori-adventure-reference
description: Use when you need a Kyori Adventure API and must not guess its shape — a map of components, style, events, serializers, audiences, MiniMessage, pagination and platforms with the real net.kyori types.
---

# Kyori Adventure API reference

A map of the API this project wraps. **Verify exact signatures against the Javadoc (https://jd.advntr.dev) or the dependency sources before use — do not invent methods.** Target is `adventure-api` 4.24+.

## Components — `net.kyori.adventure.text`
- `Component.text("...")` → `TextComponent.Builder` (`.content`, `.color`, `.decoration(TextDecoration, boolean)`, `.append(Component)`, `.clickEvent`, `.hoverEvent`).
- `Component.translatable(key, fallback, args)` → `TranslatableComponent`.
- `Component.keybind(key)`, `Component.score(name, objective)`, `Component.selector(pattern)`.
- NBT: `Component.blockNBT()`, `entityNBT()`, `storageNBT()` (path, `interpret`, separator, source).
- Join: `Component.join(JoinConfiguration, components)`; `JoinConfiguration.separator(...)`, `.noSeparators()`.

## Style — `net.kyori.adventure.text.format`
- `Style.style { }`; `TextColor.color(rgb)` / `TextColor.fromHexString("#..")`; `NamedTextColor.*`.
- `TextDecoration` (BOLD, ITALIC, UNDERLINED, STRIKETHROUGH, OBFUSCATED) with tri-state `TextDecoration.State`.
- Gradients: interpolate with `TextColor.lerp(t, a, b)`.
- `font(Key)`, `.insertion(String)`.

## Events — `net.kyori.adventure.text.event`
- `ClickEvent.openUrl/openFile/runCommand/suggestCommand/changePage/copyToClipboard`.
- `ClickEvent.callback(ClickCallback)` (server-side callback, supports options: uses, lifetime).
- `HoverEvent.showText(Component)`, `showItem(ShowItem)`, `showEntity(ShowEntity)`.

## Serializers
- MiniMessage: `MiniMessage.miniMessage().deserialize(str, TagResolver...)` / `.serialize(component)` — `adventure-text-minimessage`.
- `GsonComponentSerializer.gson()` (JSON), `LegacyComponentSerializer.legacyAmpersand()/legacySection()`, `PlainTextComponentSerializer.plainText()`.
- ANSI: `ANSIComponentSerializer` — `adventure-text-serializer-ansi` (build the `ansi` module on this).

## MiniMessage tags/resolvers — `net.kyori.adventure.text.minimessage`
- `TagResolver`, `Placeholder.parsed/unparsed/component`, `TagResolver.resolver(...)`.
- Builder/strict mode + `ParsingException` for validation work.

## Audiences — `net.kyori.adventure.audience.Audience`
- `sendMessage`, `sendActionBar`, `showTitle(Title)`, `clearTitle`.
- `Title.title(main, subtitle, Title.Times.times(Duration, Duration, Duration))`.
- `playSound(Sound)` / `stopSound`; `Sound.sound(key, source, volume, pitch)`, `Sound.Emitter`.
- `openBook(Book)`; `Book.book(title, author, pages)`.
- `showBossBar(BossBar)` / `hideBossBar`; `BossBar.bossBar(name, progress, color, overlay)`.
- `sendPlayerListHeaderAndFooter(header, footer)`.

## Extra feature modules
- Pagination: `net.kyori.adventure.text.feature.pagination.Pagination` — `adventure-text-feature-pagination`.
- Translation: `GlobalTranslator`, `TranslationStore` / `MiniMessageTranslationStore`, `TranslationRegistry`.

## Platforms
- **Paper / Velocity:** Adventure is native — `Player`/`CommandSender`/proxy sources implement `Audience` directly.
- **Fabric:** `adventure-platform-fabric` → `FabricServerAudiences`.
- `Key` (`net.kyori.adventure.key.Key`) for namespaced ids; `Keyed`/`Pointered` for identity & locale.

When in doubt: open the relevant module's sources from the Gradle dependency, or the Javadoc — never ship an API call you haven't confirmed exists.
