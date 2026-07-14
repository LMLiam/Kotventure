package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.Ticker
import org.bukkit.plugin.Plugin

/**
 * Creates a [Ticker] that runs repeating work on this plugin's Bukkit scheduler.
 *
 * Work runs on the server main thread, so it may touch world and player state directly. The
 * Bukkit scheduler only fires on whole game ticks (50 ms), so the returned ticker rejects
 * intervals it cannot honour instead of silently rounding them:
 * [Ticker.repeating] throws [IllegalArgumentException] unless the interval is a positive whole
 * number of ticks (`1.seconds`, `500.milliseconds`, and `3.ticks` are all fine; `75.milliseconds`
 * is not).
 *
 * Tasks are not bound to the plugin lifecycle beyond what Bukkit provides: the server cancels
 * them when the plugin disables.
 *
 * @sample io.github.lmliam.kotventure.paper.time.tickerSample
 */
public fun Plugin.ticker(): Ticker = PaperTicker(this)
