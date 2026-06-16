package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor

/**
 * Matches a component whose root color is [expected]. Combine with `and`/`or` or negate with `shouldNot`.
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
 * Matches a component that has no root color.
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
 * Matches a component whose root shadow color is [expected].
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
 * Matches a component that has no root shadow color.
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
 * Asserts that this component has [expected] as its root color.
 */
public infix fun Component.shouldHaveColor(expected: TextColor): Component =
    apply {
        this should haveColor(expected)
    }

/**
 * Asserts that this component has no root color.
 */
public fun Component.shouldNotHaveColor(): Component =
    apply {
        this should haveNoColor()
    }

/**
 * Asserts that this component has [expected] as its root shadow color.
 */
public infix fun Component.shouldHaveShadowColor(expected: ShadowColor): Component =
    apply {
        this should haveShadowColor(expected)
    }

/**
 * Asserts that this component has no root shadow color.
 */
public fun Component.shouldNotHaveShadowColor(): Component =
    apply {
        this should haveNoShadowColor()
    }
