# `kotventure-paper`

The Paper platform bundle. Paper implements Adventure natively — every `Player`, the console, and the
`Server` already *are* `Audience`s, so the whole audience-send DSL from [`core`](../core/README.md) works
out of the box. What Paper does not provide is Kotventure's clock abstraction: this module adapts the
Bukkit scheduler to [`Ticker`](../core/src/main/kotlin/io/github/lmliam/kotventure/core/time/Ticker.kt), so
managed UI such as timed boss bars runs on a real server clock.

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
        val ticker = ticker()   // Plugin.ticker(): Ticker, backed by the Bukkit scheduler

        context(ticker) {
            player.bossBar(over = 30.seconds) {
                name { remaining -> text("Meteor in ${remaining.inWholeSeconds}s") }
                progress(from = 1f, to = 0f)
                every(1.ticks)
            }
        }
    }
}
```

Work scheduled through the ticker runs on the server main thread, one task per `repeating` call, cancelled
by the server when the plugin disables.

The Bukkit scheduler only fires on whole game ticks (50 ms), so `repeating` rejects intervals it cannot
honour with an `IllegalArgumentException` instead of silently rounding: `1.seconds`, `500.milliseconds`,
and `3.ticks` are all fine; `75.milliseconds` is not. In unit tests, swap in the deterministic
`ManualTicker` from [`kotventure-test`](../test/README.md) — no scheduler, no server.

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
