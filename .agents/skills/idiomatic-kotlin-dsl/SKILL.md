---
name: idiomatic-kotlin-dsl
description: Use when designing, planning, or reviewing any DSL or API surface in Kotventure — REQUIRED before posting an implementation plan or sketching public API. Also use when reviewing DSL code for shape problems (typed keys, string overloads, runtime require checks, registration side effects, var properties in scopes).
---

# Idiomatic Kotlin DSLs

How API surfaces are designed and built here. Two halves: **which surface to build**
(resolution ladder + pressure-test), then **how to build it** (house idioms). Run the design
half before writing any plan; the maintainer treats a plan that skipped it as rework.

## Designing the API shape

### The resolution ladder

When a caller needs to reference a named thing, prefer the highest rung that works. Each step
down trades compile-time safety for runtime flexibility — step down only when the rung above
is impossible:

1. **Plain property** — the set of names is known where it's declared. A typo or missing
   entry is a compile error; zero library machinery.
2. **Delegated property** (`by` + `PropertyDelegateProvider`) — you need a compile-checked
   property **and** the name at runtime (registry lookup, serialization, interop). The
   delegate sees `property.name` at bind time, so one declaration serves both worlds.
   Live example: `MiniTemplate.placeholder()` in
   `modules/minimessage/.../template/MiniTemplate.kt` — `val player by placeholder<Component>()`
   makes the Kotlin property name the MiniMessage tag name, so property and markup can't drift.
3. **Typed key (value/data class)** — the set is genuinely dynamic (config, user input), so
   properties are impossible. Keys carry validation raw strings don't.
4. **Raw string** — interop boundaries only (config files, cross-plugin lookup, serialized
   formats). Never the primary API for Kotlin callers.

**Never offer two rungs in parallel for the same concept.** A typed API *plus* string
overloads of every function is two ways to do everything. Pick the rung, commit, and expose a
single explicit bridge to the string world where interop demands it (e.g. the plain top-level
`parseSelector(source)` next to the typed selector factories).

### Pressure-test the sketch before writing the plan

If any of these fails, redesign — don't compensate with more API:

- **Could the compiler enforce this instead of a `require(...)`?** Every runtime
  missing-key / typo'd-name / duplicate-entry check is a compile error you failed to design for.
- **Does the headline example type-check?** Snippets in issues and `docs/DESIGN.md` §5 are
  direction, not contracts. If a sketch can't compile as written, design the API that delivers
  its *intent* — don't replicate its syntax with runtime machinery.
- **Is there exactly one obvious way per use case?** More than one call-site form for the
  same outcome needs a justification, not a shrug.
- **Does construction mutate global state?** Defining a value never registers it. When runtime
  lookup is genuinely needed, the owning feature exposes a small explicit registry
  (e.g. `ThemeRegistry` in `core/theme`) and registration is a separate explicit call. No
  process-global registries, no `ServiceLoader`/SPI/reflection — both deliberately removed.
- **Count the public declarations.** Every extra type, overload, and helper must pay rent.
  Lookup sugar can be added later; it can't be removed.
- **Did you consider the whole Kotlin toolbox?** Lambda-with-receiver builders are the
  default, but delegated properties, `object` declarations, sealed hierarchies, context
  parameters, `infix`, and operators are all in play. Choose by call-site readability, not by
  what the last feature used.

The recurring failure mode: a design assumes compile-checked access "can't work", so it grows
key types, key factories, lookup helpers, `requireX` error paths, and string overloads of all
of it. When a plan starts growing this machinery, climb back up the ladder — don't refine the
machinery's error messages.

**No design decision is locked.** Issue text, plans, and prior maintainer choices are starting
points. When a better shape exists, raise it to the maintainer with concrete call-site
examples before implementing.

## House rules (maintainer-settled; don't relitigate silently)

These were each decided explicitly in review. Follow them; if one seems wrong for your case,
ask — don't quietly deviate.

