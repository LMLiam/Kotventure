---
name: idiomatic-kotlin-dsl
description: Use when designing or reviewing DSL/builder code in KyoriAdventureDSL — concrete Kotlin idioms with do/don't examples for type-safe, scope-safe DSLs.
---

# Idiomatic Kotlin DSLs

How DSLs are built here. Favour clarity and type-safety; avoid Java-isms and cleverness.

## Lambda-with-receiver + @DslMarker

A builder scope is an interface/class configured by a `Scope.() -> Unit` lambda, marked so inner scopes can't leak into outer ones.

```kotlin
@DslMarker
internal annotation class AdventureDsl

@AdventureDsl
public interface TextScope {
    public fun content(value: String)
    public fun color(color: TextColor)
    public fun text(init: TextScope.() -> Unit)   // nested children
}

/** Builds a [TextComponent] from a DSL block. */
public fun component(init: TextScope.() -> Unit): Component =
    TextScopeImpl().apply(init).build()
```

- The `@DslMarker` stops `component { text { content("…") /* outer color() not visible */ } }` mistakes.
- Public surface is the interface; the `…Impl` is `internal`.

## Do

- `val` over `var`; immutable value types as `data class`; closed sets as `enum`/`sealed`.
- **Extension functions** for ergonomics: `public fun Audience.message(init: TextScope.() -> Unit)`.
- **Expression bodies** for one-liners; explicit return types on public API.
- `infix`/operators **only when they read like English**: `text("Hi") styled Theme.header`. Don't force them.
- Null-safety with `?:`, `?.`, `requireNotNull(x) { "…" }`. Default parameters instead of overloads.
- Small, composable functions; one level of abstraction each.

## Don't

- ❌ `!!`, hand-rolled getters/setters, manual `Builder` classes where a DSL fits.
- ❌ Wildcard imports (ktlint forbids them).
- ❌ `object Utils { … }` / `helpers/` packages — package by feature.
- ❌ Leaking mutable builder state out of the scope; return the **built** immutable Adventure object.
- ❌ Single-impl interfaces or generics added "just in case".
- ❌ Reflection / `ServiceLoader` — use the explicit `AdventureDsl` registry instead.

## Smell check before committing

- Could a reviewer understand this file's one job from its name? If not, split it.
- Does any public declaration lack a visibility modifier, return type, or KDoc? Fix it (`explicitApi()` will fail otherwise).
- Is there a builder field that escapes mutable? Make the output immutable.
