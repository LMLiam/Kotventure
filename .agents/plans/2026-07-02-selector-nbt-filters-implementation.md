# Selector NBT Filters Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add repeatable structured positive and `!nbt { ... }` filters to every typed selector head.

**Architecture:** NBT uses the prerequisite `SelectorFilterExpression` model. The selector builder stores structured
`NbtCompound` values in a repeatable filter group, while `EntitySelectorRenderer` performs canonical SNBT rendering
and polarity serialization.

**Tech Stack:** Kotlin 2.3, Adventure API 5.1.1, Kotest, kotlin-compile-testing, Gradle

---

### Task 1: Add failing structured NBT tests

**Files:**
- Modify: `modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorTest.kt`
- Modify: `modules/core/src/test/kotlin/io/github/lmliam/kotventure/core/selector/SelectorDslTest.kt`
- Modify: `modules/core/src/test/kotlin/io/github/lmliam/kotventure/test/compilation/Assertions.kt`

- [ ] Add exact positive/negative order coverage using `nbt { ... }` and `!nbt { ... }`.
- [ ] Cover every selector head, nested compounds, scalars, typed arrays, homogeneous lists, escaping, and `{}`.
- [ ] Add compile failures for `nbt(String)` and an unscoped NBT filter expression.
- [ ] Add a real Adventure selector component assertion through `SelectorMatchers`.
- [ ] Run the focused tests and confirm RED because selector scopes do not expose `nbt`.

### Task 2: Add NBT to the common expression surface

**Files:**
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/CommonEntitySelectorScope.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorBuilder.kt`
- Modify: `modules/core/src/main/kotlin/io/github/lmliam/kotventure/core/selector/EntitySelectorRenderer.kt`

- [ ] Add:

```kotlin
public fun nbt(init: NbtCompoundScope.() -> Unit): SelectorFilterExpression
```

- [ ] Build the lambda immediately with `NbtCompoundBuilder` and append the structured result to a repeatable
  `SelectorFilterGroup<NbtCompound>`.
- [ ] Render each compound with the existing `renderCompound` function; apply `!` in the selector renderer.
- [ ] Run focused tests and confirm GREEN.

### Task 3: Add samples, documentation, and delivery

**Files:**
- Modify: `modules/core/src/samples/kotlin/io/github/lmliam/kotventure/core/selector/SelectorSamples.kt`
- Modify: `docs/DESIGN.md`

- [ ] Add an `@sample` demonstrating both forms.
- [ ] Update the canonical selector example with `nbt { ... }` and `!nbt { ... }`.
- [ ] Keep raw selector interop documented through `entitySelector(raw)` and leave `CHANGELOG.md` untouched.
- [ ] Run:

```bash
./gradlew ktlintFormat
./gradlew build
git diff --check
```

- [ ] Commit with `feat(core): add selector NBT filters`.
- [ ] Safely replace PR #211's obsolete remote branch with the rebuilt #198 branch, update its body and roadmap
  fields, and leave it as a draft for maintainer review.
