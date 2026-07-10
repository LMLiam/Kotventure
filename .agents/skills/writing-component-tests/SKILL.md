---
name: writing-component-tests
description: Use when writing or reviewing tests in Kotventure — component/audience/bossbar/book/sound/title assertions, selector parse tests, snapshot tests, or compile-fail tests. Also use when tempted to assert on serialized strings, hand-roll an assertion, or check an error message.
---

# Writing component tests

Every behavioural change ships with tests, written **first** when practical. We use
**Kotest** (`StringSpec` is the house style) and we **dogfood the project's own matchers** so
the `test` module stays good.

## The three dogfooding rules

1. **Assert with project matchers**, never hand-rolled checks. The full catalogue — content,
   colour, style, decorations, children, click/hover, translatable, keybind, score, selector,
   NBT, object, book, boss bar, sound, title times — is documented in
   [`modules/test/README.md`](../../../modules/test/README.md). If a matcher you need doesn't
   exist, **add it to the `test` module** (factory + infix sugar + its own test asserting both
   the match and the failure message) — a matcher is a deliverable in its own right.
2. **Arrange/act through Kotventure's own entry points** wherever an equivalent exists:
   `audienceOf(a, b)` not `Audience.audience(a, b)`, `emptyAudience()` not `Audience.empty()`,
   `component { }` / `bossBar { }` not raw builders. Check the owning feature package before
   reaching for a `net.kyori` factory.
3. **Assertion *expected values* stay raw `net.kyori`** (`shouldBe Component.empty()`,
   `Component.text("hi")`) so the DSL is verified against Adventure ground truth. Asserting
   `component {}` equals `component {}` proves nothing.

```kotlin
class StyleDslTest : StringSpec({
    "applies colour and bold" {
        val component = component { text("Hello") { color(NamedTextColor.AQUA); bold() } }

        val greeting = component.childAt(0)
        greeting shouldHaveColor NamedTextColor.AQUA
        greeting shouldHaveContent "Hello"
        greeting.shouldBeBold()
    }
})
```

## Matcher semantics that trip people up

- **Two surfaces per attribute:** infix `shouldHave…` sugar for single attributes (returns
  the receiver, so it chains), and `have…`/`contain…` `Matcher<Component>` factories that
  compose with Kotest's `and`/`or`/`shouldNot`/`invert()`. Sugar by default; factories for
  composition and negation.
- **Decorations are tri-state.** `shouldNotHaveDecoration(BOLD)` asserts the state is
  `NOT_SET` (inherits), *not* merely "not TRUE". To assert an explicit off, use
  `shouldHaveDecoration(BOLD, State.FALSE)` / `haveDecorationState(...)`.
- **`shouldBe…Component` narrows.** `component.shouldBeKeybindComponent()` returns the typed
  component (failing readably otherwise), so type-specific matchers after it are
  statically checked.
- Failure messages must state actual vs expected — that contract is part of matcher review.

## Selector tests

- **Never assert on error message text.** Failure *positions* are pinned structurally with
  the split-string matcher: `"@e[limit=" shouldFailToParseAt "0]"` asserts parsing fails
  exactly at the string boundary (`EntitySelectorParseException.offset`).
- Round-trips: `"@a[tag=alpha]".shouldBeCanonicalSelector()`; rendering:
  `selector shouldRenderAs "@e[…]"`.

## Snapshot tests (`test-snapshot` module)

For larger messages where regressions matter:

```kotlin
component shouldMatchSnapshot "welcome-banner"        // canonical pretty JSON
component shouldMatchCompactedSnapshot "join-toast"   // whitespace-flattened text nodes
```

- Snapshots live under test resources as `<name>.snapshot.json`; commit them.
- A mismatch never overwrites: record/update mode is explicit —
  `./gradlew test -Dkotventure.snapshot.update=true` (or env `SNAPSHOT_UPDATE=true`).
  Review the diff of regenerated snapshots like any other code change.
- Depend on `kotventure-test-snapshot` only where needed; it pulls serializer + Gson.

## Compile-fail tests

For `@DslMarker` scope-safety and other must-not-compile guarantees, use the
`assertDoesNotCompile(fileName, source, *expectedMessages)` helper
(`test/compilation/Assertions.kt` in the module's test sources, backed by kctfork) rather
than a comment saying "kotlinc should reject".

## What to cover

- Happy path + nesting/children order.
- Every edge case named in the issue's acceptance criteria.
- Fail-fast contracts: duplicate-singleton calls throw `IllegalStateException` — assert the
  *type* (`shouldThrow<IllegalStateException>`), not the message text.
- New matchers: both the match and the failure message.

## Don't

- ❌ Assert on serialized strings when a structural matcher exists — test the component, not
  its rendering (serializer tests excepted).
- ❌ Mock Adventure types — construct real ones; they're cheap and immutable. (MockK is
  available for genuinely behavioural seams like `Audience` receipt, `Ticker` scheduling.)
- ❌ Assert error-message wording anywhere — messages may be reworded freely; structure and
  types are the contract.

Verify with `./gradlew test` (or `build`, which adds lint + the 85% Kover line-coverage
gate) before committing.
