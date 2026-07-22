package io.github.lmliam.kotventure.coroutines.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import io.github.lmliam.kotventure.test.time.ManualTicker
import kotlin.time.Duration

/**
 * Records what a dispatcher asks [delegate] to do.
 *
 * This ticker shows each one-shot delay verbatim and counts each cancellation. It does not change
 * the timing.
 */
internal class RecordingTicker(
    private val delegate: ManualTicker,
) : Ticker by delegate {
    val delays: List<Duration>
        field = mutableListOf<Duration>()

    var cancellations: Int = 0
        private set

    /** Advances the virtual time of [delegate]. */
    fun advance(duration: Duration) {
        delegate.advance(duration)
    }

    override fun once(
        delay: Duration,
        action: () -> Unit,
    ): TickerTask {
        delays += delay
        val task = delegate.once(delay, action)
        return object : TickerTask {
            override fun cancel() {
                cancellations++
                task.cancel()
            }
        }
    }
}
