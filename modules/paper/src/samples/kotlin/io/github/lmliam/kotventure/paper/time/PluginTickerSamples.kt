package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.TickerTask
import io.github.lmliam.kotventure.core.time.ticks
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

internal fun tickerSample(plugin: Plugin): TickerTask {
    val ticker = plugin.ticker()
    return ticker.every(20.ticks) { plugin.logger.info("one second passed") }
}

internal fun afterTickerSample(plugin: Plugin): TickerTask {
    val ticker = plugin.ticker()
    return ticker.after(60.ticks) { plugin.logger.info("three seconds passed") }
}

internal fun entityTickerSample(
    plugin: Plugin,
    entity: Entity,
): TickerTask {
    val ticker = plugin.ticker(entity)
    return ticker.every(20.ticks) { plugin.logger.info("entity tick passed") }
}

internal fun locationTickerSample(
    plugin: Plugin,
    location: Location,
): TickerTask {
    val ticker = plugin.ticker(location)
    return ticker.every(20.ticks) { plugin.logger.info("region tick passed") }
}
