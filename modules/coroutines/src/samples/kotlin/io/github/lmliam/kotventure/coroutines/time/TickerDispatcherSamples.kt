package io.github.lmliam.kotventure.coroutines.time

import io.github.lmliam.kotventure.core.audience.actionBar
import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.ticks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun tickerDispatcherSample(ticker: Ticker) {
    // Platform code supplies the ticker. For example, Paper code can use plugin.ticker().
    val pluginScope = CoroutineScope(SupervisorJob() + ticker.asCoroutineDispatcher())
    val player = emptyAudience()

    pluginScope.launch {
        repeat(3) { count ->
            player.actionBar { text("Teleport in ${3 - count}") }
            delay(20.ticks)
        }
        player.message { text("Teleported.") }
    }
}

internal fun tickerDispatcherAnimationSample(ticker: Ticker) {
    val pluginScope = CoroutineScope(SupervisorJob() + ticker.asCoroutineDispatcher())
    val player = emptyAudience()

    // A tick duration is always exact. Thus, it satisfies every ticker.
    pluginScope.launch {
        "Ready".forEachIndexed { index, letter ->
            player.actionBar { text("Ready".take(index) + letter) }
            delay(2.ticks)
        }
    }
}

internal suspend fun immediateTickerDispatcherSample(ticker: Ticker) {
    val player = emptyAudience()

    // A caller that already owns the ticker's thread continues without a wait.
    withContext(ticker.asCoroutineDispatcher().immediate) {
        player.message { text("Welcome back.") }
    }
}
