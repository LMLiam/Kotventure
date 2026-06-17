# `kotventure-test`

Kotest matchers for asserting on Adventure `Component`s. The matchers test the **component** — its colour, decorations,
children, events, and structured payloads — rather than its serialized form, so message tests stay expressive and
survive rendering changes.

Every module in this repository dogfoods these matchers on its own output.

## Getting it

The module is consumed **test-scoped** by library modules. With the BOM imported (see the root README), add:

```kotlin
dependencies {
    testImplementation("io.github.lmliam:kotventure-test")
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

| Concern | Factories | Sugar |
| --- | --- | --- |
| **Content** ([`ContentMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ContentMatchers.kt)) | `containText`, `haveContent` | `shouldContainText`, `shouldNotContainText`, `shouldHaveContent`, `shouldNotHaveContent` |
| **Colour** ([`ColorMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ColorMatchers.kt)) | `haveColor`, `haveNoColor`, `haveShadowColor`, `haveNoShadowColor` | `shouldHaveColor`, `shouldNotHaveColor`, `shouldHaveShadowColor`, `shouldNotHaveShadowColor` |
| **Style / font / insertion** ([`StyleMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/StyleMatchers.kt)) | `haveStyle`, `haveFont`, `haveNoFont`, `haveInsertion`, `haveNoInsertion` | `shouldHaveStyle`, `shouldHaveFont`, `shouldNotHaveFont`, `shouldHaveInsertion`, `shouldNotHaveInsertion` |
| **Decorations** ([`DecorationMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/DecorationMatchers.kt)) | `haveDecoration`, `haveDecorationState` | `shouldHaveDecoration` (+ explicit state), `shouldNotHaveDecoration`, `shouldBeBold` / `Italic` / `Underlined` / `Strikethrough` / `Obfuscated` (+ `shouldNotBe…`) |
| **Children** ([`ChildMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ChildMatchers.kt)) | `haveChildCount`, `haveChildren`, `containComponent` | `shouldHaveChildCount`, `shouldHaveNoChildren`, `shouldHaveChildren`, `shouldContainComponent`, `shouldNotContainComponent`, `childAt` |
| **Click events** ([`ClickEventMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ClickEventMatchers.kt)) | `haveClickEvent`, `haveClickAction`, `haveClickTextPayload`, `haveClickIntPayload`, `haveNoClickEvent` | `shouldHaveClickEvent`, `shouldHaveClickAction`, `shouldHaveClickTextPayload`, `shouldHaveClickIntPayload`, `shouldNotHaveClickEvent` |
| **Hover events** ([`HoverEventMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/HoverEventMatchers.kt)) | `haveHoverEvent`, `haveHoverAction`, `haveHoverText`, `haveHoverItem`, `haveHoverEntity`, `haveNoHoverEvent` | `shouldHaveHoverEvent`, `shouldHaveHoverAction`, `shouldHaveHoverText`, `shouldHaveHoverItem`, `shouldHaveHoverEntity`, `shouldNotHaveHoverEvent` |
| **Translatable** ([`TranslatableMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/TranslatableMatchers.kt)) | `haveTranslationKey`, `haveFallback`, `haveNoFallback`, `haveArgumentCount`, `haveArguments` | `shouldHaveTranslationKey`, `shouldHaveFallback`, `shouldNotHaveFallback`, `shouldHaveArgumentCount`, `shouldHaveArguments` |
| **Keybind** ([`KeybindMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/KeybindMatchers.kt)) | `haveKeybind` | `shouldBeKeybindComponent`, `shouldHaveKeybind` |
| **Score** ([`ScoreMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ScoreMatchers.kt)) | `haveScoreName`, `haveScoreObjective` | `shouldBeScoreComponent`, `shouldHaveScoreName`, `shouldHaveScoreObjective` |
| **Selector** ([`SelectorMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/SelectorMatchers.kt)) | `haveSelectorPattern`, `haveSelectorSeparator`, `haveNoSelectorSeparator` | `shouldBeSelectorComponent`, `shouldHaveSelectorPattern`, `shouldHaveSelectorSeparator`, `shouldNotHaveSelectorSeparator` |
| **Object** ([`ObjectComponentMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/ObjectComponentMatchers.kt)) | `haveObjectContents`, `haveObjectFallback`, `haveNoObjectFallback` | `shouldBeObjectComponent`, `shouldHaveObjectContents`, `shouldHaveObjectFallback`, `shouldNotHaveObjectFallback` |
| **NBT** ([`NbtMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/text/NbtMatchers.kt)) | `haveNbtPath`, `haveInterpretState`, `haveNbtSeparator`, `haveNoNbtSeparator`, `haveBlockPos`, `haveEntitySelector`, `haveStorageKey` | `shouldBe{Block,Entity,Storage}NbtComponent`, `shouldHaveNbtPath`, `shouldInterpret`, `shouldNotInterpret`, `shouldHaveNbtSeparator`, `shouldNotHaveNbtSeparator`, `shouldHaveBlockPos`, `shouldHaveEntitySelector`, `shouldHaveStorageKey` |
| **Snapshot** ([`SnapshotMatchers`](src/main/kotlin/io/github/lmliam/kotventure/test/snapshot/SnapshotMatchers.kt)) | `matchSnapshot` | `shouldMatchSnapshot` |

