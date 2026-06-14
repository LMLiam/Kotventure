---
name: idiomatic-kotlin-dsl
description: Use when designing, planning, or reviewing any DSL/API surface in Kotventure — REQUIRED before writing an implementation plan. Covers how to choose the API shape (compile-time vs runtime resolution, the design pressure-test) plus concrete Kotlin idioms with do/don't examples.
---

# Idiomatic Kotlin DSLs

How DSLs are built here. Favour clarity and type-safety; avoid Java-isms and cleverness.

## Designing the API shape (read this BEFORE planning a feature)

Implementation idioms below tell you how to build a surface; this section tells you which surface to build.

### The resolution ladder

When a caller needs to reference a named thing, prefer the highest rung that works. Each step down trades compile-time
safety for runtime flexibility — only step down when the rung above is impossible:

1. **Plain property** — the set of names is known where it's declared. A typo or a missing entry is a compile error;
   zero library machinery needed.
2. **Delegated property** (`by` + `PropertyDelegateProvider`) — you need a real compile-checked property **and** the
   name at runtime (registry lookup, serialization, interop). The delegate sees `property.name` at bind time and can
   record `name → value` into a map, so one declaration serves both worlds:

   ```kotlin
   public abstract class Catalog(public val name: String) {
       private val entries = LinkedHashMap<String, Entry>()

       public fun entry(name: String): Entry? = entries[name]   // the single string bridge

       protected fun entry(init: EntryScope.() -> Unit): PropertyDelegateProvider<Catalog, ReadOnlyProperty<Catalog, Entry>> =
           PropertyDelegateProvider { thisRef, property ->
               val built = buildEntry(init)
               thisRef.entries[property.name] = built            // runtime name, recorded once
               ReadOnlyProperty { _, _ -> built }                // compile-checked property
           }
   }
   ```

3. **Typed key (value/data class)** — the set is genuinely dynamic (built from config, user input) so properties are
   impossible. Keys carry validation and type-safety that raw strings don't.
4. **Raw string** — interop boundaries only (config files, cross-plugin lookup, serialized formats). Never the primary
   API for Kotlin callers.

**Never offer two rungs in parallel for the same concept.** A typed-key API *plus* string overloads of every function
is two ways to do everything and a maintenance tax — pick the rung, commit to it, and expose a single explicit bridge
to the string world where interop demands it.

### Pressure-test the design before writing the plan

Run the sketch through these. If any fails, redesign — don't compensate with more API:

- **Could the compiler enforce this instead of a `require(...)`?** Every runtime error message you carefully write and
  test is a compile error you failed to design for. Missing-key/typo'd-name/duplicate-entry checks are the smell.
- **Does your headline example actually type-check?** Illustrative snippets in issues and `docs/DESIGN.md` are
  direction, not contracts. If a sketch can't compile as written, find the design that delivers its intent — don't
  approximate its syntax with runtime lookup machinery.
- **Is there exactly one obvious way to express each use case?** Count the call-site forms for the same outcome; more
  than one needs a justification, not a shrug.
- **Does construction mutate global state?** Defining a value and registering it are separate, explicit steps. A
  builder that silently writes to `AdventureDsl` is runtime magic, which this project rejects.
- **Are declarations co-located with use?** If callers must declare loose keys/constants in one place to use a thing
  defined in another, the design is fighting them.
- **Count the public declarations.** Compare against the smallest design that meets the acceptance criteria; every
  extra type, overload, and helper must pay rent. Lookup sugar can always be added later — it can't be removed.
- **Did you consider the whole Kotlin toolbox?** Lambda-with-receiver builders are the default here, but delegated
  properties, `object` declarations, sealed hierarchies, `infix`, and operators are all in play. Choose by call-site
  readability, not by which pattern the last feature used.

### The shape of the failure mode

The recurring anti-pattern looks like this: a design assumes compile-checked access "can't work" for arbitrary names,
so it compensates with key types, key factories, built-in key objects, lookup helper methods, `requireX` error paths,
and string overloads of all of it — a large runtime surface validating things the compiler could have enforced. When
you notice a plan growing this machinery, that is the signal to climb back up the ladder, not to refine the
machinery's error messages.

## Lambda-with-receiver + @DslMarker

A builder scope is an interface/class configured by a `Scope.() -> Unit` lambda, marked so inner scopes can't leak into
outer ones.

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
- **Delegated properties** (`by`, `PropertyDelegateProvider`) when a declaration needs to be both a compile-checked
  property and a runtime-resolvable name — see the resolution ladder above.
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
- Does any public declaration lack a visibility modifier, return type, or KDoc? Fix it (`explicitApi()` will fail
  otherwise).
- Is there a builder field that escapes mutable? Make the output immutable.
