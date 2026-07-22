package io.github.lmliam.kotventure.coroutines.time

import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlin.coroutines.CoroutineContext

/**
 * Runs coroutines on the ticker of [dispatching] and skips the wait when it can.
 *
 * A caller that already runs in the ticker's context continues in place. Each other caller waits for
 * the next opportunity of the ticker, exactly as [dispatching] does.
 */
@OptIn(InternalCoroutinesApi::class)
internal class ImmediateTickerDispatcher(
    private val dispatching: TickerDispatcher,
) : MainCoroutineDispatcher(),
    Delay by dispatching {
    override val immediate: MainCoroutineDispatcher
        get() = this

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = !dispatching.ticker.isCurrent

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable,
    ) {
        dispatching.dispatch(context, block)
    }

    override fun toString(): String = "$dispatching.immediate"
}
