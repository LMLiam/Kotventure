---
name: writing-component-tests
description: Use when writing tests for components or audiences in Kotventure — Kotest patterns, the project's own component matchers, and snapshot testing.
---

# Writing component tests

Every behavioural change ships with tests. We use **Kotest**, and we **dogfood the project's own matchers** (the `test` module) so the matcher library stays good.

## Framework

- Kotest specs (`StringSpec` is the house style for unit tests). Tests run via `useJUnitPlatform()`.
- Write the test first when practical; assert on the **real Adventure object** the DSL produces.

```kotlin
class StyleDslTest : StringSpec({
    "applies colour and decorations" {
        val component = component {
            text("Hello") { color(NamedTextColor.AQUA); bold() }
        }

        component shouldHaveColor NamedTextColor.AQUA
        component shouldContainText "Hello"
        component.hasDecoration(TextDecoration.BOLD) shouldBe true
    }
})
```

## Use the project matchers

- Prefer the project's matchers (`shouldHaveColor`, `shouldContainText`, structural/child/event matchers from the `test` module) over hand-rolled assertions. If a needed matcher doesn't exist yet, add it to the `test` module (it's a deliverable in its own right — see issue #31) rather than asserting inline.
- Matchers must give **readable failure messages** (actual vs expected).

## Snapshot / golden tests

- For larger messages where regressions matter, use `shouldMatchSnapshot()` (issue #32). Snapshots serialize to a stable form (canonical JSON or MiniMessage) and live under test resources.
- Update mode must be **explicit** (flag/env var) — never silently overwrite snapshots.

## What to cover

- Happy path + nesting/children order.
- The specific edge cases named in the issue's acceptance criteria.
- For `@DslMarker` scope-safety, a compile-fail test (e.g. `// kotlinc should reject`) where relevant.

## Don't

- ❌ Assert on serialized strings when a structural matcher exists — test the component, not its rendering (except in serializer tests).
- ❌ Leave a new matcher untested; matchers get their own tests too.
- ❌ Mock Adventure types — construct real ones; they're cheap and immutable.

Run `./gradlew test` (or `build`) and make sure it's green before committing.
