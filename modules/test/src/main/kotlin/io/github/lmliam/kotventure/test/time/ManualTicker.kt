package io.github.lmliam.kotventure.test.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import kotlin.time.Duration

/**
 * Deterministic [Ticker] for unit tests: time advances only via [advance], which fires every
 * due repeating task in schedule order.
 *
 * Consumers can drive countdown bars and other ticker-backed surfaces without a server clock.
 */
public class ManualTicker : Ticker {
    private var now: Duration = Duration.ZERO
    private val tasks: MutableList<ManualTask> = mutableListOf()

    /**
     * Advances the virtual clock by [duration] and runs every task whose next fire time falls
     * at or before the new clock value, in chronological order. A task that remains active is
     * rescheduled by adding its interval to its previous fire time (phase is preserved).
     *
     * @throws IllegalArgumentException when [duration] is negative.
     */
    public fun advance(duration: Duration) {
        require(duration >= Duration.ZERO) { "advance duration must not be negative, got $duration." }
        val end = now + duration
        while (true) {
            tasks.removeAll { !it.isActive }
            val next = tasks.minByOrNull { it.nextFire } ?: break
            if (next.nextFire > end) {
                break
            }
            now = next.nextFire
            next.fire()
        }
        now = end
    }

    override fun repeating(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask {
        require(interval.isPositive()) { "repeating interval must be positive, got $interval." }
        val task = ManualTask(nextFire = now + interval, interval = interval, action = action)
        tasks += task
        return task
    }

    private class ManualTask(
        var nextFire: Duration,
        private val interval: Duration,
        private val action: () -> Unit,
    ) : TickerTask {
        var isActive: Boolean = true
            private set

        fun fire() {
            if (!isActive) {
                return
            }
            action()
            if (isActive) {
                nextFire += interval
            }
        }

        override fun cancel() {
            isActive = false
        }
    }
}
