package io.github.lmliam.kotventure.coroutines.time

import io.github.lmliam.kotventure.core.time.Ticker
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Delay
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * Runs coroutines on [ticker] and always waits for the next opportunity of the ticker.
 *
 * [immediate] gives the variant that runs the work at once when the caller already runs in the
 * ticker's context. Both variants share this delay implementation.
 */
@OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
internal class TickerDispatcher(
    val ticker: Ticker,
) : MainCoroutineDispatcher(),
    Delay {
    override val immediate: MainCoroutineDispatcher = ImmediateTickerDispatcher(this)

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable,
    ) {
        ticker.after { block.run() }
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
        val task = ticker.after(timeMillis.milliseconds) { block.run() }
        return DisposableHandle { task.cancel() }
    }

    override fun toString(): String = "Ticker.asCoroutineDispatcher($ticker)"
}
