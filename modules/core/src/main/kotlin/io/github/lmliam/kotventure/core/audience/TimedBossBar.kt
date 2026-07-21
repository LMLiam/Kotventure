package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.bossbar.timed.TimedBossBar
import io.github.lmliam.kotventure.core.bossbar.timed.TimedBossBarBuilder
import io.github.lmliam.kotventure.core.bossbar.timed.TimedBossBarScope
import io.github.lmliam.kotventure.core.time.Ticker
import net.kyori.adventure.audience.Audience
import kotlin.time.Duration

/**
 * Creates a lifecycle-managed [TimedBossBar] that interpolates progress over [over], shows it on
 * this [Audience], and schedules updates on the contextual [Ticker].
 *
 * Provide a ticker once with `context(ticker) { … }` (platform adapters in production,
 * `ManualTicker` in tests). The [over] parameter selects managed behaviour. The static [bossBar] overload builds a
 * plain Adventure bar.
 *
 * @param over the positive lifetime of the bar. Progress reaches its end value when this duration elapses.
 * @param init configures name, colour, progress endpoints, cadence, and lifecycle hooks.
 * @return a [TimedBossBar] handle for pause/resume/cancel and extra viewers.
 * @throws IllegalArgumentException when [over] is not positive, a progress endpoint is outside
 *   `0f..1f`, or `every` exceeds [over].
 * @throws IllegalStateException when a required slot is missing or any singleton slot is set twice.
 * @sample io.github.lmliam.kotventure.core.audience.timedBossBarSample
 */
context(ticker: Ticker)
public fun Audience.bossBar(
    over: Duration,
    init: TimedBossBarScope.() -> Unit,
): TimedBossBar = TimedBossBarBuilder().apply(init).build(over, ticker, initialViewer = this)

/**
 * Shows the managed [bar] to this [Audience] and tracks this audience for auto-hide on
 * completion or cancel.
 *
 * This function delegates to [TimedBossBar.show]. It does nothing after the bar finishes or is cancelled.
 *
 * @sample io.github.lmliam.kotventure.core.audience.timedBossBarSample
 */
public fun Audience.show(bar: TimedBossBar): Unit = bar.show(this)

/**
 * Hides the managed [bar] from this [Audience] and stops tracking this audience for auto-hide.
 *
 * This function delegates to [TimedBossBar.hide].
 *
 * @sample io.github.lmliam.kotventure.core.audience.timedBossBarSample
 */
public fun Audience.hide(bar: TimedBossBar): Unit = bar.hide(this)
