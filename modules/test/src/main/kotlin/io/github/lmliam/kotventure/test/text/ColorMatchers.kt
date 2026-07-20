package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor

/**
 * Matches a component whose root colour is [expected]. Combine it with `and` or `or`, or negate it with `shouldNot`.
 */
public fun haveColor(expected: TextColor): Matcher<Component> =
    Matcher { value ->
        val actual = value.color()
        MatcherResult(
            actual == expected,
            { "Expected component color <$expected>, but was <$actual>." },
            { "Expected component color not to be <$expected>." },
        )
    }

/**
 * Matches a component that has no root colour.
 */
public fun haveNoColor(): Matcher<Component> =
    Matcher { value ->
        val actual = value.color()
        MatcherResult(
            actual == null,
            { "Expected component color to be absent, but was <$actual>." },
            { "Expected component color to be present." },
        )
    }

/**
 * Matches a component whose root shadow colour is [expected].
 */
public fun haveShadowColor(expected: ShadowColor): Matcher<Component> =
    Matcher { value ->
        val actual = value.style().shadowColor()
        MatcherResult(
            actual == expected,
            { "Expected component shadow color <$expected>, but was <$actual>." },
            { "Expected component shadow color not to be <$expected>." },
        )
    }

/**
 * Matches a component that has no root shadow colour.
 */
public fun haveNoShadowColor(): Matcher<Component> =
    Matcher { value ->
        val actual = value.style().shadowColor()
        MatcherResult(
            actual == null,
            { "Expected component shadow color to be absent, but was <$actual>." },
            { "Expected component shadow color to be present." },
        )
    }

/**
 * Asserts that this component has [expected] as its root colour.
 */
public infix fun Component.shouldHaveColor(expected: TextColor): Component =
    apply {
        this should haveColor(expected)
    }

/**
 * Asserts that this component has no root colour.
 */
public fun Component.shouldNotHaveColor(): Component =
    apply {
        this should haveNoColor()
    }

/**
 * Asserts that this component has [expected] as its root shadow colour.
 */
public infix fun Component.shouldHaveShadowColor(expected: ShadowColor): Component =
    apply {
        this should haveShadowColor(expected)
    }

/**
 * Asserts that this component has no root shadow colour.
 */
public fun Component.shouldNotHaveShadowColor(): Component =
    apply {
        this should haveNoShadowColor()
    }
