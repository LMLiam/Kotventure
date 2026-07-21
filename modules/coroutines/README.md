# `kotventure-coroutines`

This module bridges Kotlin coroutines and [`kotventure-core`](../core/README.md). Use `click(scope) { ... }` to run a
suspending body when a player clicks. The body launches into your `CoroutineScope` and runs on that scope's
dispatcher, not the click thread.

## Getting it

After you import the BOM, add this dependency. Refer to the [root README](../../README.md#getting-it).

```kotlin
dependencies {
    implementation("com.github.LMLiam.Kotventure:kotventure-coroutines")
}
```

## Suspend clicks

```kotlin
audience.message {
    text("Claim reward") {
        click(pluginScope) { clicker -> rewards.claim(clicker) }
    }
}
```

A click does nothing after the cancellation of `pluginScope`. A failure in the body goes to the scope's exception
handling. To limit the callback, set `uses` and `lifetime` in the `options` block. Unset slots keep the Adventure
defaults: one use, and a lifetime of twelve hours.

```kotlin
click(pluginScope, options = {
    uses(1)
    lifetime(10.minutes)
}) { clicker -> rewards.claim(clicker) }
```

## Context parameters

When a `CoroutineScope` is implicit, omit the argument. For example, a plugin class can implement `CoroutineScope`.
The lambda must name its clicker parameter. A lambda without a parameter selects the core click-action builder
instead.

```kotlin
class RewardsPlugin : JavaPlugin(), CoroutineScope {
    fun offer(audience: Audience) {
        audience.message {
            text("Claim reward") {
                click { clicker -> rewards.claim(clicker) }
            }
        }
    }
}
```

A function can also declare the requirement with a context parameter. Callers supply the scope implicitly, or bridge
with `with(pluginScope) { ... }`:

```kotlin
context(_: CoroutineScope)
fun offerReward(audience: Audience) {
    audience.message {
        text("Claim reward") {
            click { clicker -> rewards.claim(clicker) }
        }
    }
}
```

## Reusable events

The top-level form creates one suspend click event for many components:

```kotlin
val claim = click(pluginScope) { clicker -> rewards.claim(clicker) }

audience.message { text("[Claim]") { click(claim) } }
broadcast.message { text("[Claim]") { click(claim) } }
```

## Docs

- [Getting Started guide](../../docs/GETTING-STARTED.md)
- KDoc for each public declaration, with compiled `@sample` snippets from [`src/samples`](src/samples)
