package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style

/**
 * Returns a matcher that compares the complete root [Style] with [expected].
 */
public fun haveStyle(expected: Style): Matcher<Component> =
    Matcher { value ->
        val actual = value.style()
        MatcherResult(
            actual == expected,
            { "Expected component style <$expected>, but was <$actual>." },
            { "Expected component style not to be <$expected>." },
        )
    }

/**
 * Returns a matcher that compares the root font with [expected].
 */
public fun haveFont(expected: Key): Matcher<Component> =
    Matcher { value ->
        val actual = value.font()
        MatcherResult(
            actual == expected,
            { "Expected component font <$expected>, but was <${actual ?: "null"}>." },
            { "Expected component font not to be <$expected>." },
        )
    }

/**
 * Returns a matcher that accepts a component without a root font.
 */
public fun haveNoFont(): Matcher<Component> =
    Matcher { value ->
        val actual = value.font()
        MatcherResult(
            actual == null,
            { "Expected component font to be absent, but was <$actual>." },
            { "Expected component font to be present." },
        )
    }

/**
 * Returns a matcher that compares the root shift-click insertion text with [expected].
 */
public fun haveInsertion(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.insertion()
        MatcherResult(
            actual == expected,
            { "Expected component insertion <$expected>, but was <${actual ?: "null"}>." },
            { "Expected component insertion not to be <$expected>." },
        )
    }

/**
 * Returns a matcher that accepts a component without root shift-click insertion text.
 */
public fun haveNoInsertion(): Matcher<Component> =
    Matcher { value ->
        val actual = value.insertion()
        MatcherResult(
            actual == null,
            { "Expected component insertion to be absent, but was <$actual>." },
            { "Expected component insertion to be present." },
        )
    }

/**
 * Verifies that this component has [expected] as its complete root style.
 */
public infix fun Component.shouldHaveStyle(expected: Style): Component =
    apply {
        this should haveStyle(expected)
    }

/**
 * Verifies that this component has [expected] as its root font.
 */
public infix fun Component.shouldHaveFont(expected: Key): Component =
    apply {
        this should haveFont(expected)
    }

/**
 * Verifies that this component has no root font.
 */
public fun Component.shouldNotHaveFont(): Component =
    apply {
        this should haveNoFont()
    }

/**
 * Verifies that this component has [expected] as its root shift-click insertion text.
 */
public infix fun Component.shouldHaveInsertion(expected: String): Component =
    apply {
        this should haveInsertion(expected)
    }

/**
 * Verifies that this component has no root shift-click insertion text.
 */
public fun Component.shouldNotHaveInsertion(): Component =
    apply {
        this should haveNoInsertion()
    }
