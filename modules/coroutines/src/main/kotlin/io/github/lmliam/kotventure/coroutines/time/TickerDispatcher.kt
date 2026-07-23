package io.github.lmliam.kotventure.coroutines.time

import io.github.lmliam.kotventure.core.time.Ticker
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Delay
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * A [MainCoroutineDispatcher] backed by [Ticker].
 *
 * Dispatch always waits for a ticker opportunity, including from the
 * ticker's context.
 * Its [immediate] variant runs work in place from that context.
 * Both variants schedule delays and timeouts through [ticker].
 */
@OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
internal class TickerDispatcher(
    internal val ticker: Ticker,
) : MainCoroutineDispatcher(),
    Delay {
    override val immediate: MainCoroutineDispatcher = ImmediateTickerDispatcher(this)

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable,
    ) {
        ticker.after(action = block::run)
    }

    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: CancellableContinuation<Unit>,
    ) {
        val task =
            ticker.after(timeMillis.milliseconds) {
                with(continuation) { resumeUndispatched(Unit) }
            }
        continuation.invokeOnCancellation { task.cancel() }
    }

    override fun invokeOnTimeout(
        timeMillis: Long,
        block: Runnable,
        context: CoroutineContext,
    ): DisposableHandle {
        val task = ticker.after(timeMillis.milliseconds, block::run)
        return DisposableHandle { task.cancel() }
    }

    override fun toString(): String = "Ticker.asCoroutineDispatcher($ticker)"
}
