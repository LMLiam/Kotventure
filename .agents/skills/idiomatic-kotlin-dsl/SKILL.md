---
name: idiomatic-kotlin-dsl
description: >-
  Use this skill to design, plan, or review a Kotventure DSL or API. You must use it before you post an implementation
  plan or propose public API. Also use it to find API-shape problems.
---

# Idiomatic Kotlin DSLs

This skill specifies how to design and build an API. First, select the API surface with the resolution ladder and the
design test. Then, apply the project idioms. Complete the design section before you write a plan.

## Designing the API shape

### The resolution ladder

When a caller refers to a named item, use the highest possible level. Each lower level gives less compile-time safety
and more runtime flexibility. Move to a lower level only when the higher level is not possible:

1. **Plain property:** The declaration site knows the set of names. The compiler finds an incorrect or absent entry.
   No library mechanism is necessary.
2. **Delegated property** (`by` + `PropertyDelegateProvider`): You need a compile-checked
   property **and** the name at runtime for registry lookup, serialisation, or interoperability. The
   delegate sees `property.name` at bind time, so one declaration serves both worlds.
   Live example: `MiniTemplate.placeholder()` in
   In `modules/minimessage/.../template/MiniTemplate.kt`, `val player by placeholder<Component>()`
   makes the Kotlin property name the MiniMessage tag name. Thus, the property and markup stay consistent.
3. **Typed key (value/data class):** The set is dynamic because it comes from configuration or user input. Thus,
   properties are not possible. Keys validate values that raw strings cannot validate.
4. **Raw string:** Use raw strings only at interoperability boundaries, such as configuration files, cross-plugin
   lookup, or serialised formats. Do not make them the primary API for Kotlin callers.

**Do not supply two levels for the same concept.** A typed API plus string overloads gives two methods for one task.
Select one level. Add one explicit string bridge only when interoperability requires it. For example, put the top-level
`parseSelector(source)` function next to the typed selector factories.

### Pressure-test the sketch before writing the plan

If the design fails one of these tests, change the design. Do not add more API to compensate:

- **Can the compiler enforce this instead of `require(...)`?** Prefer compile-time checks for absent keys, incorrect
  names, and duplicate entries.
- **Does the headline example type-check?** Snippets in issues and `docs/DESIGN.md` §5 are
  design direction, not contracts. If an example does not compile, design an API that implements its *intent*. Do not
  reproduce the syntax with runtime mechanisms.
- **Is there one clear method for each use case?** Give a technical reason if two call forms produce the same result.
- **Does construction change global state?** A value declaration does not register the value. When runtime
  lookup is genuinely needed, the owning feature exposes a small explicit registry
  such as `ThemeRegistry` in `core/theme`. Registration is a separate explicit call. No
  process-wide registries, `ServiceLoader`, SPI, or reflection.
- **Count the public declarations.** Each additional type, overload, and helper must be necessary. You can add lookup
  convenience later, but you cannot remove released API without a compatibility cost.
- **Did you consider the complete Kotlin toolset?** Lambda-with-receiver builders are the
  default, but delegated properties, `object` declarations, sealed hierarchies, context
  parameters, `infix`, and operators are all in play. Choose by call-site readability, not by
  what the last feature used.

A common failure occurs when a design assumes that compiler-checked access is not possible. The design then adds key
types, key factories, lookup helpers, `requireX` error paths, and string overloads. If this occurs, move up the
resolution ladder. Do not improve error messages for an unnecessary mechanism.

**You can review all design decisions.** Issue text, plans, and prior maintainer choices are starting points. Before
implementation, show the maintainer concrete call-site examples for a better design.

## Project rules

The maintainer approved these rules in reviews. Follow them. If a rule is not suitable for a case, ask the maintainer
before you use a different design.

- **Required values are parameters. Optional values go in the block.** This rule also applies to plain values.
  `text("Hello") { color(red) }`, not `text("Hello", color = red)`.
