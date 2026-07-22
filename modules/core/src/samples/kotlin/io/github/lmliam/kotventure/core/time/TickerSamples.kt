package io.github.lmliam.kotventure.core.time

import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.text.text

internal fun tickerOnceSample(ticker: Ticker): TickerTask {
    // Platform code supplies the ticker. For example, Paper code can use plugin.ticker().
    val player = emptyAudience()

    return ticker.once(20.ticks) { player.message { text("One second passed.") } }
}

internal fun tickerNextOpportunitySample(ticker: Ticker): TickerTask {
    val player = emptyAudience()

    // Without a delay, the ticker runs the action at its next opportunity.
    return ticker.once { player.message { text("The ticker reached its next opportunity.") } }
}

internal fun tickerOwnsCurrentThreadSample(ticker: Ticker) {
    val player = emptyAudience()
    val greeting = { player.message { text("Hello.") } }

    if (ticker.ownsCurrentThread) {
        greeting()
    } else {
        ticker.once { greeting() }
    }
}
