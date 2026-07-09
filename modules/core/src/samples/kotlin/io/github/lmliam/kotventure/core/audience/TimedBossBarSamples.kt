package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.ticks
import kotlin.time.Duration.Companion.seconds

internal fun timedBossBarSample(ticker: Ticker) {
    // Platform code supplies the ticker (e.g. paperTicker(plugin)); tests use ManualTicker.
    val player = emptyAudience()

    context(ticker) {
        val meteor =
            player.bossBar(over = 30.seconds) {
                name { remaining -> text("Meteor in ${remaining.inWholeSeconds}s") }
                color(red)
                overlay(notched10)
                progress(from = 1f, to = 0f)
                every(1.ticks)
                onFinish { /* auto-hidden from all tracked viewers */ }
            }

        val spectator = emptyAudience()
        spectator.show(meteor)
        spectator.hide(meteor)

        meteor.pause()
        meteor.resume()
        meteor.cancel()
    }
}
