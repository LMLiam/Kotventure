package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import io.github.lmliam.kotventure.core.time.ticks
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun timedBossBarSample() {
    // Platform code supplies a real ticker (e.g. paperTicker(plugin)); tests use ManualTicker.
    val ticker =
        object : Ticker {
            override fun repeating(
                interval: Duration,
                action: () -> Unit,
            ): TickerTask =
                object : TickerTask {
                    override fun cancel() {}
                }
        }
    val player = emptyAudience()

    context(ticker) {
        player.bossBar(over = 30.seconds) {
            name { remaining -> text("Meteor in ${remaining.inWholeSeconds}s") }
            color(red)
            overlay(notched10)
            progress(from = 1f, to = 0f)
            every(1.ticks)
            onFinish { /* auto-hidden from all tracked viewers */ }
        }
    }
}
