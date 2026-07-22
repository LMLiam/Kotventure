package io.github.lmliam.kotventure.core.time

/**
 * A cancellable execution scheduled by a [Ticker].
 *
 * Cancellation is idempotent. It prevents future invocations but does not have to interrupt an
 * action that is already running.
 */
public interface TickerTask {
    /** Prevents future invocations. */
    public fun cancel()
}
