package io.github.lmliam.kotventure.core.time

import kotlin.time.Duration

/**
 * Schedules repeating work on a platform clock.
 *
 * Core ships no default implementation — platforms provide real tickers (for example a Paper
 * scheduler adapter) and the `test` module ships a deterministic `ManualTicker` for unit tests.
 * This interface is the basis of the animation ticker. Later animation work extends it instead of replacing it.
 *
 * @see TickerTask
 */
public interface Ticker {
    /**
     * Schedules [action] to run repeatedly every [interval], starting after the first interval
     * elapses.
     *
     * @param interval positive delay between successive invocations.
     * @param action work to run on the ticker's thread each interval.
     * @return a handle that can [TickerTask.cancel] further invocations.
     * @throws IllegalArgumentException when [interval] is not positive.
     */
    public fun repeating(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask
}
