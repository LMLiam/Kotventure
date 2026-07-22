package io.github.lmliam.kotventure.coroutines.prompt

import kotlinx.coroutines.CancellableContinuation
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * Resumes an active prompt one time.
 *
 * An answer or cancellation closes the prompt. Later answers do nothing.
 */
internal class PendingPrompt<T>(
    private val continuation: CancellableContinuation<T>,
) {
    private val active = AtomicBoolean(true)

    internal fun cancel() {
        active.set(false)
    }

    internal fun answer(value: T) {
        if (active.compareAndSet(true, false)) {
            continuation.resume(value)
        }
    }
}
