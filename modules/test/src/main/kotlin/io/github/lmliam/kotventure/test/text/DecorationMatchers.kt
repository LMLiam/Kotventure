package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

/**
 * Returns a matcher that accepts a component with [decoration] enabled on the root style.
 *
 * Use [haveDecorationState] to test an explicit enabled, disabled, or unset state.
 */
public fun haveDecoration(decoration: TextDecoration): Matcher<Component> = haveDecorationState(decoration, State.TRUE)

/**
 * Returns a matcher that accepts [state] for [decoration] on the root style.
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
 * Verifies that this component enables [expected] on its root style.
 */
public infix fun Component.shouldHaveDecoration(expected: TextDecoration): Component =
    apply {
        this should haveDecoration(expected)
    }

/**
 * Verifies that this component sets [decoration] to [state] on its root style.
 */
public fun Component.shouldHaveDecoration(
    decoration: TextDecoration,
    state: State,
): Component =
    apply {
        this should haveDecorationState(decoration, state)
    }

/**
 * Verifies that this component leaves [expected] unset on its root style.
 *
 * This function requires [State.NOT_SET]. It does not accept [State.FALSE]. Use the explicit-state
 * overload of [shouldHaveDecoration] when you require [State.FALSE]. Use a function such as
 * [shouldNotBeBold] when both states mean "not enabled".
 */
public infix fun Component.shouldNotHaveDecoration(expected: TextDecoration): Component =
    apply {
        this should haveDecorationState(expected, State.NOT_SET)
    }

/**
 * Verifies that this component enables [TextDecoration.BOLD] on its root style.
 */
public fun Component.shouldBeBold(): Component =
    apply {
        this should haveDecoration(TextDecoration.BOLD)
    }

/**
 * Verifies that this component does not enable [TextDecoration.BOLD] on its root style.
 *
 * This function accepts [State.NOT_SET] and [State.FALSE].
 */
public fun Component.shouldNotBeBold(): Component =
    apply {
        this shouldNot haveDecoration(TextDecoration.BOLD)
    }

/**
 * Verifies that this component enables [TextDecoration.ITALIC] on its root style.
 */
public fun Component.shouldBeItalic(): Component =
    apply {
        this should haveDecoration(TextDecoration.ITALIC)
    }

/**
 * Verifies that this component does not enable [TextDecoration.ITALIC] on its root style.
 *
 * This function accepts [State.NOT_SET] and [State.FALSE].
 */
public fun Component.shouldNotBeItalic(): Component =
    apply {
        this shouldNot haveDecoration(TextDecoration.ITALIC)
    }

/**
 * Verifies that this component enables [TextDecoration.UNDERLINED] on its root style.
 */
public fun Component.shouldBeUnderlined(): Component =
    apply {
        this should haveDecoration(TextDecoration.UNDERLINED)
    }

/**
 * Verifies that this component does not enable [TextDecoration.UNDERLINED] on its root style.
 *
 * This function accepts [State.NOT_SET] and [State.FALSE].
 */
public fun Component.shouldNotBeUnderlined(): Component =
    apply {
        this shouldNot haveDecoration(TextDecoration.UNDERLINED)
    }

/**
 * Verifies that this component enables [TextDecoration.STRIKETHROUGH] on its root style.
 */
public fun Component.shouldBeStrikethrough(): Component =
    apply {
        this should haveDecoration(TextDecoration.STRIKETHROUGH)
    }

/**
 * Verifies that this component does not enable [TextDecoration.STRIKETHROUGH] on its root style.
 *
 * This function accepts [State.NOT_SET] and [State.FALSE].
 */
public fun Component.shouldNotBeStrikethrough(): Component =
    apply {
        this shouldNot haveDecoration(TextDecoration.STRIKETHROUGH)
    }

/**
 * Verifies that this component enables [TextDecoration.OBFUSCATED] on its root style.
 */
public fun Component.shouldBeObfuscated(): Component =
    apply {
        this should haveDecoration(TextDecoration.OBFUSCATED)
    }

/**
 * Verifies that this component does not enable [TextDecoration.OBFUSCATED] on its root style.
 *
 * This function accepts [State.NOT_SET] and [State.FALSE].
 */
public fun Component.shouldNotBeObfuscated(): Component =
    apply {
        this shouldNot haveDecoration(TextDecoration.OBFUSCATED)
    }
