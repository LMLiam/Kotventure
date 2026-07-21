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

## Ask prompts

`ask` sends a question and waits. It resumes with the value of the option that the player clicks.

```kotlin
val kit = audience.ask {
    text("Choose a kit: ")
    option(Kit.ARCHER) { text("[Archer]") { color(green) } }
    text(" ")
    option(Kit.MAGE) { text("[Mage]") { color(aqua) } }
}

audience.message { text("Enjoy your $kit!") }
```

The block is a full component build. Each `option` appends a clickable child. Each other operation appends text around
the options. The awaiting coroutine is the scope of the call. Thus, `ask` has no `CoroutineScope` parameter.

The first click resumes the prompt. A later click does nothing. A prompt with no options fails immediately, and the
function sends nothing.

### Deadlines

`ask` has no timeout parameter. Set a deadline with `withTimeout` or `withTimeoutOrNull`:

```kotlin
val kit = withTimeoutOrNull(30.seconds) {
    audience.ask { ... }
}
```

Cancellation makes the prompt dead. Each click after cancellation does nothing.

The `lifetime` parameter sets how long the buttons stay clickable. The default is Adventure's twelve hours.

```kotlin
val kit = audience.ask(lifetime = 5.minutes) { ... }
```

### Reusable prompts

A `Prompt` is a template, not a component. `ask` runs the template one time for each audience. Therefore, a prompt that
reads `viewer` shows different options to different audiences.

```kotlin
val kitPrompt = Prompt<Kit> {
    text("Choose a kit: ")
    kits.unlocked(viewer).forEach { kit ->
        option(kit) { text("[${kit.label}]") { color(kit.color) } }
    }
}

val kit = audience.ask(kitPrompt)
```

A prompt class can carry its dependencies, and an object can hold a static prompt:

```kotlin
class ShopPrompt(shop: Shop) : Prompt<Item>(
    {
        shop.stockFor(viewer).forEach { item -> option(item) { text("[${item.name}]") } }
    },
)

object KitPrompt : Prompt<Kit>(
    {
        text("Choose a kit: ")
        option(Kit.ARCHER) { text("[Archer]") { color(green) } }
    },
)

val kit = audience.ask(KitPrompt, lifetime = 5.minutes)
```

A supertype call cannot take a trailing lambda. Therefore, the block stays inside the parentheses, and the project
formatter puts it on its own line.

Kotventure scopes use one DSL marker. Therefore, an `option` block masks `viewer`. Inside an option block, use a label
such as `this@ask.viewer`, or read the property into a local value before the block.

### Broadcasts

Each member of an audience receives the message. The first member to click claims the answer. This behaviour is correct
for a "first to click" broadcast. `ask` gives one answer, because a suspending function resumes one time.

## Docs

- [Getting Started guide](../../docs/GETTING-STARTED.md)
- KDoc for each public declaration, with compiled `@sample` snippets from [`src/samples`](src/samples)
