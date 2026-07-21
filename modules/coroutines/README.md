# `kotventure-coroutines`

This module bridges Kotlin coroutines and [`kotventure-core`](../core/README.md). Use `callback(scope) { ... }` inside a
`click { }` block to run a suspending body when a player clicks. The body launches into your `CoroutineScope` and runs on
that scope's dispatcher, not the click thread.

## Getting it

After you import the BOM, add this dependency. Refer to the [root README](../../README.md#getting-it).

```kotlin
dependencies {
    implementation("com.github.LMLiam.Kotventure:kotventure-coroutines")
}
```

## Suspend click callbacks

```kotlin
audience.message {
    text("Claim reward") {
        click { callback(pluginScope) { clicker -> rewards.claim(clicker) } }
    }
}
```

A click after `pluginScope` is cancelled does nothing. A failure in the body goes to the scope's exception handling.
Add `uses` and `lifetime`, or pass a prebuilt `ClickCallback.Options`, to limit the callback:

```kotlin
click {
    callback(pluginScope, uses = 1, lifetime = 10.minutes) { clicker -> rewards.claim(clicker) }
}
```

## Docs

- [Getting Started guide](../../docs/GETTING-STARTED.md)
- KDoc for each public declaration, with compiled `@sample` snippets from [`src/samples`](src/samples)
