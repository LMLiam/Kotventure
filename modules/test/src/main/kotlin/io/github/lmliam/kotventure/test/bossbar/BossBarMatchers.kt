package io.github.lmliam.kotventure.test.bossbar

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.bossbar.BossBar

/**
 * Returns a matcher that compares boss-bar progress with [expected].
 */
public fun haveProgress(expected: Float): Matcher<BossBar> =
    Matcher { value ->
        val actual = value.progress()
        MatcherResult(
            actual == expected,
            { "Expected boss bar progress <$expected>, but was <$actual>." },
            { "Expected boss bar progress not to be <$expected>." },
        )
    }

/**
 * Returns a matcher that compares the boss-bar colour with [expected].
 */
public fun haveBossBarColor(expected: BossBar.Color): Matcher<BossBar> =
    Matcher { value ->
        val actual = value.color()
        MatcherResult(
            actual == expected,
            { "Expected boss bar color <$expected>, but was <$actual>." },
            { "Expected boss bar color not to be <$expected>." },
        )
    }

/**
 * Returns a matcher that compares the boss-bar overlay with [expected].
 */
public fun haveOverlay(expected: BossBar.Overlay): Matcher<BossBar> =
    Matcher { value ->
        val actual = value.overlay()
        MatcherResult(
            actual == expected,
            { "Expected boss bar overlay <$expected>, but was <$actual>." },
            { "Expected boss bar overlay not to be <$expected>." },
        )
    }

/**
 * Returns a matcher that compares the complete boss-bar flag set with [expected].
 */
public fun haveFlags(expected: Set<BossBar.Flag>): Matcher<BossBar> =
    Matcher { value ->
        val actual = value.flags()
        MatcherResult(
            actual == expected,
            { "Expected boss bar flags <$expected>, but was <$actual>." },
            { "Expected boss bar flags not to be <$expected>." },
        )
    }

/**
 * Returns a matcher that accepts a boss bar that has [expected].
 */
public fun haveFlag(expected: BossBar.Flag): Matcher<BossBar> =
    Matcher { value ->
        MatcherResult(
            value.hasFlag(expected),
            { "Expected boss bar to have flag <$expected>, but flags were <${value.flags()}>." },
            { "Expected boss bar not to have flag <$expected>." },
        )
    }

/**
 * Returns a matcher that accepts a boss bar without flags.
 */
public fun haveNoFlags(): Matcher<BossBar> =
    Matcher { value ->
        val actual = value.flags()
        MatcherResult(
            actual.isEmpty(),
            { "Expected boss bar to have no flags, but was <$actual>." },
            { "Expected boss bar to have flags." },
        )
    }

/**
 * Verifies that this [BossBar] has progress [expected].
 */
public infix fun BossBar.shouldHaveProgress(expected: Float): BossBar =
    apply {
        this should haveProgress(expected)
    }

/**
 * Verifies that this [BossBar] does not have progress [expected].
 */
public infix fun BossBar.shouldNotHaveProgress(expected: Float): BossBar =
    apply {
        this shouldNot haveProgress(expected)
    }

/**
 * Verifies that this [BossBar] has colour [expected].
 */
public infix fun BossBar.shouldHaveColor(expected: BossBar.Color): BossBar =
    apply {
        this should haveBossBarColor(expected)
    }

/**
 * Verifies that this [BossBar] does not have colour [expected].
 */
public infix fun BossBar.shouldNotHaveColor(expected: BossBar.Color): BossBar =
    apply {
        this shouldNot haveBossBarColor(expected)
    }

/**
 * Verifies that this [BossBar] has overlay [expected].
 */
public infix fun BossBar.shouldHaveOverlay(expected: BossBar.Overlay): BossBar =
    apply {
        this should haveOverlay(expected)
    }

/**
 * Verifies that this [BossBar] does not have overlay [expected].
 */
public infix fun BossBar.shouldNotHaveOverlay(expected: BossBar.Overlay): BossBar =
    apply {
        this shouldNot haveOverlay(expected)
    }

/**
 * Verifies that this [BossBar] has exactly the flag set [expected].
 */
public infix fun BossBar.shouldHaveFlags(expected: Set<BossBar.Flag>): BossBar =
    apply {
        this should haveFlags(expected)
    }

/**
 * Verifies that this [BossBar] does not have exactly the flag set [expected].
 */
public infix fun BossBar.shouldNotHaveFlags(expected: Set<BossBar.Flag>): BossBar =
    apply {
        this shouldNot haveFlags(expected)
    }

/**
 * Verifies that this [BossBar] has the flag [expected].
 */
public infix fun BossBar.shouldHaveFlag(expected: BossBar.Flag): BossBar =
    apply {
        this should haveFlag(expected)
    }

/**
 * Verifies that this [BossBar] does not have the flag [expected].
 */
public infix fun BossBar.shouldNotHaveFlag(expected: BossBar.Flag): BossBar =
    apply {
        this shouldNot haveFlag(expected)
    }

/**
 * Verifies that this [BossBar] has no flags.
 */
public fun BossBar.shouldHaveNoFlags(): BossBar =
    apply {
        this should haveNoFlags()
    }
