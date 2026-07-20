---
name: adventure-reference
description: >-
  Use this skill before you write code against an Adventure type that you have not verified in this session. It covers
  components, styles, events, audiences, titles, sounds, boss bars, books, NBT, serialisers, keys, and platforms.
---

# Adventure API reference

This document maps the Adventure API that the project wraps. **Confirm that each call exists before you release it.**
Use sources in this order:

1. **Dependency sources** — open the class from the Gradle dependency in the IDE.
2. **Javadoc** — https://jd.advntr.dev
3. These reference files. They describe the Adventure API that Kotventure uses, but not the complete API.

Read `adventureApi` in `gradle/libs.versions.toml` to get the target version. Do not assume a version. Artefact
coordinates use `net.kyori:adventure-*`. The `[libraries]` section identifies the permitted artefacts for each module.
The `core` module can use only `adventure-api`.

## Domain map

| You need… | Read |
|---|---|
| `Component` factories: text, translatable, keybind, score, selector, NBT, object contents, join/newline, iteration | [references/components.md](references/components.md) |
| `Style`, colours, `ShadowColor`, decorations, tri-state values, fonts, insertion, `ClickEvent`, and `HoverEvent` | [references/style-and-events.md](references/style-and-events.md) |
| `Audience` operations: messages, chat + `SignedMessage`, titles, action bar, sounds, boss bars, books, tab list | [references/audiences.md](references/audiences.md) |
| Serialisers (Gson/JSON, legacy, plain), `Key`/`Keyed`, and platform integration | [references/serializers-and-platforms.md](references/serializers-and-platforms.md) |
| MiniMessage (parsing, tag resolvers, placeholders, strict mode) | the `minimessage-reference` skill — it covers both the Adventure API and Kotventure's typed layer |

## Rules when wrapping

- Builders must return the applicable `net.kyori` type. Tests must examine that type directly.
- Adventure value types are immutable. Components, styles, events, titles, sounds, and books are value types. Their
  setter-like functions return new instances, so a defensive copy is not necessary. `BossBar` is a mutable, shared
  object.
- Check nullability at the Kotlin and Java boundary. Adventure uses `@Nullable` and `@NotNull`, which give Kotlin the
  applicable types. Use the compiler result and not memory.
