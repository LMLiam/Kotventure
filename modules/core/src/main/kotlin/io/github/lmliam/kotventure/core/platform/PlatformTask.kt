package io.github.lmliam.kotventure.core.platform

/**
 * Handle for a scheduled platform task.
 */
public fun interface PlatformTask {
    /**
     * Cancels the scheduled task.
     */
    public fun cancel(): Unit
}
