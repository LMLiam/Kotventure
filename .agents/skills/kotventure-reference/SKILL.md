---
name: kotventure-reference
description: >-
  Use this skill before you write Kotlin that constructs Kotventure values. It identifies the existing Kotventure
  entry point for tests, samples, documentation, and features.
---

# Kotventure surface reference

The project uses its DSL in tests, samples, and documentation. Before you call a raw `net.kyori` factory or construct a
value manually, find the existing entry point here. Assertion expected values are the one exception. They use raw
Adventure values, as specified in `writing-component-tests`.

The base package is `io.github.lmliam.kotventure.<module>`. If you do not know whether an entry point exists, search the
feature package first:
`rg -n '^public (inline )?fun' modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/<feature>`.

## Module map

| Module | What it holds | Deep dive |
|---|---|---|
| `core` | Component, style, and audience DSL with one package for each feature | [references/core-entry-points.md](references/core-entry-points.md) |
| `minimessage` | `mini(...)`, typed `MiniTemplate`s, placeholders, validation, mini→DSL conversion | the `minimessage-reference` skill |
| `serializer` | `Component` ↔ string extensions (below) | — |
| `test` | Kotest matchers for each output type | `modules/test/README.md` and the `writing-component-tests` skill |
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
| Manually written `@e[distance=..5]` strings | `entities { distance(atMost(5.0)) }` (typed selector DSL) |
