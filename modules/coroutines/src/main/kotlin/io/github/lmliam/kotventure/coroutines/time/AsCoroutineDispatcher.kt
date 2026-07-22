package io.github.lmliam.kotventure.coroutines.time

import io.github.lmliam.kotventure.core.time.Ticker
import kotlinx.coroutines.MainCoroutineDispatcher

/**
 * Returns a [MainCoroutineDispatcher] that runs coroutines on this ticker.
 *
 * Each coroutine body, each resumption, and each `delay` runs on the ticker. Thus, a body can do the
 * operations that the ticker's own context permits, and no more. A global Paper ticker gives the
 * global tick context. An entity ticker or a location ticker gives only the region that it follows.
 *
 * The dispatcher also controls `delay`, `withTimeout`, and `withTimeoutOrNull`. It schedules each
 * of them with [Ticker.once], and it cancels the schedule when the coroutine cancels.
 *
 * This dispatcher always waits for the next opportunity of the ticker, even when the caller is
 * already on the ticker's thread. Use [MainCoroutineDispatcher.immediate] to remove that wait:
 *
 * ```kotlin
 * val tick = plugin.ticker().asCoroutineDispatcher()
 *
 * launch(tick) { }                  // always starts on the next tick
 * withContext(tick.immediate) { }   // starts now if the caller owns the ticker's thread
 * ```
 *
 * The ticker keeps its own delay contract. A tick-based ticker accepts only an exact number of
 * ticks. Therefore, `delay(10.milliseconds)` throws [IllegalArgumentException]. Write each delay
 * with [ticks][io.github.lmliam.kotventure.core.time.ticks], because a tick duration is always
 * exact:
 *
 * ```kotlin
 * delay(5.ticks)   // 250 ms, correct on every ticker
 * delay(1.seconds) // 20 ticks, also correct
 * ```
 *
 * @sample io.github.lmliam.kotventure.coroutines.time.tickerDispatcherSample
 * @sample io.github.lmliam.kotventure.coroutines.time.tickerDispatcherAnimationSample
 * @sample io.github.lmliam.kotventure.coroutines.time.immediateTickerDispatcherSample
 */
public fun Ticker.asCoroutineDispatcher(): MainCoroutineDispatcher = TickerDispatcher(this)
