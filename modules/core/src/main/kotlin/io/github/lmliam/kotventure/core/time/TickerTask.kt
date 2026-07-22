package io.github.lmliam.kotventure.core.time

/**
 * A scheduled unit of work produced by [Ticker.repeating] or [Ticker.once].
 *
 * Cancellation is idempotent. It prevents future invocations but does not have to interrupt an action that is already
 * running. For a task from [Ticker.once], cancellation before the delay elapses prevents the single run.
 */
public interface TickerTask {
    /**
     * Prevents future invocations of this task.
     *
     * Safe to call more than once.
     */
    public fun cancel()
}
