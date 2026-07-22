package io.github.lmliam.kotventure.core.time

import kotlin.time.Duration

/**
 * Schedules work on a platform clock.
 *
 * Core has no default implementation. Platforms provide real tickers, such as a Paper
 * scheduler adapter, and the `test` module ships a deterministic `ManualTicker` for unit tests.
 * Implementations define the execution thread but must preserve the delay and cancellation contracts below.
 *
 * @see TickerTask
 */
public interface Ticker {
    /**
     * Shows if this ticker runs its work on the calling thread.
     *
     * A caller reads this property to prevent an unnecessary delay. If the value is `true`, the
     * caller is already in the correct context and can do the work immediately.
     *
     * A regional platform such as Folia gives region ownership here. The property does not show one
     * fixed thread.
     *
     * @sample io.github.lmliam.kotventure.core.time.tickerOwnsCurrentThreadSample
     */
    public val ownsCurrentThread: Boolean

    /**
     * Schedules [action] to run repeatedly every [interval], starting after the first interval
     * elapses.
     *
     * @param interval positive delay between successive invocations.
     * @param action work to run on the ticker's thread each interval.
     * @return a handle that can [TickerTask.cancel] future invocations.
     * @throws IllegalArgumentException when [interval] is not positive.
     */
    public fun repeating(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask

    /**
     * Schedules [action] to run one time after [delay].
     *
     * A [delay] of zero selects the next opportunity. On a tick-based platform, this is the next
     * tick. The ticker does not run [action] on the calling thread.
     *
     * Each implementation defines the delay granularity. A tick-based ticker accepts only an exact
     * number of ticks.
     *
     * @param delay time to wait before the run. Zero selects the next opportunity.
     * @param action work to run one time on the ticker's thread.
     * @return a handle that can [TickerTask.cancel] the run before it starts.
     * @throws IllegalArgumentException when [delay] is negative, or when the implementation cannot
     *   use the given precision.
     * @sample io.github.lmliam.kotventure.core.time.tickerOnceSample
     * @sample io.github.lmliam.kotventure.core.time.tickerNextOpportunitySample
     */
    public fun once(
        delay: Duration = Duration.ZERO,
        action: () -> Unit,
    ): TickerTask
}
