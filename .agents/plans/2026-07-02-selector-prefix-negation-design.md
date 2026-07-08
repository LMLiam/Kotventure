# Selector Prefix Negation Design

## Scope and delivery

This prerequisite replaces value-wrapped selector negation such as `tag(!"hidden")` with uniform prefix negation:

```kotlin
entities {
    !type("zombie")
    !typeTag(key("minecraft", "raiders"))
    !name("Boss")
    !gamemode(survival)
    !team("spectators")
    !tag("hidden")
}
```

It ships as its own issue and PR. Issue #198 then adds structured NBT filters on top:

```kotlin
entities {
    nbt { "Health" eq 20.0f }
    !nbt { "Invisible" eq true }
}
```

The prerequisite does not add NBT filters. The #198 PR does not redesign existing scalar negation.

## Public API

Every negatable selector operation returns an opaque `SelectorFilterExpression`. The selector scope exposes one
scoped unary operator:

```kotlin
public sealed interface SelectorFilterExpression

public operator fun SelectorFilterExpression.not(): Unit
```

The following named-value operations return the expression:

- `tag(String)`
- `name(String)`
- `gamemode(GameMode)`
- `team(String)`
- `type(Key)`
- `type(String)`
- `typeTag(Key)`
- in #198, `nbt(NbtCompoundScope.() -> Unit)`

Presence operations retain `Unit`:

- `tag(any)` and `tag(none)`
- `team(any)` and `team(none)`

This makes invalid forms such as `!tag(any)` fail at compile time. Head-specific receiver capabilities continue to
make player `!type(...)` calls fail at compile time.

`Excluded<T>`, every `Excluded<T>` overload, the scoped value operators, and the top-level erased type overloads are
removed. Prefix negation becomes the only typed negation form.

## Evaluation model

Kotlin evaluates `!tag("hidden")` and `!nbt { ... }` by calling the filter operation first, then invoking `not()` on
the returned expression. Each operation therefore:

1. appends a positive internal filter entry to its owning builder;
2. returns the entry through the opaque public marker; and
3. lets the scoped `not()` mark that entry negative before the next statement executes.

The expression exposes no public state or constructors. It is a transient DSL result, not a reusable filter value.

## Ownership and lifecycle

Each internal expression entry records its owning `EntitySelectorBuilder`. Negation checks:

- the expression belongs to the current selector builder;
- the builder is still inside its configuration block; and
- the expression has not already been negated.

Cross-selector, late, and repeated negation throw `IllegalStateException` with a focused message. The builder closes
its configuration phase in `finally`, including when the user block throws, so escaped expressions cannot mutate
abandoned or rendered builders.

## Filter groups and validation

Polarity validation moves from each call to the end of selector configuration because the outer `!` runs after the
filter operation returns.

An internal `SelectorFilterGroup<T>` owns ordered entries and one policy:

- `EXCLUSIVE`: one positive value or accumulated exclusions, never both. Used by type, name, gamemode, and team.
- `REPEATABLE`: any ordered combination of positive and negative entries. Used by tags and, in #198, NBT.

Presence operations append fixed entries with their vanilla polarity but return `Unit`. For example, `team(any)`
stores the vanilla `team=!` entry, while `team(none)` stores `team=`.

Validation names the malformed vanilla argument. Duplicate positive and mixed-polarity behavior remains strict; only
the validation point moves to the end of the same factory call.

## Responsibilities

- `SelectorFilterExpression.kt`: public opaque marker only.
- `SelectorFilterEntry.kt`: one internal value, polarity, owner, and one-shot negation.
- `SelectorFilterGroup.kt`: ordered collection and post-configuration policy validation.
- `EntitySelectorBuilder.kt`: selector DSL orchestration and lifecycle.
- `EntitySelectorRenderer.kt`: wire-format ordering, quoting, and `!` rendering only.

No model renders itself. No process-global state, thread-local context, reflection, or compiler plugin is introduced.

## Tests

The prerequisite covers:

- every new prefix form and exact vanilla output;
- positive calls remaining source-ergonomic as statements;
- repeatable exclusion order;
- duplicate-positive and mixed-polarity rejection;
- compile-time player/type and presence-negation boundaries;
- unavailability of `!` outside selector scopes;
- cross-selector, late, and repeated expression rejection;
- removal of old `tag(!value)` and related forms; and
- a real selector component through `SelectorMatchers`.

Issue #198 adds all-six-head NBT coverage, positive/negative call order, nested compounds, arrays, lists, escaping,
empty compounds, raw-overload rejection, and component integration.

## Migration and downstream impact

`docs/DESIGN.md` and selector samples move to prefix syntax. Public KDoc documents the expression lifetime and links
to samples. `CHANGELOG.md` remains release-please-owned.

Open stacked PRs #212–#217 are not rebased in the prerequisite or #198 slices. Their branches must adopt prefix
syntax when they are later rebuilt on the new #198 head.
