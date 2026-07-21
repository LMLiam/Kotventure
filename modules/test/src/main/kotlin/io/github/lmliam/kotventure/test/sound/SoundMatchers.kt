package io.github.lmliam.kotventure.test.sound

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

/**
 * Returns a matcher that compares the sound name with [expected].
 */
public fun haveName(expected: Key): Matcher<Sound> = soundMatcher("name", expected) { it.name() }

/**
 * Returns a matcher that compares the sound source with [expected].
 */
public fun haveSource(expected: Sound.Source): Matcher<Sound> = soundMatcher("source", expected) { it.source() }

/**
 * Returns a matcher that compares the sound volume with [expected].
 */
public fun haveVolume(expected: Float): Matcher<Sound> = soundMatcher("volume", expected) { it.volume() }

/**
 * Returns a matcher that compares the sound pitch with [expected].
 */
public fun havePitch(expected: Float): Matcher<Sound> = soundMatcher("pitch", expected) { it.pitch() }

/**
 * Returns a matcher that accepts a sound with seed [expected].
 */
public fun haveSeed(expected: Long): Matcher<Sound> =
    Matcher { value ->
        val actual = value.seed()
        MatcherResult(
            actual.isPresent && actual.asLong == expected,
            { "Expected sound seed <$expected>, but was <$actual>." },
            { "Expected sound seed not to be <$expected>." },
        )
    }

/**
 * Returns a matcher that accepts a sound without a seed.
 */
public fun haveNoSeed(): Matcher<Sound> =
    Matcher { value ->
        val actual = value.seed()
        MatcherResult(
            actual.isEmpty,
            { "Expected sound to have no seed, but was <$actual>." },
            { "Expected sound to have a seed." },
        )
    }

/**
 * Verifies that this [Sound] has name [expected].
 */
public infix fun Sound.shouldHaveName(expected: Key): Sound =
    apply {
        this should haveName(expected)
    }

/**
 * Verifies that this [Sound] does not have name [expected].
 */
public infix fun Sound.shouldNotHaveName(expected: Key): Sound =
    apply {
        this shouldNot haveName(expected)
    }

/**
 * Verifies that this [Sound] has source [expected].
 */
public infix fun Sound.shouldHaveSource(expected: Sound.Source): Sound =
    apply {
        this should haveSource(expected)
    }

/**
 * Verifies that this [Sound] does not have source [expected].
 */
public infix fun Sound.shouldNotHaveSource(expected: Sound.Source): Sound =
    apply {
        this shouldNot haveSource(expected)
    }

/**
 * Verifies that this [Sound] has volume [expected].
 */
public infix fun Sound.shouldHaveVolume(expected: Float): Sound =
    apply {
        this should haveVolume(expected)
    }

/**
 * Verifies that this [Sound] does not have volume [expected].
 */
public infix fun Sound.shouldNotHaveVolume(expected: Float): Sound =
    apply {
        this shouldNot haveVolume(expected)
    }

/**
 * Verifies that this [Sound] has pitch [expected].
 */
public infix fun Sound.shouldHavePitch(expected: Float): Sound =
    apply {
        this should havePitch(expected)
    }

/**
 * Verifies that this [Sound] does not have pitch [expected].
 */
public infix fun Sound.shouldNotHavePitch(expected: Float): Sound =
    apply {
        this shouldNot havePitch(expected)
    }

/**
 * Verifies that this [Sound] has seed [expected].
 */
public infix fun Sound.shouldHaveSeed(expected: Long): Sound =
    apply {
        this should haveSeed(expected)
    }

/**
 * Verifies that this [Sound] does not have seed [expected].
 */
public infix fun Sound.shouldNotHaveSeed(expected: Long): Sound =
    apply {
        this shouldNot haveSeed(expected)
    }

/**
 * Verifies that this [Sound] has no seed.
 */
public fun Sound.shouldHaveNoSeed(): Sound =
    apply {
        this should haveNoSeed()
    }

private fun <T> soundMatcher(
    slot: String,
    expected: T,
    actualOf: (Sound) -> T,
): Matcher<Sound> =
    Matcher { value ->
        val actual = actualOf(value)
        MatcherResult(
            actual == expected,
            { "Expected sound $slot <$expected>, but was <$actual>." },
            { "Expected sound $slot not to be <$expected>." },
        )
    }
