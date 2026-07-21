# `kotventure-test`

This module provides Kotest matchers for Adventure `Component` values. The matchers check colour, decorations, children,
events, and structured payloads.
They do not check the serialised form. Thus, message tests remain clear when rendering changes.

Each module in this repository uses these matchers for its output.

## Getting it

Library modules use this module in the **test scope**. After you import the BOM, add this dependency.

```kotlin
dependencies {
    testImplementation("com.github.LMLiam.Kotventure:kotventure-test")
}
```

## Two surfaces

Each attribute has two entry points. Select one for each call site:

1. **`shouldHave…` and `shouldBe…` functions** are infix or extension assertions for one attribute.
   Each function returns its receiver. Thus, you can chain assertions.

   ```kotlin
   component shouldHaveColor NamedTextColor.AQUA
   component.shouldBeBold()
   ```

2. **`have…` and `contain…` factories** return Kotest `Matcher<Component>` values.
   Compose or negate them with Kotest combinators:

   ```kotlin
   import io.kotest.matchers.and
   import io.kotest.matchers.or
   import io.kotest.matchers.should
   import io.kotest.matchers.shouldNot

   component should (haveColor(NamedTextColor.RED) and haveDecoration(TextDecoration.BOLD))
   component should (haveColor(NamedTextColor.RED) or haveColor(NamedTextColor.BLUE))
   component shouldNot haveColor(NamedTextColor.BLUE)
   component should haveColor(NamedTextColor.BLUE).invert()
   ```

The assertion functions use the matcher factories. Thus, the two surfaces have the same behaviour.
Use an assertion function for one attribute. Use a factory to compose attributes or check a negative condition.

## Failure messages

Each matcher reports the actual and expected values, for example:

```
Expected component color <NamedTextColor.BLUE>, but was <NamedTextColor.RED>.
Expected children <[two, one]>, but was <[one, two]>.
Expected keybind component, but was <TextComponentImpl>.
```

Negated assertions (`shouldNot`, `shouldNotHave…`) report the inverse condition (`… not to be …`).

## Catalogue

| Concern                                                                                                                    | Factories                                                                                                                             | Sugar                                                                                                                                                                                                                                       |
|----------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Content** ([`ContentMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ContentMatchers.kt))                | `containText`, `haveContent` (flatten each `TextComponent` node, without plain-text serialisation or `toPlainText()`)                 | `shouldContainText`, `shouldNotContainText`, `shouldHaveContent`, `shouldNotHaveContent`                                                                                                                                                    |
| **Colour** ([`ColorMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ColorMatchers.kt))                     | `haveColor`, `haveNoColor`, `haveShadowColor`, `haveNoShadowColor`                                                                    | `shouldHaveColor`, `shouldNotHaveColor`, `shouldHaveShadowColor`, `shouldNotHaveShadowColor`                                                                                                                                                |
| **Style / font / insertion** ([`StyleMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/StyleMatchers.kt))   | `haveStyle`, `haveFont`, `haveNoFont`, `haveInsertion`, `haveNoInsertion`                                                             | `shouldHaveStyle`, `shouldHaveFont`, `shouldNotHaveFont`, `shouldHaveInsertion`, `shouldNotHaveInsertion`                                                                                                                                   |
| **Decorations** ([`DecorationMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/DecorationMatchers.kt))      | `haveDecoration`, `haveDecorationState`                                                                                               | `shouldHaveDecoration` (+ explicit state). `shouldNotHaveDecoration` = `NOT_SET` only. `shouldBeBold` / `Italic` / … (+ `shouldNotBe…` = not `TRUE`, so `FALSE` passes)                                                                     |
| **Children** ([`ChildMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ChildMatchers.kt))                   | `haveChildCount`, `haveChildren`, `containComponent`                                                                                  | `shouldHaveChildCount`, `shouldHaveNoChildren`, `shouldHaveChildren`, `shouldContainComponent`, `shouldNotContainComponent`, `childAt`                                                                                                      |
| **Click events** ([`ClickEventMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ClickEventMatchers.kt))     | `haveClickEvent`, `haveClickAction`, `haveClickTextPayload`, `haveClickIntPayload`, `haveNoClickEvent`                                | `shouldHaveClickEvent`, `shouldHaveClickAction`, `shouldHaveClickTextPayload`, `shouldHaveClickIntPayload`, `shouldNotHaveClickEvent`                                                                                                       |
| **Hover events** ([`HoverEventMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/HoverEventMatchers.kt))     | `haveHoverEvent`, `haveHoverAction`, `haveHoverText`, `haveHoverItem`, `haveHoverEntity`, `haveNoHoverEvent`                          | `shouldHaveHoverEvent`, `shouldHaveHoverAction`, `shouldHaveHoverText`, `shouldHaveHoverItem`, `shouldHaveHoverEntity`, `shouldNotHaveHoverEvent`                                                                                           |
| **Translatable** ([`TranslatableMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/TranslatableMatchers.kt)) | `haveTranslationKey`, `haveFallback`, `haveNoFallback`, `haveArgumentCount`, `haveArguments`                                          | `shouldHaveTranslationKey`, `shouldHaveFallback`, `shouldNotHaveFallback`, `shouldHaveArgumentCount`, `shouldHaveArguments`                                                                                                                 |
| **Keybind** ([`KeybindMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/KeybindMatchers.kt))                | `haveKeybind`                                                                                                                         | `shouldBeKeybindComponent`, `shouldHaveKeybind`                                                                                                                                                                                             |
| **Score** ([`ScoreMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ScoreMatchers.kt))                      | `haveScoreName`, `haveScoreObjective`                                                                                                 | `shouldBeScoreComponent`, `shouldHaveScoreName`, `shouldHaveScoreObjective`                                                                                                                                                                 |
| **Selector** ([`SelectorMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/SelectorMatchers.kt))             | `haveSelectorPattern`, `haveSelectorSeparator`, `haveNoSelectorSeparator`                                                             | `shouldBeSelectorComponent`, `shouldHaveSelectorPattern`, `shouldHaveSelectorSeparator`, `shouldNotHaveSelectorSeparator`                                                                                                                   |
| **Object** ([`ObjectComponentMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ObjectComponentMatchers.kt)) | `haveObjectContents`, `haveObjectFallback`, `haveNoObjectFallback`                                                                    | `shouldBeObjectComponent`, `shouldHaveObjectContents`, `shouldHaveObjectFallback`, `shouldNotHaveObjectFallback`                                                                                                                            |
| **NBT** ([`NbtMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/NbtMatchers.kt))                            | `haveNbtPath`, `haveInterpretState`, `haveNbtSeparator`, `haveNoNbtSeparator`, `haveBlockPos`, `haveEntitySelector`, `haveStorageKey` | `shouldBe{Block,Entity,Storage}NbtComponent`, `shouldHaveNbtPath`, `shouldInterpret`, `shouldNotInterpret`, `shouldHaveNbtSeparator`, `shouldNotHaveNbtSeparator`, `shouldHaveBlockPos`, `shouldHaveEntitySelector`, `shouldHaveStorageKey` |

The `shouldBe…Component` assertions return the concrete component type. An incorrect type causes a clear failure
message.
The compiler then checks the type-specific matchers.

The separate [`kotventure-test-snapshot`](../test-snapshot/README.md) artefact contains snapshot assertions.
Thus, matcher-only users do not need serializer and Gson runtime dependencies.

## Adding a matcher

For a new attribute, add a `have…` or `contain…` factory to its feature file. Return `Matcher<Component>`.
Add positive and negated failure messages. Also add the applicable `shouldHave…` assertion function.
Give each new matcher a test for the match and both failure messages.
