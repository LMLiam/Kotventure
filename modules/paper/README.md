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

Folia's region schedulers are a separate, planned slice.
