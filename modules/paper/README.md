# `kotventure-paper`

The Paper platform bundle. Paper implements Adventure natively — every `Player`, the console, and the
`Server` already *are* `Audience`s, so the whole audience-send DSL from [`core`](../core/README.md) works
out of the box. This module also adapts Paper's Folia-aware schedulers to
[`Ticker`](../core/src/main/kotlin/io/github/lmliam/kotventure/core/time/Ticker.kt), so managed UI such as
timed boss bars can run on the global tick, an entity's owning region, or a fixed region.

## Getting it

With the BOM imported (see the [root README](../../README.md#getting-it)), add:

```kotlin
dependencies {
    implementation("com.github.LMLiam.Kotventure:kotventure-paper")
}
```

`paper-api` is `compileOnly` — the server provides it at runtime, so this module adds nothing to your jar
beyond its own classes.

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

The same code runs on plain Paper and Folia; choose the ticker by dispatch target, with no mode selection in
Kotventure. The global ticker runs in the global tick context, the entity ticker follows the entity's region,
and the location ticker runs in the region containing that location. The server cancels scheduled tasks when
the plugin disables.

Paper's schedulers only fire on whole game ticks (50 ms), so `repeating` rejects intervals it cannot honour
with an `IllegalArgumentException` instead of silently rounding: `1.seconds`, `500.milliseconds`, and
`3.ticks` are all fine; `75.milliseconds` is not. If an entity has already been removed when
`repeating` is called, the entity ticker fails fast with `IllegalStateException`; if the entity is removed
mid-flight, Paper stops the task. In unit tests, swap in the deterministic `ManualTicker` from
[`kotventure-test`](../test/README.md) — no scheduler, no server.

A plugin that consumes this module must declare `folia-supported: true` in its own `plugin.yml` to load on
Folia. Kotventure needs no extra runtime setup.

## Item name & lore

The `io.github.lmliam.kotventure.paper.item` package creates item stacks and replaces custom names or lore through
either Paper's data components or the stable `ItemMeta` view. Every non-empty line is explicitly non-italic unless
the line opts into italics, avoiding Minecraft's inherited italic item-text default.

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

Paper's [dialog](https://docs.papermc.io/paper/dev/dialogs) forms get a typed builder in the
`io.github.lmliam.kotventure.paper.dialog` package. The dialog type is chosen at the call site with a
`DialogKind` token — `notice`, `confirmation`, `multiAction`, `dialogList`, or `serverLinks` — so
only that kind's capabilities are in scope. Two entry points:

- `dialog(kind) { … }` builds a `Dialog` value (construction only — no side effects).
- `Audience.dialog(kind) { … }` builds one and shows it to the audience.

```kotlin
val reward = dialog(confirmation) {
    title { text("Daily reward") }              // required; other base slots are optional
    externalTitle { text("Rewards") }
    closeOnEscape(false)
    afterAction(wait)                           // close / none / wait
    message { text("Claim your reward?") }      // bodies and inputs accumulate in order
    inputs {
        boolean("subscribe") { label { text("Subscribe") }; default() }
    }
    yes {                                       // confirmation's own yes/no buttons
        label { text("Claim") }
        tooltip { text("Adds it to your inventory") }
        onClick { response, audience -> audience.message { text("Claimed!") } }
    }
    no { label { text("Later") } }
}

player.dialog(notice) { title { text("Welcome") }; button { label { text("Understood") } } }
```

Each singleton slot rejects a second assignment and a missing `title` throws `IllegalStateException`;
repeatable bodies and `inputs { … }` blocks accumulate in call order. Each kind's scope adds only what
Paper accepts: `notice { button { … } }` (button optional), `confirmation { yes { … }; no { … } }` (both
required), `multiAction { button { … }; columns(…); exitButton { … } }`,
`dialogList { dialogs(…); columns(…); buttonWidth(…); exitButton { … } }` (dialogs required), and
`serverLinks { columns(…); buttonWidth(…); exitButton { … } }` (columns and buttonWidth required —
Paper's factory demands them). Inputs live in `inputs { … }`: `text`,
`boolean` (with `values { true("yes"); false("no") }`), `range(key, range)` (with
`format(label, ": ", value)`), and `option` (with `options { "id" { display { … }; default() }; +"id" }`).
A button chooses at most one action — `onClick { … }`, `runCommand(template)`,
`custom(key[, additions])`, or `click { … }` (reusing the core click DSL).

Dialogs require a Minecraft **1.21.6+** server and client. Adventure's `showDialog` / `closeDialog` are the
native transport; on platforms or audiences that do not support dialogs they are documented no-ops, so
`Audience.dialog { … }` silently does nothing there rather than failing.
