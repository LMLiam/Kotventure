package io.github.lmliam.kotventure.test.bossbar

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.bossbar.BossBar

/**
 * Matches a [BossBar] whose progress equals [expected].
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
 * Matches a [BossBar] whose colour equals [expected].
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
 * Matches a [BossBar] whose overlay equals [expected].
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
 * Matches a [BossBar] whose flag set equals [expected].
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
 * Matches a [BossBar] that includes [expected] among its flags.
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
 * Matches a [BossBar] with no flags set.
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
 * Asserts this [BossBar] has progress [expected].
 */
public infix fun BossBar.shouldHaveProgress(expected: Float): BossBar =
    apply {
        this should haveProgress(expected)
    }

/**
 * Asserts this [BossBar] does not have progress [expected].
 */
public infix fun BossBar.shouldNotHaveProgress(expected: Float): BossBar =
    apply {
        this shouldNot haveProgress(expected)
    }

/**
 * Asserts this [BossBar] has colour [expected].
 */
public infix fun BossBar.shouldHaveColor(expected: BossBar.Color): BossBar =
    apply {
        this should haveBossBarColor(expected)
    }

/**
 * Asserts this [BossBar] does not have colour [expected].
 */
public infix fun BossBar.shouldNotHaveColor(expected: BossBar.Color): BossBar =
    apply {
        this shouldNot haveBossBarColor(expected)
    }

/**
 * Asserts this [BossBar] has overlay [expected].
 */
public infix fun BossBar.shouldHaveOverlay(expected: BossBar.Overlay): BossBar =
    apply {
        this should haveOverlay(expected)
    }

/**
 * Asserts this [BossBar] does not have overlay [expected].
 */
public infix fun BossBar.shouldNotHaveOverlay(expected: BossBar.Overlay): BossBar =
    apply {
        this shouldNot haveOverlay(expected)
    }

/**
 * Asserts this [BossBar] has exactly the flag set [expected].
 */
public infix fun BossBar.shouldHaveFlags(expected: Set<BossBar.Flag>): BossBar =
    apply {
        this should haveFlags(expected)
    }

/**
 * Asserts this [BossBar] does not have exactly the flag set [expected].
 */
public infix fun BossBar.shouldNotHaveFlags(expected: Set<BossBar.Flag>): BossBar =
    apply {
        this shouldNot haveFlags(expected)
    }

/**
 * Asserts this [BossBar] includes [expected] among its flags.
 */
public infix fun BossBar.shouldHaveFlag(expected: BossBar.Flag): BossBar =
    apply {
        this should haveFlag(expected)
    }

/**
 * Asserts this [BossBar] does not include [expected] among its flags.
 */
public infix fun BossBar.shouldNotHaveFlag(expected: BossBar.Flag): BossBar =
    apply {
        this shouldNot haveFlag(expected)
    }

/**
 * Asserts this [BossBar] has no flags set.
 */
public fun BossBar.shouldHaveNoFlags(): BossBar =
    apply {
        this should haveNoFlags()
    }
