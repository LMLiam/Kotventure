package io.github.lmliam.kotventure.test.title

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.title.Title
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

/**
 * Matches [Title.Times] whose fade-in equals [expected].
 *
 * Accepts [kotlin.time.Duration] so call sites can use `1.ticks` / `3.seconds` without
 * `toJavaDuration()`.
 */
public fun haveFadeIn(expected: Duration): Matcher<Title.Times> =
    timingMatcher("fade-in", expected.toJavaDuration()) { it.fadeIn() }

/**
 * Matches [Title.Times] whose fade-in equals [expected].
 *
 * Accepts [java.time.Duration] so values from Adventure (e.g. `Title.DEFAULT_TIMES.fadeIn()`)
 * need no conversion.
 */
public fun haveFadeIn(expected: JavaDuration): Matcher<Title.Times> = timingMatcher("fade-in", expected) { it.fadeIn() }

/**
 * Matches [Title.Times] whose stay equals [expected].
 *
 * Accepts [kotlin.time.Duration] so call sites can use `1.ticks` / `3.seconds` without
 * `toJavaDuration()`.
 */
public fun haveStay(expected: Duration): Matcher<Title.Times> =
    timingMatcher("stay", expected.toJavaDuration()) { it.stay() }

/**
 * Matches [Title.Times] whose stay equals [expected].
 *
 * Accepts [java.time.Duration] so values from Adventure need no conversion.
 */
public fun haveStay(expected: JavaDuration): Matcher<Title.Times> = timingMatcher("stay", expected) { it.stay() }

/**
 * Matches [Title.Times] whose fade-out equals [expected].
 *
 * Accepts [kotlin.time.Duration] so call sites can use `1.ticks` / `3.seconds` without
 * `toJavaDuration()`.
 */
public fun haveFadeOut(expected: Duration): Matcher<Title.Times> =
    timingMatcher("fade-out", expected.toJavaDuration()) { it.fadeOut() }

/**
 * Matches [Title.Times] whose fade-out equals [expected].
 *
 * Accepts [java.time.Duration] so values from Adventure need no conversion.
 */
public fun haveFadeOut(expected: JavaDuration): Matcher<Title.Times> =
    timingMatcher("fade-out", expected) { it.fadeOut() }

/**
 * Asserts this [Title.Times] has the given fade-in duration.
 */
public infix fun Title.Times.shouldHaveFadeIn(expected: Duration): Title.Times =
    apply {
        this should haveFadeIn(expected)
    }

/**
 * Asserts this [Title.Times] has the given fade-in duration.
 */
public infix fun Title.Times.shouldHaveFadeIn(expected: JavaDuration): Title.Times =
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
 * Asserts this [Title.Times] does not have the given fade-in duration.
 */
public infix fun Title.Times.shouldNotHaveFadeIn(expected: JavaDuration): Title.Times =
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
 * Asserts this [Title.Times] has the given stay duration.
 */
public infix fun Title.Times.shouldHaveStay(expected: JavaDuration): Title.Times =
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
 * Asserts this [Title.Times] does not have the given stay duration.
 */
public infix fun Title.Times.shouldNotHaveStay(expected: JavaDuration): Title.Times =
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
 * Asserts this [Title.Times] has the given fade-out duration.
 */
public infix fun Title.Times.shouldHaveFadeOut(expected: JavaDuration): Title.Times =
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

/**
 * Asserts this [Title.Times] does not have the given fade-out duration.
 */
public infix fun Title.Times.shouldNotHaveFadeOut(expected: JavaDuration): Title.Times =
    apply {
        this shouldNot haveFadeOut(expected)
    }

private fun timingMatcher(
    slot: String,
    expected: JavaDuration,
    actualOf: (Title.Times) -> JavaDuration,
): Matcher<Title.Times> =
    Matcher { value ->
        val actual = actualOf(value)
        MatcherResult(
            actual == expected,
            {
                "Expected title $slot <${expected.toKotlinDuration()}>, " +
                    "but was <${actual.toKotlinDuration()}>."
            },
            {
                "Expected title $slot not to be <${expected.toKotlinDuration()}>."
            },
        )
    }
