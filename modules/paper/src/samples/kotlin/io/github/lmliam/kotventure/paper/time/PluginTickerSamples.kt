package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.TickerTask
import io.github.lmliam.kotventure.core.time.ticks
import org.bukkit.plugin.Plugin

internal fun tickerSample(plugin: Plugin): TickerTask {
    val ticker = plugin.ticker()
    return ticker.repeating(20.ticks) { plugin.logger.info("one second passed") }
}
