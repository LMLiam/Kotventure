# Selector Prefix Negation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace selector value-wrapped negation with one uniform prefix-negation expression API.

**Architecture:** Negatable operations append an internal filter entry and return it through an opaque public
`SelectorFilterExpression`. The scoped unary `!` marks that entry negative while its builder is active; ordered filter
groups validate polarity after configuration, and the renderer remains the only wire-format boundary.

**Tech Stack:** Kotlin 2.3, Adventure API 5.1.1, Kotest, kotlin-compile-testing, Gradle

---

### Task 1: Establish the new public behavior with failing tests

**Files:**
- Modify: `modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorTest.kt`
- Modify: `modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/selector/SelectorDslTest.kt`

- [ ] Replace existing negated calls with `!type(...)`, `!typeTag(...)`, `!name(...)`, `!gamemode(...)`,
  `!team(...)`, and `!tag(...)`.
- [ ] Add compile-failure fixtures for the removed inner-value syntax, `!tag(any)`, player `!type(...)`, and an
  unscoped expression `!`.
- [ ] Add runtime cases for repeated negation, cross-selector expression use, duplicate positives, and mixed
  polarities.
- [ ] Run:

```bash
./gradlew :core:test --tests io.github.lmliam.kotventure.core.selector.EntitySelectorTest
```

Expected: RED because filter operations still return `Unit` and the prefix operator does not exist.

### Task 2: Add the expression and ordered filter-group model

**Files:**
- Create: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SelectorFilterExpression.kt`
- Create: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SelectorFilterEntry.kt`
- Create: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SelectorFilterGroup.kt`

- [ ] Add the opaque marker:

```kotlin
package io.github.lmliam.kotventure.core.selector

/**
 * A transient selector-filter expression that may be negated with `!` inside its creating selector block.
 *
 * Expressions cannot be constructed directly or reused across selector blocks.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
 */
public sealed interface SelectorFilterExpression
```

- [ ] Add an internal entry containing owner, value, polarity, and one-shot negation:

```kotlin
internal enum class SelectorFilterPolarity {
    POSITIVE,
    NEGATIVE,
}

internal class SelectorFilterEntry<T>(
    val owner: EntitySelectorBuilder,
    val argument: String,
    val value: T,
    initialPolarity: SelectorFilterPolarity,
) : SelectorFilterExpression {
    var polarity: SelectorFilterPolarity = initialPolarity
        private set

    fun negate(requester: EntitySelectorBuilder) {
        check(owner === requester) { "Selector filter expressions cannot be reused across selector blocks." }
        check(owner.isConfiguring) { "Selector filter expressions can only be negated while their selector is being configured." }
        check(polarity == SelectorFilterPolarity.POSITIVE) {
            "Selector filter expression for '$argument' is already negated."
        }
        polarity = SelectorFilterPolarity.NEGATIVE
    }
}
```

- [ ] Add a group with `EXCLUSIVE` and `REPEATABLE` policies. `add` returns an expression, `addFixed` returns `Unit`,
  and `validate` rejects more than one positive or mixed polarities for exclusive groups.

### Task 3: Replace the public selector surface

**Files:**
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/CommonEntitySelectorScope.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SelfEntitySelectorScope.kt`
- Delete: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/Excluded.kt`
- Delete: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/SelectorFilter.kt`

- [ ] Make named negatable operations return `SelectorFilterExpression`.
- [ ] Keep tag/team presence overloads returning `Unit`.
- [ ] Remove every `Excluded<T>` overload and scoped value `not()` operator.
- [ ] Remove the erased top-level `type(Excluded<Key/String>)` extensions and casts.
- [ ] Declare one scoped operator:

```kotlin
public operator fun SelectorFilterExpression.not(): Unit
```

- [ ] Update KDoc and samples to show only prefix negation.

### Task 4: Implement lifecycle, validation, and rendering

**Files:**
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorBuilder.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorFactory.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorRenderer.kt`

- [ ] Replace grouped `SelectorFilter<T>` fields and raw tag strings with `SelectorFilterGroup<T>` instances.
- [ ] Implement each named operation by validating its raw value, appending a positive entry, and returning it.
- [ ] Implement presence operations with fixed positive/negative entries and no expression result.
- [ ] Implement `SelectorFilterExpression.not()` by delegating to the internal entry with the current builder.
- [ ] Add a configuration phase closed in `finally`; validate all groups only after a successful user block.
- [ ] Render each entry in group order, applying `!` only in `EntitySelectorRenderer`.
- [ ] Run the focused selector tests and keep them GREEN.

### Task 5: Document, verify, and publish the prerequisite

**Files:**
- Modify: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/selector/SelectorSamples.kt`
- Modify: `docs/DESIGN.md`

- [ ] Update all examples and add a concise migration note from `argument(!value)` to `!argument(value)`.
- [ ] Do not edit `CHANGELOG.md`.
- [ ] Run:

```bash
./gradlew ktlintFormat
./gradlew build
git diff --check
```

Expected: all tests, lint, Dokka samples, explicit API, and coverage pass.

- [ ] Commit with `refactor(core): make selector filters prefix-negatable`.
- [ ] Push the prerequisite branch and open a draft PR linked to its dedicated issue and roadmap metadata.
