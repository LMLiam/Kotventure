package io.github.lmliam.kotventure.test.title

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.title.Title
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

/**
 * Matches [Title.Times] whose fade-in equals [expected].
 *
 * Takes [kotlin.time.Duration] so call sites can use `1.ticks` / `3.seconds`; convert Adventure
 * values (e.g. `Title.DEFAULT_TIMES.fadeIn()`) with [toKotlinDuration].
 */
public fun haveFadeIn(expected: Duration): Matcher<Title.Times> = timingMatcher("fade-in", expected) { it.fadeIn() }

/**
 * Matches [Title.Times] whose stay equals [expected].
 *
 * Takes [kotlin.time.Duration] so call sites can use `1.ticks` / `3.seconds`; convert Adventure
 * values (e.g. `Title.DEFAULT_TIMES.stay()`) with [toKotlinDuration].
 */
public fun haveStay(expected: Duration): Matcher<Title.Times> = timingMatcher("stay", expected) { it.stay() }

/**
 * Matches [Title.Times] whose fade-out equals [expected].
 *
 * Takes [kotlin.time.Duration] so call sites can use `1.ticks` / `3.seconds`; convert Adventure
 * values (e.g. `Title.DEFAULT_TIMES.fadeOut()`) with [toKotlinDuration].
 */
public fun haveFadeOut(expected: Duration): Matcher<Title.Times> = timingMatcher("fade-out", expected) { it.fadeOut() }

/**
 * Asserts this [Title.Times] has the given fade-in duration.
 */
public infix fun Title.Times.shouldHaveFadeIn(expected: Duration): Title.Times =
    apply {
        this should haveFadeIn(expected)
    }

/**
 * Asserts this [Title.Times] does not have the given fade-in duration.
 */
public infix fun Title.Times.shouldNotHaveFadeIn(expected: Duration): Title.Times =
    apply {
        this shouldNot haveFadeIn(expected)
    }

/**
 * Asserts this [Title.Times] has the given stay duration.
 */
public infix fun Title.Times.shouldHaveStay(expected: Duration): Title.Times =
    apply {
        this should haveStay(expected)
    }

/**
 * Asserts this [Title.Times] does not have the given stay duration.
 */
public infix fun Title.Times.shouldNotHaveStay(expected: Duration): Title.Times =
    apply {
        this shouldNot haveStay(expected)
    }

/**
 * Asserts this [Title.Times] has the given fade-out duration.
 */
public infix fun Title.Times.shouldHaveFadeOut(expected: Duration): Title.Times =
    apply {
        this should haveFadeOut(expected)
    }

/**
 * Asserts this [Title.Times] does not have the given fade-out duration.
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
