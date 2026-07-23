package io.github.lmliam.kotventure.core.time

import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.text.text

internal fun tickerAfterSample(ticker: Ticker): TickerTask {
    val player = emptyAudience()

    return ticker.after(20.ticks) { player.message { text("One second passed.") } }
}

internal fun tickerNextOpportunitySample(ticker: Ticker): TickerTask {
    val player = emptyAudience()

    return ticker.after { player.message { text("The ticker reached its next opportunity.") } }
}

internal fun tickerIsCurrentSample(ticker: Ticker) {
    val player = emptyAudience()
    val greeting = { player.message { text("Hello.") } }

    if (ticker.isCurrent) {
        greeting()
    } else {
        ticker.after(action = greeting)
    }
}
