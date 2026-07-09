package io.github.lmliam.kotventure.core.time

/**
 * A scheduled unit of work produced by [Ticker.repeating].
 *
 * Cancelling is idempotent: a second [cancel] is a no-op.
 */
public interface TickerTask {
    /**
     * Stops this task so it will not fire again.
     *
     * Safe to call more than once.
     */
    public fun cancel()
}
