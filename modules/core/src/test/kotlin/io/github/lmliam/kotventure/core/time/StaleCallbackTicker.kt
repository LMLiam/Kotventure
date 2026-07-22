package io.github.lmliam.kotventure.core.time

import kotlin.time.Duration

internal class StaleCallbackTicker : Ticker {
    private val actions = mutableListOf<() -> Unit>()

    override val isCurrent: Boolean = false

    val scheduledCount: Int
        get() = actions.size

    override fun every(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask {
        actions += action
        return StaleTask
    }

    override fun after(
        delay: Duration,
        action: () -> Unit,
    ): TickerTask = error("This ticker records recurring work only.")

    fun run(index: Int) {
        actions[index]()
    }

    private object StaleTask : TickerTask {
        override fun cancel(): Unit = Unit
    }
}