The `shouldBe…Component` assertions narrow a `Component` to its concrete type (and fail with a readable message
otherwise), so the type-specific matchers that follow are statically checked.

## Snapshot testing

Structural matchers assert one attribute at a time; **snapshots** capture a whole message's serialized output and diff
it against a committed golden file, so a regression anywhere in a large message fails CI. Reach for a snapshot when the
*entire* rendered output is the thing you don't want to change by accident, and for a structural matcher when a single
attribute is — they complement rather than replace each other.

```kotlin
import io.github.lmliam.kotventure.test.snapshot.shouldMatchSnapshot

component shouldMatchSnapshot "welcome-message"
```

The component is [`compacted`](../core/src/main/kotlin/io/github/lmliam/kotventure/core/text/Compacted.kt) (so
structurally different but visually identical trees normalise to the same shape), serialized to JSON via the
[serializer](../serializer) module (lossless for every component type, including NBT, score, and selector), and
pretty-printed so committed snapshots produce reviewable line-by-line diffs. A mismatch — or a snapshot that has never
been recorded — fails the test with the offending JSON.

Snapshots live under your test resources at `snapshots/<name>.snapshot.json`:

```
src/test/resources/snapshots/welcome-message.snapshot.json
```

### Recording and updating

Snapshots are **never** written silently. A missing or differing snapshot is only recorded when record mode is on,
which is opt-in via a system property (scopes to a single run) or an environment variable (handy for CI):

| Setting | System property | Environment variable | Effect |
| --- | --- | --- | --- |
| Record mode | `kotventure.snapshot.update` | `SNAPSHOT_UPDATE` | When `true`/`1`/`yes`, writes/updates the snapshot and passes instead of failing |
| Snapshot directory | `kotventure.snapshot.dir` | `SNAPSHOT_DIR` | Reads and writes snapshots under this directory instead of the default test resources |

```bash
# Review the diff first; then record intentional changes:
SNAPSHOT_UPDATE=true ./gradlew :your-module:test
```

The factory (`matchSnapshot(name)`) and sugar (`shouldMatchSnapshot`) mirror the two-surface pattern above, so snapshot
matchers compose and negate like every other matcher.

## Compile-failure assertions

[`assertDoesNotCompile`](src/main/kotlin/io/github/lmliam/kotventure/test/compilation/Assertions.kt) verifies that a
snippet is rejected by the compiler — used to prove `@DslMarker` scope-safety and other compile-time guarantees.

## Adding a matcher

A new attribute gets a `have…`/`contain…` factory returning `Matcher<Component>` (with both a positive and a negated
failure message) plus the thin `shouldHave…` sugar, in the feature file for that concern. Matchers are a deliverable in
their own right: give every new matcher its own test asserting both the match and the failure message.