- **Required values are parameters; optional knobs go in the block** — even plain values.
  `text("Hello") { color(red) }`, not `text("Hello", color = red)`.
- **Slots in scopes are function calls, never `var` properties.** `progress(0.25f)`,
  `color(red)` — assignment syntax is not part of the DSL grammar here.
- **Reject malformed input; never normalize it away.** Setting a singleton twice in one block
  throws `IllegalStateException` naming the argument (see `OnceAssign` in `core/dsl`).
  Last-write-wins is not a convention here. Genuinely repeatable arguments accumulate in call
  order.
- **Capability scopes model what the target parser accepts**, not a name's semantic intent —
  e.g. the `@n` selector scope keeps `sort`/`limit` because vanilla treats them as overridable
  defaults there.
- **Scope-bound sugar uses stdlib-style names as scope members** — `list()` inside
  `NbtCompoundScope` beats a global `nbtList()`. Shadowing the stdlib only inside the block is
  the point. Build scope-bound operators with context parameters where they read best.
- **Plain verb-first top-level functions for parse bridges** — `parseSelector(...)`, not
  `EntitySelector.invoke(...)` or a `String` extension.
- **No `Impl`/`Base` suffixes.** A single implementation is a concrete class with an
  `internal` constructor behind a public interface only when the interface pays rent; shared
  behaviour composes via a named abstraction + interface delegation, not an abstract `Base`
  class.
- **Prefer `inline fun <reified T>`** over `Class<*>`/`KClass` parameters.
- **Use the newest Kotlin features when they express intent better** — experimental ones with
  maintainer approval. Explicit backing fields are enabled repo-wide
  (`-Xexplicit-backing-fields`): prefer `val tags: List<String>` + `field = mutableListOf()`
  over `_name` pairs.
- **No deprecated forwarders or dual APIs on redesigns.** Pre-1.0: delete the old form
  outright.

## Building the surface

A builder scope is an interface configured by a `Scope.() -> Unit` lambda, marked with the
project's `@KotventureDslMarker` (in `core/dsl`) so inner scopes can't leak into outer ones:

```kotlin
@KotventureDslMarker
public interface TitleScope { /* slots as functions */ }

/** Builds and shows a [Title] configured by [init]. */
public fun Audience.title(init: TitleScope.() -> Unit) { ... }
```

- Public surface is the interface + entry function; the builder class is a separate file and
  the implementation detail stays `internal`.
- Entry points are **extension functions** on the natural receiver (`Audience.title { }`,
  `ComponentScope.text { }`) plus a top-level form where standalone construction makes sense
  (`component { }`, `bossBar { }`).
- Every builder must produce a **real Adventure object** — wrap `net.kyori.*`, never
  re-implement it. Confirm API shapes via the `adventure-reference` skill; don't guess.
- `explicitApi()` is on: every public declaration needs explicit visibility, explicit return
  type, and KDoc (see the `documenting-public-api` skill).

## Do / Don't

**Do:** `val` over `var` · immutable `data class` value types · `sealed`/`enum` for closed
sets · expression bodies for one-liners · default parameters over overloads ·
`requireNotNull(x) { "…" }` over `!!` · small functions, one abstraction level each.

**Don't:** ❌ `!!` · ❌ wildcard imports (ktlint) · ❌ `Utils` objects or `helpers/` packages
(package by feature) · ❌ mutable builder state escaping the scope · ❌ single-impl interfaces
or generics "just in case" · ❌ Java-isms (manual getters/setters, hand-rolled `Builder`
classes where a DSL fits).

## Smell check before committing

- **Exactly one top-level class/interface/object per file**, named after it — scope + builder
  = two files; base + subtype = two files. Feature-grouped top-level *functions/vals* sharing
  a file are fine (`NamedColors.kt`). Violations are automatic rework.
- Cyclomatic complexity ≤ 10 per function; extract named predicates instead of stacking
  conditions.
- Could a reviewer get this file's one job from its name? If not, split it.
- Any public declaration missing visibility, return type, or KDoc fails `explicitApi()`.
