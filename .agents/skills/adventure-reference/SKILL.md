---
name: adventure-reference
description: Use when wrapping or calling any net.kyori Adventure API and its exact shape must not be guessed — components, style, click/hover events, audiences, titles, sounds, boss bars, books, NBT, serializers, keys, platforms. Use before writing code against an Adventure type you have not verified this session.
---

# Adventure API reference

A map of the API this project wraps. **Never ship a call you haven't confirmed exists.**
Ground truth, in order of authority:

1. **Dependency sources** — open the class from the Gradle dependency in the IDE.
2. **Javadoc** — https://jd.advntr.dev
3. These reference files — a curated map of what Kotventure touches, not a full API listing.

The target version is whatever `adventureApi` says in `gradle/libs.versions.toml` — check it;
don't assume. Artifact coordinates are `net.kyori:adventure-*` (see the `[libraries]` section
there for exactly which artifacts each module may use — `core` gets `adventure-api` +
`adventure-nbt` only).

## Domain map

| You need… | Read |
|---|---|
| `Component` factories: text, translatable, keybind, score, selector, NBT, object contents, join/newline, iteration | [references/components.md](references/components.md) |
| `Style`, colors, `ShadowColor`, decorations + tri-state, fonts, insertion; `ClickEvent` (incl. callbacks), `HoverEvent` (text/item/entity) | [references/style-and-events.md](references/style-and-events.md) |
| `Audience` operations: messages, chat + `SignedMessage`, titles, action bar, sounds, boss bars, books, tab list | [references/audiences.md](references/audiences.md) |
| Serializers (Gson/JSON, legacy, plain), `Key`/`Keyed`, platform integration (Paper/Velocity/Fabric) | [references/serializers-and-platforms.md](references/serializers-and-platforms.md) |
| MiniMessage (parsing, tag resolvers, placeholders, strict mode) | the `minimessage-reference` skill — it covers both the Adventure API and Kotventure's typed layer |

## Rules when wrapping

- Builders must return the **real** `net.kyori` type; tests assert on it directly.
- Adventure objects are immutable — "setters" return new instances; there is nothing to
  defensively copy.
- Watch nullability at the Kotlin/Java boundary: Adventure is annotated
  (`@Nullable`/`@NotNull`), so Kotlin sees real types — trust the compiler, not memory.
