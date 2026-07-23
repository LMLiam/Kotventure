package io.github.lmliam.kotventure.coroutines.time

import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

/**
 * Runs coroutines on the ticker of [dispatcher] and skips the wait when it can.
 *
 * A caller that already runs in the ticker's context continues in place. Each other caller waits for
 * the next opportunity of the ticker, exactly as [dispatcher] does.
 */
@OptIn(InternalCoroutinesApi::class)
internal class ImmediateTickerDispatcher(
    private val dispatcher: TickerDispatcher,
) : MainCoroutineDispatcher(),
    Delay by dispatcher {
    override val immediate: MainCoroutineDispatcher
        get() = this

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = !dispatcher.ticker.isCurrent

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable,
    ) = dispatcher.dispatch(context, block)

    override fun toString(): String = "$dispatcher.immediate"
}
