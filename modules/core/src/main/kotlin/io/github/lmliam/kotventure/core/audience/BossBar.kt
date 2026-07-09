package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.bossbar.BossBarScope
import io.github.lmliam.kotventure.core.bossbar.timed.TimedBossBar
import io.github.lmliam.kotventure.core.bossbar.timed.TimedBossBarBuilder
import io.github.lmliam.kotventure.core.bossbar.timed.TimedBossBarScope
import io.github.lmliam.kotventure.core.time.Ticker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import kotlin.time.Duration
import io.github.lmliam.kotventure.core.bossbar.bossBar as buildBossBar

/**
 * Shows [bar] to this [Audience].
 *
 * Type-overloaded [show] so future showable types (books, …) can share the verb.
 *
 * @sample io.github.lmliam.kotventure.core.audience.audienceShowHideBossBarSample
 */
public fun Audience.show(bar: BossBar): Unit = showBossBar(bar)

/**
 * Hides [bar] from this [Audience].
 *
 * Type-overloaded [hide] so future hideable types can share the verb.
 *
 * @sample io.github.lmliam.kotventure.core.audience.audienceShowHideBossBarSample
 */
public fun Audience.hide(bar: BossBar): Unit = hideBossBar(bar)

/**
 * Builds a [BossBar] from [init], shows it on this [Audience], and returns it for later
 * [hide] or live updates.
 *
 * Works for any audience — a player, the console, or a forwarding audience over many members;
 * audiences without a boss-bar surface ignore it.
 *
 * @throws IllegalStateException when `name` is missing or any slot/flag is set twice.
 * @throws IllegalArgumentException when `progress` is outside `0f..1f`.
 * @sample io.github.lmliam.kotventure.core.audience.audienceBossBarSample
 */
public fun Audience.bossBar(init: BossBarScope.() -> Unit): BossBar {
    val bar = buildBossBar(init)
    show(bar)
    return bar
}

/**
 * Builds a lifecycle-managed [TimedBossBar] that interpolates progress over [over], shows it on
 * this [Audience], and schedules updates on the contextual [Ticker].
 *
 * Provide a ticker once with `context(ticker) { … }` (platform adapters in production,
 * `ManualTicker` in tests). The [over] parameter is what opts into managed behaviour; the static
 * [bossBar] overload builds a plain Adventure bar.
 *
 * @param over positive lifetime of the bar; progress reaches its end value exactly when this
 *   elapses.
 * @param init configures name, colour, progress endpoints, cadence, and lifecycle hooks.
 * @return a [TimedBossBar] handle for pause/resume/cancel and extra viewers.
 * @throws IllegalArgumentException when [over] is not positive, or a progress endpoint is outside
 *   `0f..1f`.
 * @throws IllegalStateException when a required slot is missing or any singleton slot is set twice.
 * @sample io.github.lmliam.kotventure.core.audience.timedBossBarSample
 */
context(ticker: Ticker)
public fun Audience.bossBar(
    over: Duration,
    init: TimedBossBarScope.() -> Unit,
): TimedBossBar {
    val config = TimedBossBarBuilder().apply(init).buildConfig(over)
    return TimedBossBar(ticker, config, creator = this)
}
