# `kotventure-core`

The heart of Kotventure: an idiomatic Kotlin DSL over `adventure-api` for everything a player sees. Components,
styles, colors and gradients, click/hover events, titles, boss bars (including self-advancing timed bars), books,
sounds, tab lists, typed vanilla entity selectors, NBT paths and compounds, translatables, keybinds, scores, and
registrable style themes.

`core` depends only on `adventure-api` — no MiniMessage, no coroutines, no platform code — so every other module and
platform adapter can build on it.

## Getting it

With the BOM imported (see the [root README](../../README.md#getting-it)), add:

```kotlin
dependencies {
    implementation("com.github.LMLiam.Kotventure:kotventure-core")
}
```

## A taste

```kotlin
val hint = component {
    text("Press ") { color(gray) }
    keybind("key.sneak") { color(aqua) }
    text(" to sneak past the ")
    translatable("entity.minecraft.warden") { fallback("Warden") }
}

player.tabList {
    header { text("Sky Games") { color(gold) } }
    footer { text("play.example.com") }
}

val champions = allPlayers {
    tag("champion")
    scores { "wins" eq atLeast(3) }
}
```

Every builder produces a plain Adventure object (`Component`, `Style`, `BossBar`, `Book`, `Sound`, …), so the result
plugs into any Adventure-speaking API. Malformed input fails fast: setting a singleton slot twice or expressing a
combination vanilla cannot parse throws `IllegalStateException` naming the argument, rather than silently
last-write-winning.

## Package map

One feature per package under `io.github.lmliam.kotventure.core`:

| Package | Entry points |
|---------|--------------|
| `component`, `text` | `component { }`, `text(...) { }`, `emptyComponent()`, `join`, traversal |
| `style`, `color` | `style { }`, `styled`, named colors, `hex`/`rgb`/`hsv`, gradients |
| `event` | `click { }`, `hover { }` — also available inside any style block |
| `audience` | `audienceOf`, `message`, `chat`, `title`, `sound`, `bossBar`, `book`, `tabList`, … |
| `bossbar`, `book`, `sound` | `bossBar { }` (+ `bossbar.timed`), `book { }`, `sound(key) { }` |
| `selector` | `allPlayers { }`, `entities { }`, …, strict `parseSelector(...)` bridge |
| `nbt`, `objectcomponent` | `nbt { }`, `nbtPath(...)`, block/entity/storage NBT components, `display(...)` |
| `translatable`, `keybind`, `score`, `key`, `uuid` | the remaining component types and value helpers |
| `theme` | `Theme` base class with `by style { }` properties, explicit `ThemeRegistry` |
| `time` | `ticks`, `Ticker` — the scheduling seam platform adapters implement |

## Docs

- [Getting Started guide](../../docs/GETTING-STARTED.md)
- [Design & architecture](../../docs/DESIGN.md)
- KDoc on every public declaration, with compiled `@sample` snippets from [`src/samples`](src/samples)
