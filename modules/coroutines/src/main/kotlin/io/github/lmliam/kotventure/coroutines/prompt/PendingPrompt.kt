package io.github.lmliam.kotventure.coroutines.prompt

import kotlinx.coroutines.CancellableContinuation
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * Resumes one awaiting prompt one time.
 *
 * The guard is necessary, because a second resume of a resumed continuation fails. A resume of a cancelled
 * continuation does nothing. Therefore, a cancelled prompt needs no other state.
 */
internal class PendingPrompt<T>(
    private val continuation: CancellableContinuation<T>,
) {
    private val answered = AtomicBoolean()

    internal fun answer(value: T) {
        if (answered.compareAndSet(false, true)) {
            continuation.resume(value)
        }
    }
}
