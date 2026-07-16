package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.Ticker
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

/**
 * Creates a [Ticker] that runs repeating work in this plugin's global tick context.
 *
 * Work runs on the global region scheduler ([Server.getGlobalRegionScheduler]) — the main thread
 * on plain Paper, the global region thread on Folia — so the same code serves both flavours.
 * Paper's schedulers only fire on whole game ticks (50 ms), so the returned ticker rejects
 * intervals it cannot honour instead of silently rounding them: [Ticker.repeating] throws
 * [IllegalArgumentException] unless the interval is a positive whole number of ticks
 * (`1.seconds`, `500.milliseconds`, and `3.ticks` are all fine; `75.milliseconds` is not).
 *
 * Tasks are not bound to the plugin lifecycle beyond what Paper provides: the server cancels
 * them when the plugin disables.
 *
 * @sample io.github.lmliam.kotventure.paper.time.tickerSample
 */
public fun Plugin.ticker(): Ticker = GlobalRegionTicker(this)

/**
 * Creates a [Ticker] that runs repeating work in the region that owns [entity].
 *
 * Work runs on the entity's scheduler ([Entity.getScheduler]) and follows the entity across
 * regions and worlds, with the same behaviour on plain Paper and Folia. The returned ticker
 * keeps the whole-tick interval contract of [Plugin.ticker]: [Ticker.repeating] throws
 * [IllegalArgumentException] unless the interval is a positive whole number of ticks.
 *
 * [Ticker.repeating] throws [IllegalStateException] when [entity] has already been removed; when
 * the entity is removed while a task is scheduled, Paper retires (stops) that task. The server
 * cancels remaining tasks when the plugin disables.
 *
 * @sample io.github.lmliam.kotventure.paper.time.entityTickerSample
 */
public fun Plugin.ticker(entity: Entity): Ticker = EntityTicker(this, entity)

/**
 * Creates a [Ticker] that runs repeating work in the region containing [location].
 *
 * Work runs on the region scheduler ([Server.getRegionScheduler]) for [location]'s chunk, with
 * the same behaviour on plain Paper and Folia. The returned ticker keeps the whole-tick interval
 * contract of [Plugin.ticker]: [Ticker.repeating] throws [IllegalArgumentException] unless the
 * interval is a positive whole number of ticks.
 *
 * Tasks are not bound to the plugin lifecycle beyond what Paper provides: the server cancels
 * them when the plugin disables.
 *
 * @sample io.github.lmliam.kotventure.paper.time.locationTickerSample
 */
public fun Plugin.ticker(location: Location): Ticker = RegionTicker(this, location)
