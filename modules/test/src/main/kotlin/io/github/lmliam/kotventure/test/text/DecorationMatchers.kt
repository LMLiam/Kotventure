package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

/**
 * Matches a component whose root style enables [decoration] (state `TRUE`).
 *
 * For an explicit on/off/inherited check use [haveDecorationState]. Combine with `and`/`or` or negate with `shouldNot`.
 */
public fun haveDecoration(decoration: TextDecoration): Matcher<Component> = haveDecorationState(decoration, State.TRUE)

/**
 * Matches a component whose root style sets [decoration] to [state].
 */
public fun haveDecorationState(
    decoration: TextDecoration,
    state: State,
): Matcher<Component> =
    Matcher { value ->
        val actual = value.style().decoration(decoration)
        MatcherResult(
            actual == state,
            { "Expected component decoration <$decoration> to be <${state.name}>, but was <${actual.name}>." },
            { "Expected component decoration <$decoration> not to be <${state.name}>." },
        )
    }

/**
 * Asserts that this component has [expected] enabled on its root style.
 */
public infix fun Component.shouldHaveDecoration(expected: TextDecoration): Component =
    apply {
        this should haveDecoration(expected)
    }

/**
 * Asserts that this component has [decoration] set to [state] on its root style.
 */
public fun Component.shouldHaveDecoration(
    decoration: TextDecoration,
    state: State,
): Component =
    apply {
        this should haveDecorationState(decoration, state)
    }

/**
 * Asserts that this component leaves [expected] unset (state `NOT_SET`) on its root style.
 */
public infix fun Component.shouldNotHaveDecoration(expected: TextDecoration): Component =
    apply {
        this should haveDecorationState(expected, State.NOT_SET)
    }

/**
 * Asserts that this component is bold (decoration `BOLD` enabled).
 */
public fun Component.shouldBeBold(): Component =
    apply {
        this should haveDecoration(TextDecoration.BOLD)
    }

/**
 * Asserts that this component is not bold (decoration `BOLD` not enabled).
 */
public fun Component.shouldNotBeBold(): Component =
    apply {
        this shouldNot haveDecoration(TextDecoration.BOLD)
    }

/**
 * Asserts that this component is italic (decoration `ITALIC` enabled).
 */
public fun Component.shouldBeItalic(): Component =
    apply {
        this should haveDecoration(TextDecoration.ITALIC)
    }

/**
 * Asserts that this component is not italic (decoration `ITALIC` not enabled).
 */
public fun Component.shouldNotBeItalic(): Component =
    apply {
        this shouldNot haveDecoration(TextDecoration.ITALIC)
    }

/**
 * Asserts that this component is underlined (decoration `UNDERLINED` enabled).
 */
public fun Component.shouldBeUnderlined(): Component =
    apply {
        this should haveDecoration(TextDecoration.UNDERLINED)
    }

/**
 * Asserts that this component is not underlined (decoration `UNDERLINED` not enabled).
 */
public fun Component.shouldNotBeUnderlined(): Component =
    apply {
        this shouldNot haveDecoration(TextDecoration.UNDERLINED)
    }

/**
 * Asserts that this component is struck through (decoration `STRIKETHROUGH` enabled).
 */
public fun Component.shouldBeStrikethrough(): Component =
    apply {
        this should haveDecoration(TextDecoration.STRIKETHROUGH)
    }

/**
 * Asserts that this component is not struck through (decoration `STRIKETHROUGH` not enabled).
 */
public fun Component.shouldNotBeStrikethrough(): Component =
    apply {
        this shouldNot haveDecoration(TextDecoration.STRIKETHROUGH)
    }

/**
 * Asserts that this component is obfuscated (decoration `OBFUSCATED` enabled).
 */
public fun Component.shouldBeObfuscated(): Component =
    apply {
        this should haveDecoration(TextDecoration.OBFUSCATED)
    }

/**
 * Asserts that this component is not obfuscated (decoration `OBFUSCATED` not enabled).
 */
public fun Component.shouldNotBeObfuscated(): Component =
    apply {
        this shouldNot haveDecoration(TextDecoration.OBFUSCATED)
    }
