# `kotventure-paper`

This module provides the Paper platform bundle. Paper implements Adventure as a native API.
Each `Player`, the console, and the `Server` implement `Audience`. Thus, they can use the audience DSL from [`core`](../core/README.md).
The module also adapts Paper Folia schedulers to [`Ticker`](../core/src/main/kotlin/io/github/lmliam/kotventure/core/time/Ticker.kt).
Managed UI can use the global tick, the region that owns an entity, or a fixed region.

## Getting it

After you import the BOM, add this dependency. Refer to the [root README](../../README.md#getting-it).

```kotlin
dependencies {
    implementation("com.github.LMLiam.Kotventure:kotventure-paper")
}
```

`paper-api` has the `compileOnly` scope. The server provides it at runtime.
Thus, this module adds only its classes to your jar.

## The surface

```kotlin
class MyPlugin : JavaPlugin() {
    override fun onEnable() {
        val ui = ticker()                 // global tick   -> Server.getGlobalRegionScheduler()
        val follow = ticker(entity)       // entity-bound  -> Entity.getScheduler()
        val region = ticker(location)     // region-bound  -> Server.getRegionScheduler()

        context(ui) {
            player.bossBar(over = 30.seconds) {
                name { remaining -> text("Meteor in ${remaining.inWholeSeconds}s") }
                progress(from = 1f, to = 0f)
                every(1.ticks)
            }
        }
    }
}
```

The same code runs on Paper and Folia. Select the ticker for the dispatch target. Kotventure does not use a mode selection.
The global ticker uses the global tick context. The entity ticker follows the entity region.

The location ticker selects the region when you schedule a task. It does not follow later location changes. The server
cancels scheduled tasks when the plugin stops.

Paper schedulers operate only on complete game ticks (50 ms). `repeating` rejects an interval that it cannot honour.
It throws `IllegalArgumentException` and does not round the interval. It accepts `1.seconds`, `500.milliseconds`, and `3.ticks`.

It rejects `75.milliseconds`. The entity ticker throws `IllegalStateException` if Paper removed the entity before the call.
If Paper removes the entity after the call, Paper stops the task. In unit tests, use the deterministic `ManualTicker` from
[`kotventure-test`](../test/README.md). This test ticker does not need a scheduler or server.

`once` schedules work one time. A positive delay obeys the same whole-tick rule as `repeating`. A delay of zero, which is
the default, selects the next tick of the target.

```kotlin
ticker().once(60.ticks) { world.setStorm(true) }   // Server.getGlobalRegionScheduler().runDelayed(…, 60)
ticker().once { world.setStorm(true) }             // Server.getGlobalRegionScheduler().run(…)
```

`ownsCurrentThread` shows if the caller is already in the ticker's context. The global ticker reads
`Server.isGlobalTickThread`. The entity and the location tickers read `Server.isOwnedByCurrentRegion`. Read this property
to prevent a schedule that you do not need.

```kotlin
if (ticker().ownsCurrentThread) world.setStorm(true) else ticker().once { world.setStorm(true) }
```

To run a coroutine on the game thread, give the ticker to
[`kotventure-coroutines`](../coroutines/README.md#tick-dispatcher).

A plugin must declare `folia-supported: true` in its `plugin.yml` to load on Folia.
Kotventure does not need more runtime configuration.

## Item name & lore

The `io.github.lmliam.kotventure.paper.item` package creates item stacks. It replaces custom names or lore through Paper data components or `ItemMeta`.
Each line other than a `blank()` line has a non-italic style unless the line selects italics. This prevents the inherited
Minecraft italic style.

```kotlin
val sword = item(Material.DIAMOND_SWORD) {
    name("Excalibur") { color(gold) }
    lore {
        +"A legendary blade"
        "+5 Strength" { color(gray) }
        blank()
    }
}

sword.lore { +"Bound to soul" }             // Paper data-component path
sword.editMeta { meta -> meta.lore { +"Soulbound" } } // stable ItemMeta path
```

## Dialogs

The `io.github.lmliam.kotventure.paper.dialog` package provides typed builders for Paper
[dialogs](https://docs.papermc.io/paper/dev/dialogs). Select the dialog type at the call site with a `DialogKind` token.
The tokens are `notice`, `confirmation`, `multiAction`, `dialogList`, and `serverLinks`.
Each token puts only its capabilities in scope. Use one of these entry points:

- `dialog(kind) { … }` builds a `Dialog` value without side effects.
- `Audience.dialog(kind) { … }` builds a dialog and shows it to the audience.

```kotlin
val reward = dialog(confirmation) {
    title { text("Daily reward") }              // The title is required. Other base slots are optional.
    externalTitle { text("Rewards") }
    closeOnEscape(false)
    afterAction(wait)                           // close / none / wait
    message { text("Claim your reward?") }      // bodies and inputs accumulate in order
    inputs {
        boolean("subscribe") {
            label { text("Subscribe") }
            default()
        }
    }
    yes {                                       // confirmation's own yes/no buttons
        label { text("Claim") }
        tooltip { text("Adds it to your inventory") }
        onClick { response, audience -> audience.message { text("Claimed!") } }
    }
    no { label { text("Later") } }
}

player.dialog(notice) {
    title { text("Welcome") }
    button { label { text("Understood") } }
}
```

Each singleton slot rejects a second assignment. A missing `title` causes `IllegalStateException`.
Repeatable bodies and `inputs { … }` blocks keep call order. Each dialog scope contains only the values that Paper accepts:

- `notice { button { … } }` has an optional button.
- A `confirmation` dialog requires both `yes` and `no` buttons.
- A `multiAction` dialog accepts multiple `button` blocks. It also accepts `columns` and `exitButton`.
- A `dialogList` dialog requires `dialogs`. It also accepts `columns`, `buttonWidth`, and `exitButton`.
- A `serverLinks` dialog requires `columns` and `buttonWidth`. It also accepts `exitButton`.

Put inputs in `inputs { … }`. Available inputs are `text`, `boolean`, `range(key, range)`, and `option`.
Use a `values` block for custom Boolean values. Set the `true` and `false` receiver values in this block. Use
`format(label, ": ", value)` for a range.

Use an `options` block to add option identifiers. An option block can set `display` and `default`. The unary `+`
form adds an identifier with Paper's default display.
A button can have one action. Select `onClick`, `runCommand`, `custom`, or the core `click` DSL.

Dialogs require a Minecraft **1.21.6+** server and client. Adventure `showDialog` and `closeDialog` provide the transport.
Adventure specifies no operation for a platform or audience that does not support dialogs. Thus, `Audience.dialog { … }` does not fail there.