- **Slots in scopes are function calls and not `var` properties.** Use `progress(0.25f)` and
  `color(red)`. Assignment syntax is not part of the DSL grammar here.
- **Reject malformed input. Do not normalise it.** Setting a singleton twice in one block
  throws `IllegalStateException` naming the argument (see `OnceAssign` in `core/dsl`).
  Last-write-wins is not a convention here. Genuinely repeatable arguments accumulate in call
  order.
- **Capability scopes model what the target parser accepts**, not the semantic intent of a name.
  For example, the `@n` selector scope keeps `sort` and `limit`. Vanilla treats them as overridable defaults there.
- **Scope-bound sugar uses stdlib-style names as scope members.** `list()` inside
  `NbtCompoundScope` beats a global `nbtList()`. Shadowing the stdlib only inside the block is
  the point. Build scope-bound operators with context parameters where they read best.
- **Use plain verb-first top-level functions for parse bridges.** Use `parseSelector(...)`, not
  `EntitySelector.invoke(...)` or a `String` extension.
- **Do not use `Impl` or `Base` suffixes.** A single implementation is a concrete class. Put an `internal` constructor
  behind a public interface only when the interface is necessary. Compose shared behaviour with a named abstraction
  and interface delegation. Do not use an abstract `Base` class.
- **Prefer `inline fun <reified T>`** over `Class<*>`/`KClass` parameters.
- **Use the newest Kotlin features when they express intent better.** Experimental features require
  maintainer approval. Explicit backing fields are enabled repo-wide
  (`-Xexplicit-backing-fields`): prefer `val tags: List<String>` + `field = mutableListOf()`
  over `_name` pairs.
- **Do not keep deprecated forwarders or two APIs after a redesign.** Before version 1.0, delete the old form.

## Building the surface

A builder scope is an interface configured by a `Scope.() -> Unit` lambda, marked with the
project's `@KotventureDslMarker` in `core/dsl`. This marker prevents inner scopes from leaking into outer scopes:

```kotlin
@KotventureDslMarker
public interface TitleScope { /* slots as functions */ }

/** Builds and shows a [Title] configured by [init]. */
public fun Audience.title(init: TitleScope.() -> Unit) { ... }
```

- The public API contains the interface and entry function. Put the builder class in a separate file and keep it
  `internal`.
- Entry points are **extension functions** on the natural receiver (`Audience.title { }`,
  `ComponentScope.text { }`) plus a top-level form where standalone construction makes sense
  (`component { }`, `bossBar { }`).
- Each builder must produce an Adventure object. Wrap `net.kyori.*` and do not reimplement it. Use the
  `adventure-reference` skill to confirm API forms.
- `explicitApi()` is on: every public declaration needs explicit visibility, explicit return
  type, and KDoc (see the `documenting-public-api` skill).

## Recommended and prohibited forms

**Do:** `val` over `var` · immutable `data class` value types · `sealed`/`enum` for closed
sets · expression bodies for one-liners · default parameters over overloads ·
`requireNotNull(x) { "…" }` over `!!` · small functions, one abstraction level each.

**Do not use:** ❌ `!!` · ❌ wildcard imports (ktlint) · ❌ `Utils` objects or `helpers/` packages
(package by feature) · ❌ mutable builder state escaping the scope · ❌ single-impl interfaces
or generics "just in case" · ❌ Java-isms (manual getters/setters, hand-rolled `Builder`
classes where a DSL fits).

## Smell check before committing

- Put **exactly one top-level class, interface, or object in each file** and use its name for the file. Put a scope and
  builder in two files. Put a base and subtype in two files. Feature-related top-level functions and values can share
  a file such as `NamedColors.kt`.
- Keep cyclomatic complexity at 10 or less for each function. Extract named predicates instead of stacking
  conditions.
- Could a reviewer get this file's one job from its name? If not, split it.
- `explicitApi()` fails a public declaration that has no visibility, return type, or KDoc.
