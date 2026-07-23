package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.audience.bossBar
import io.github.lmliam.kotventure.core.time.Ticker
import net.kyori.adventure.audience.Audience
import kotlin.time.Duration

internal fun timedBossBar(
    ticker: Ticker,
    audience: Audience,
    over: Duration,
    init: TimedBossBarScope.() -> Unit,
): TimedBossBar =
    context(ticker) {
        audience.bossBar(over, init)
    }
