# `kotventure-core`

`kotventure-core` provides the primary Kotlin DSL for `adventure-api`. It includes these features:

- Components, styles, colours, and gradients.
- Click events, hover events, titles, boss bars, books, sounds, and tab lists.
- Chat pagination, typed vanilla entity selectors, NBT paths, and NBT compounds.
- Translatable components, keybinds, scores, and registrable style themes.

`core` depends only on `adventure-api`. It does not contain MiniMessage, coroutine, or platform code.
Thus, each module and platform adapter can use it.

## Getting it

After you import the BOM, add this dependency. Refer to the [root README](../../README.md#getting-it).

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

Each builder makes an Adventure object such as `Component`, `Style`, `BossBar`, `Book`, or `Sound`.
You can use the result with an Adventure API. Malformed input causes an immediate failure.
`IllegalStateException` identifies a duplicate singleton slot or a combination that vanilla cannot parse.

## Package map

Each package under `io.github.lmliam.kotventure.core` contains one feature:

| Package                                           | Entry points                                                                                   |
|---------------------------------------------------|------------------------------------------------------------------------------------------------|
| `component`, `text`                               | `component { }`, `text(...) { }`, `emptyComponent()`, `newlineComponent()`, `join`, traversal  |
| `style`, `color`                                  | `style { }`, `styled`, named colours, `hex`/`rgb`/`hsv`, gradients                             |
| `event`                                           | `click { }`, `hover { }`. These functions are also available inside any style block.           |
| `audience`                                        | `audienceOf`, `message`, `chat`, `title`, `sound`, `bossBar`, `book`, `tabList`, `paginate`, … |
| `bossbar`, `book`, `sound`                        | `bossBar { }` (+ `bossbar.timed`), `book { }`, `sound(key) { }`                                |
| `pagination`                                      | `paginate(items) { }` makes pages with clickable, self-navigating previous and next buttons.   |
| `selector`                                        | `allPlayers { }`, `entities { }`, …, strict `parseSelector(...)` bridge                        |
| `nbt`, `objectcomponent`                          | `nbt { }`, `nbtPath(...)`, block/entity/storage NBT components, `display(...)`                 |
| `translatable`, `keybind`, `score`, `key`, `uuid` | the remaining component types and value helpers                                                |
| `theme`                                           | `Theme` base class with `by style { }` properties, explicit `ThemeRegistry`                    |
| `time`                                            | `ticks` and `Ticker`. Platform adapters implement this scheduling interface.                   |

## Docs

- [Getting Started guide](../../docs/GETTING-STARTED.md)
- [Design & architecture](../../docs/DESIGN.md)
- KDoc for each public declaration, with compiled `@sample` snippets from [`src/samples`](src/samples)
