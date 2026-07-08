package io.github.lmliam.kotventure.core.time

import net.kyori.adventure.util.Ticks
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

/**
 * Duration of this many Minecraft game ticks (20 ticks = 1 second).
 *
 * @sample io.github.lmliam.kotventure.core.time.ticksSample
 */
public val Int.ticks: Duration
    get() = Ticks.duration(toLong()).toKotlinDuration()

/**
 * Duration of this many Minecraft game ticks (20 ticks = 1 second).
 *
 * @sample io.github.lmliam.kotventure.core.time.ticksSample
 */
public val Long.ticks: Duration
    get() = Ticks.duration(this).toKotlinDuration()
