---
name: writing-component-tests
description: >-
  Use this skill to write or review Kotventure tests. It covers component assertions, selector tests, snapshots, and
  compile-fail tests. Also use it before you compare serialised strings or error messages.
---

# Writing component tests

Include tests with each behavioural change. Write them first when practical. Use Kotest `StringSpec` and the project's
own matchers.

## The three dogfooding rules

1. **Use project matchers.** Do not write a manual check. The complete catalogue includes content,
   colour, style, decorations, children, click/hover, translatable, keybind, score, selector,
   NBT, object, book, boss bar, sound, title times — is documented in
   [`modules/test/README.md`](../../../modules/test/README.md). If a required matcher does not exist, add it to the
   `test` module. Supply a factory, infix convenience function, and tests for the match and failure message.
2. **Use Kotventure entry points for arrange and act steps** when an equivalent exists.
   `audienceOf(a, b)` not `Audience.audience(a, b)`, `emptyAudience()` not `Audience.empty()`,
   `component { }` / `bossBar { }` not raw builders. Check the owning feature package before
   reaching for a `net.kyori` factory.
3. **Use raw `net.kyori` expected values** such as `Component.empty()` and `Component.text("hi")`. This practice tests
   the DSL against Adventure. Do not compare one DSL-built value with another DSL-built value.

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

## Matcher semantics

- **Two forms for each attribute:** Infix `shouldHave…` convenience functions test one attribute and return
  the receiver, so it chains), and `have…`/`contain…` `Matcher<Component>` factories that
  compose with Kotest's `and`, `or`, `shouldNot`, and `invert()`. Use convenience functions by default. Use factories
  for composition and negation.
- **Decorations are tri-state.** `shouldNotHaveDecoration(BOLD)` asserts the state is
  `NOT_SET` (inherits), *not* merely "not TRUE". To assert an explicit off, use
  `shouldHaveDecoration(BOLD, State.FALSE)` / `haveDecorationState(...)`.
- **`shouldBe…Component` narrows.** `component.shouldBeKeybindComponent()` returns the typed
  component (failing readably otherwise), so type-specific matchers after it are
  statically checked.
- Failure messages must state the actual and expected values. This contract is part of matcher review.

## Selector tests

- **Do not compare error message text.** Test failure positions structurally with
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

- Snapshots are in test resources as `<name>.snapshot.json`. Commit them.
- A mismatch does not overwrite a snapshot. Enable record or update mode explicitly:
  `./gradlew test -Dkotventure.snapshot.update=true` (or env `SNAPSHOT_UPDATE=true`).
  Review the diff of regenerated snapshots like other code changes.
- Depend on `kotventure-test-snapshot` only where necessary. It adds serialiser and Gson dependencies.

## Compile-fail tests

For `@DslMarker` scope-safety and other must-not-compile guarantees, use the
`assertDoesNotCompile(fileName, source, *expectedMessages)` helper
(`test/compilation/Assertions.kt` in the module's test sources, backed by kctfork) instead of a comment that says
"kotlinc should reject".

## What to cover

- Happy path + nesting/children order.
- Every edge case named in the issue's acceptance criteria.
- Fail-fast contracts: duplicate-singleton calls throw `IllegalStateException` — assert the
  *type* (`shouldThrow<IllegalStateException>`), not the message text.
- New matchers: both the match and the failure message.

## Prohibited test forms

- ❌ Do not compare serialised strings when a structural matcher exists. Test the component and not its rendered form.
  Serialiser tests are the exception.
- ❌ Do not mock Adventure types. Construct them directly because they are inexpensive and immutable. MockK is
  available for behavioural interfaces such as `Audience` receipt and `Ticker` schedules.
- ❌ Do not compare error-message wording. Structure and types are the contract.

Verify with `./gradlew test` (or `build`, which adds lint + the 85% Kover line-coverage
gate) before committing.
