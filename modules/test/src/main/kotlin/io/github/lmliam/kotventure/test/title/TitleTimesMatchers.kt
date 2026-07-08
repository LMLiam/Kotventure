package io.github.lmliam.kotventure.test.title

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.title.Title
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

/**
 * Matches [Title.Times] whose fade-in equals [expected].
 *
 * Converts at the assertion boundary so call sites can stay in `kotlin.time` (e.g. `1.ticks`)
 * without calling `toJavaDuration()`.
 */
public fun haveFadeIn(expected: Duration): Matcher<Title.Times> = timingMatcher("fade-in", expected) { it.fadeIn() }

/**
 * Matches [Title.Times] whose stay equals [expected].
 *
 * Converts at the assertion boundary so call sites can stay in `kotlin.time` without calling
 * `toJavaDuration()`.
 */
public fun haveStay(expected: Duration): Matcher<Title.Times> = timingMatcher("stay", expected) { it.stay() }

/**
 * Matches [Title.Times] whose fade-out equals [expected].
 *
 * Converts at the assertion boundary so call sites can stay in `kotlin.time` without calling
 * `toJavaDuration()`.
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
        val expectedJava = expected.toJavaDuration()
        val actual = actualOf(value)
        MatcherResult(
            actual == expectedJava,
            {
                "Expected title $slot <$expected>, but was <${actual.toKotlinDuration()}>."
            },
            {
                "Expected title $slot not to be <$expected>."
            },
        )
    }
