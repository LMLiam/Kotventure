package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.Ticker
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

/**
 * Returns a [Ticker] for this plugin's global scheduler.
 *
 * The action runs on [Server.getGlobalRegionScheduler]. It runs on the main thread on Paper. It
 * runs on the global region thread on Folia.
 *
 * Paper schedulers use complete game ticks of 50 milliseconds. [Ticker.every] accepts only a
 * positive duration that is an exact number of ticks. It does not round the duration. For example,
 * `500.milliseconds` is valid and `75.milliseconds` is not valid. [Ticker.after] has the same rule
 * for a positive delay. A delay of zero selects the next tick.
 *
 * [Ticker.isCurrent] gives the result of `Server.isGlobalTickThread`.
 *
 * The first run occurs after one interval. Paper cancels remaining tasks when the plugin disables.
 *
 * @sample io.github.lmliam.kotventure.paper.time.tickerSample
 * @sample io.github.lmliam.kotventure.paper.time.afterTickerSample
 */
public fun Plugin.ticker(): Ticker = GlobalRegionTicker(this)

/**
 * Returns a [Ticker] that follows [entity].
 *
 * The action runs through [Entity.getScheduler] on the region that owns the entity. The scheduler
 * follows the entity when it moves between regions or worlds.
 *
 * This ticker has the exact whole-tick interval contract of [Plugin.ticker]. The first run occurs
 * after one interval. Paper stops the task when it retires the entity. Paper also cancels remaining
 * tasks when the plugin disables.
 *
 * [Ticker.every] and [Ticker.after] fail if Paper has already retired the entity scheduler.
 * [Ticker.isCurrent] gives the result of `Server.isOwnedByCurrentRegion` for the entity.
 *
 * @sample io.github.lmliam.kotventure.paper.time.entityTickerSample
 */
public fun Plugin.ticker(entity: Entity): Ticker = EntityTicker(this, entity)

/**
 * Returns a [Ticker] for the region that contains [location].
 *
 * Each [Ticker.every] and [Ticker.after] call reads the current world and chunk from [location].
 * The scheduled action then runs through [Server.getRegionScheduler] for that fixed region. The task
 * does not follow later changes to the location or movement of an entity.
 *
 * This ticker has the exact whole-tick interval contract of [Plugin.ticker]. The first run occurs
 * after one interval. Paper cancels remaining tasks when the plugin disables.
 * [Ticker.isCurrent] gives the result of `Server.isOwnedByCurrentRegion` for the location.
 *
 * @sample io.github.lmliam.kotventure.paper.time.locationTickerSample
 */
public fun Plugin.ticker(location: Location): Ticker = RegionTicker(this, location)
