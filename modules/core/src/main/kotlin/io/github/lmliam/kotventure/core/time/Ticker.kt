package io.github.lmliam.kotventure.core.time

import kotlin.time.Duration

/**
 * Schedules work for a platform execution context.
 *
 * An implementation can represent a thread, a region, or another platform-defined context.
 * Implementations define supported delay precision.
 *
 * @see TickerTask
 */
public interface Ticker {
    /**
     * Shows whether the current execution context can run this ticker's work immediately.
     *
     * On a region-based platform, this checks ownership of the ticker's region rather than a fixed thread.
     *
     * @sample io.github.lmliam.kotventure.core.time.tickerIsCurrentSample
     */
    public val isCurrent: Boolean

    /**
     * Schedules [action] at each [interval].
     *
     * The first invocation occurs after one complete [interval].
     * This function returns before it invokes [action].
     * An implementation must not invoke [action] during this function call.
     *
     * The returned task controls the scheduled invocations.
     * Cancellation prevents future invocations.
     * Cancellation does not have to interrupt an invocation that is already running.
     *
     * [interval] must be positive.
     * An implementation must reject [interval] when its precision is unsupported.
     *
     * @throws IllegalArgumentException when [interval] is not positive or its precision is unsupported.
     */
    public fun every(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask

    /**
     * Schedules [action] once after [delay].
     *
     * A zero delay selects the ticker's next opportunity; the ticker must not invoke [action] inline.
     *
     * @throws IllegalArgumentException when [delay] is negative or its precision is unsupported.
     * @sample io.github.lmliam.kotventure.core.time.tickerAfterSample
     * @sample io.github.lmliam.kotventure.core.time.tickerNextOpportunitySample
     */
    public fun after(
        delay: Duration = Duration.ZERO,
        action: () -> Unit,
    ): TickerTask
}
