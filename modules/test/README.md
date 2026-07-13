# `kotventure-test`

Kotest matchers for asserting on Adventure `Component`s. The matchers test the **component** — its colour, decorations,
children, events, and structured payloads — rather than its serialized form, so message tests stay expressive and
survive rendering changes.

Every module in this repository dogfoods these matchers on its own output.

## Getting it

The module is consumed **test-scoped** by library modules. With the BOM imported (see the root README), add:

```kotlin
dependencies {
    testImplementation("com.github.LMLiam.Kotventure:kotventure-test")
}
```

## Two surfaces

Each attribute has two entry points, and you choose per call site:

1. **`shouldHave…` / `shouldBe…` sugar** — an infix or extension assertion for the common single-attribute case. It
   returns the receiver, so assertions chain.

   ```kotlin
   component shouldHaveColor NamedTextColor.AQUA
   component.shouldBeBold()
   ```

2. **`have…` / `contain…` factories** — plain Kotest `Matcher<Component>` values. Because they are ordinary matchers
   they **compose** and **negate** with Kotest's own combinators:

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

The sugar is built on the factories, so the two never disagree. Reach for the sugar by default; drop to the factory
when you want to compose several attributes or assert the negative.

## Failure messages

Every matcher reports actual vs. expected, e.g.:

```
Expected component color <NamedTextColor.BLUE>, but was <NamedTextColor.RED>.
Expected children <[two, one]>, but was <[one, two]>.
Expected keybind component, but was <TextComponentImpl>.
```

Negated assertions (`shouldNot`, `shouldNotHave…`) report the inverse (`… not to be …`).

## Catalogue

| Concern                                                                                                                    | Factories                                                                                                                             | Sugar                                                                                                                                                                                                                                       |
|----------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Content** ([`ContentMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ContentMatchers.kt))                | `containText`, `haveContent` (flatten every `TextComponent` node — not plain-text serialize / `toPlainText()`)                        | `shouldContainText`, `shouldNotContainText`, `shouldHaveContent`, `shouldNotHaveContent`                                                                                                                                                    |
| **Colour** ([`ColorMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ColorMatchers.kt))                     | `haveColor`, `haveNoColor`, `haveShadowColor`, `haveNoShadowColor`                                                                    | `shouldHaveColor`, `shouldNotHaveColor`, `shouldHaveShadowColor`, `shouldNotHaveShadowColor`                                                                                                                                                |
| **Style / font / insertion** ([`StyleMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/StyleMatchers.kt))   | `haveStyle`, `haveFont`, `haveNoFont`, `haveInsertion`, `haveNoInsertion`                                                             | `shouldHaveStyle`, `shouldHaveFont`, `shouldNotHaveFont`, `shouldHaveInsertion`, `shouldNotHaveInsertion`                                                                                                                                   |
| **Decorations** ([`DecorationMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/DecorationMatchers.kt))      | `haveDecoration`, `haveDecorationState`                                                                                               | `shouldHaveDecoration` (+ explicit state); `shouldNotHaveDecoration` = `NOT_SET` only; `shouldBeBold` / `Italic` / … (+ `shouldNotBe…` = not `TRUE`, so `FALSE` passes)                                                                   |
| **Children** ([`ChildMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ChildMatchers.kt))                   | `haveChildCount`, `haveChildren`, `containComponent`                                                                                  | `shouldHaveChildCount`, `shouldHaveNoChildren`, `shouldHaveChildren`, `shouldContainComponent`, `shouldNotContainComponent`, `childAt`                                                                                                      |
| **Click events** ([`ClickEventMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ClickEventMatchers.kt))     | `haveClickEvent`, `haveClickAction`, `haveClickTextPayload`, `haveClickIntPayload`, `haveNoClickEvent`                                | `shouldHaveClickEvent`, `shouldHaveClickAction`, `shouldHaveClickTextPayload`, `shouldHaveClickIntPayload`, `shouldNotHaveClickEvent`                                                                                                       |
| **Hover events** ([`HoverEventMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/HoverEventMatchers.kt))     | `haveHoverEvent`, `haveHoverAction`, `haveHoverText`, `haveHoverItem`, `haveHoverEntity`, `haveNoHoverEvent`                          | `shouldHaveHoverEvent`, `shouldHaveHoverAction`, `shouldHaveHoverText`, `shouldHaveHoverItem`, `shouldHaveHoverEntity`, `shouldNotHaveHoverEvent`                                                                                           |
| **Translatable** ([`TranslatableMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/TranslatableMatchers.kt)) | `haveTranslationKey`, `haveFallback`, `haveNoFallback`, `haveArgumentCount`, `haveArguments`                                          | `shouldHaveTranslationKey`, `shouldHaveFallback`, `shouldNotHaveFallback`, `shouldHaveArgumentCount`, `shouldHaveArguments`                                                                                                                 |
| **Keybind** ([`KeybindMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/KeybindMatchers.kt))                | `haveKeybind`                                                                                                                         | `shouldBeKeybindComponent`, `shouldHaveKeybind`                                                                                                                                                                                             |
| **Score** ([`ScoreMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ScoreMatchers.kt))                      | `haveScoreName`, `haveScoreObjective`                                                                                                 | `shouldBeScoreComponent`, `shouldHaveScoreName`, `shouldHaveScoreObjective`                                                                                                                                                                 |
| **Selector** ([`SelectorMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/SelectorMatchers.kt))             | `haveSelectorPattern`, `haveSelectorSeparator`, `haveNoSelectorSeparator`                                                             | `shouldBeSelectorComponent`, `shouldHaveSelectorPattern`, `shouldHaveSelectorSeparator`, `shouldNotHaveSelectorSeparator`                                                                                                                   |
| **Object** ([`ObjectComponentMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ObjectComponentMatchers.kt)) | `haveObjectContents`, `haveObjectFallback`, `haveNoObjectFallback`                                                                    | `shouldBeObjectComponent`, `shouldHaveObjectContents`, `shouldHaveObjectFallback`, `shouldNotHaveObjectFallback`                                                                                                                            |
| **NBT** ([`NbtMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/NbtMatchers.kt))                            | `haveNbtPath`, `haveInterpretState`, `haveNbtSeparator`, `haveNoNbtSeparator`, `haveBlockPos`, `haveEntitySelector`, `haveStorageKey` | `shouldBe{Block,Entity,Storage}NbtComponent`, `shouldHaveNbtPath`, `shouldInterpret`, `shouldNotInterpret`, `shouldHaveNbtSeparator`, `shouldNotHaveNbtSeparator`, `shouldHaveBlockPos`, `shouldHaveEntitySelector`, `shouldHaveStorageKey` |

The `shouldBe…Component` assertions narrow a `Component` to its concrete type (and fail with a readable message
otherwise), so the type-specific matchers that follow are statically checked.

Snapshot assertions now live in the separate [`kotventure-test-snapshot`](../test-snapshot/README.md) artifact, so
matcher-only consumers do not pull serializer and Gson runtime dependencies.

## Adding a matcher

A new attribute gets a `have…`/`contain…` factory returning `Matcher<Component>` (with both a positive and a negated
failure message) plus the thin `shouldHave…` sugar, in the feature file for that concern. Matchers are a deliverable in
their own right: give every new matcher its own test asserting both the match and the failure message.
