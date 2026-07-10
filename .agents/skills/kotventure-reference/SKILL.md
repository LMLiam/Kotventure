---
name: kotventure-reference
description: Use when writing any Kotlin that builds components, styles, audiences, selectors, NBT, sounds, books, or boss bars in this repo — tests, samples, docs, or features — to find the existing Kotventure entry point instead of a raw net.kyori call. Use before writing arrange/act code or claiming an entry point does not exist.
---

# Kotventure surface reference

The project **dogfoods its own DSL everywhere** — tests, samples, docs. Before calling a raw
`net.kyori` factory or re-building something by hand, find the existing entry point here.
(The one deliberate exception: assertion *expected values* stay raw Adventure — see
`writing-component-tests`.)

Base package: `io.github.lmliam.kotventure.<module>`. When unsure whether something exists,
search the feature package first:
`rg -n '^public (inline )?fun' modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/<feature>`.

## Module map

| Module | What it holds | Deep dive |
|---|---|---|
| `core` | The component/style/audience DSL — one feature package per concern | [references/core-entry-points.md](references/core-entry-points.md) |
| `minimessage` | `mini(...)`, typed `MiniTemplate`s, placeholders, validation, mini→DSL conversion | the `minimessage-reference` skill |
| `serializer` | `Component` ↔ string extensions (below) | — |
| `test` | Kotest matchers for every produced type | `modules/test/README.md` + the `writing-component-tests` skill |
| `test-snapshot` | `shouldMatchSnapshot` / `shouldMatchCompactedSnapshot` | `writing-component-tests` |
| `bom` | Version alignment for consumers | — |

## serializer (complete surface)

`Component.toJson()` / `String.asJsonComponent()` · `Component.toLegacyAmpersand()` /
`.toLegacySection()` / `String.asLegacyAmpersandComponent()` / `.asLegacySectionComponent()` ·
`Component.toPlainText()` · `Component.toMiniMessage()`.

## The five most-missed substitutions

| Instead of… | Use |
|---|---|
| `Audience.audience(a, b)` / `Audience.empty()` | `audienceOf(a, b)` / `emptyAudience()` |
| `Component.text("hi").color(...)` chains | `component { text("hi") { color(...) } }` or `text("hi") { ... }` |
| `Component.empty()` in arrange/act code | `emptyComponent()` |
| `BossBar.bossBar(...)` / `Book.book(...)` | `bossBar { }` / `book { }` |
| Hand-writing `@e[distance=..5]` strings | `entities { distance(atMost(5.0)) }` (typed selector DSL) |
