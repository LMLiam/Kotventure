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
 * Matches [Title.Times] whose fade-in, stay, and fade-out equal the given Kotlin [Duration]s.
 *
 * Converts expectations at the assertion boundary so call sites can stay in `kotlin.time` (e.g.
 * `1.ticks`, `3.seconds`) without calling `toJavaDuration()`.
 */
public fun haveTimes(
    fadeIn: Duration,
    stay: Duration,
    fadeOut: Duration,
): Matcher<Title.Times> =
    Matcher { value ->
        val expectedFadeIn = fadeIn.toJavaDuration()
        val expectedStay = stay.toJavaDuration()
        val expectedFadeOut = fadeOut.toJavaDuration()
        val actualFadeIn = value.fadeIn()
        val actualStay = value.stay()
        val actualFadeOut = value.fadeOut()
        MatcherResult(
            actualFadeIn == expectedFadeIn &&
                actualStay == expectedStay &&
                actualFadeOut == expectedFadeOut,
            {
                "Expected title times <fadeIn=$fadeIn, stay=$stay, fadeOut=$fadeOut>, " +
                    "but was <fadeIn=${actualFadeIn.toKotlinDuration()}, " +
                    "stay=${actualStay.toKotlinDuration()}, " +
                    "fadeOut=${actualFadeOut.toKotlinDuration()}>."
            },
            {
                "Expected title times not to be <fadeIn=$fadeIn, stay=$stay, fadeOut=$fadeOut>."
            },
        )
    }

/**
 * Asserts this [Title.Times] has the given fade-in, stay, and fade-out durations.
 */
public fun Title.Times.shouldHaveTimes(
    fadeIn: Duration,
    stay: Duration,
    fadeOut: Duration,
): Title.Times =
    apply {
        this should haveTimes(fadeIn, stay, fadeOut)
    }

/**
 * Asserts this [Title.Times] does not have the given fade-in, stay, and fade-out durations.
 */
public fun Title.Times.shouldNotHaveTimes(
    fadeIn: Duration,
    stay: Duration,
    fadeOut: Duration,
): Title.Times =
    apply {
        this shouldNot haveTimes(fadeIn, stay, fadeOut)
    }
