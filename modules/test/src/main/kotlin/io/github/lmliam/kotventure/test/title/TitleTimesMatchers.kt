package io.github.lmliam.kotventure.test.title

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.title.Title
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

/**
 * Returns a matcher that compares the fade-in duration with [expected].
 *
 * The matcher converts the Adventure duration to a Kotlin [Duration] before comparison.
 */
public fun haveFadeIn(expected: Duration): Matcher<Title.Times> = timingMatcher("fade-in", expected) { it.fadeIn() }

/**
 * Returns a matcher that compares the stay duration with [expected].
 *
 * The matcher converts the Adventure duration to a Kotlin [Duration] before comparison.
 */
public fun haveStay(expected: Duration): Matcher<Title.Times> = timingMatcher("stay", expected) { it.stay() }

/**
 * Returns a matcher that compares the fade-out duration with [expected].
 *
 * The matcher converts the Adventure duration to a Kotlin [Duration] before comparison.
 */
public fun haveFadeOut(expected: Duration): Matcher<Title.Times> = timingMatcher("fade-out", expected) { it.fadeOut() }

/**
 * Verifies that this [Title.Times] has fade-in duration [expected].
 */
public infix fun Title.Times.shouldHaveFadeIn(expected: Duration): Title.Times =
    apply {
        this should haveFadeIn(expected)
    }

/**
 * Verifies that this [Title.Times] does not have fade-in duration [expected].
 */
public infix fun Title.Times.shouldNotHaveFadeIn(expected: Duration): Title.Times =
    apply {
        this shouldNot haveFadeIn(expected)
    }

/**
 * Verifies that this [Title.Times] has stay duration [expected].
 */
public infix fun Title.Times.shouldHaveStay(expected: Duration): Title.Times =
    apply {
        this should haveStay(expected)
    }

/**
 * Verifies that this [Title.Times] does not have stay duration [expected].
 */
public infix fun Title.Times.shouldNotHaveStay(expected: Duration): Title.Times =
    apply {
        this shouldNot haveStay(expected)
    }

/**
 * Verifies that this [Title.Times] has fade-out duration [expected].
 */
public infix fun Title.Times.shouldHaveFadeOut(expected: Duration): Title.Times =
    apply {
        this should haveFadeOut(expected)
    }

/**
 * Verifies that this [Title.Times] does not have fade-out duration [expected].
 */
public infix fun Title.Times.shouldNotHaveFadeOut(expected: Duration): Title.Times =
    apply {
        this shouldNot haveFadeOut(expected)
    }

private fun timingMatcher(
    slot: String,
    expected: Duration,
    actualOf: (Title.Times) -> java.time.Duration,
): Matcher<Title.Times> =
    Matcher { value ->
        val actual = actualOf(value).toKotlinDuration()
        MatcherResult(
            actual == expected,
            { "Expected title $slot <$expected>, but was <$actual>." },
            { "Expected title $slot not to be <$expected>." },
        )
    }
