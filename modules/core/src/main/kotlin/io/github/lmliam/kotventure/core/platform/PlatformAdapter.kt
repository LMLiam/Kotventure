package io.github.lmliam.kotventure.core.platform

import net.kyori.adventure.audience.Audience

/**
 * Bridges platform-specific audiences and scheduling into Kotventure.
 */
public interface PlatformAdapter {
    /**
     * Unique registration name for this platform adapter.
     */
    public val name: String

    /**
     * Returns the console audience for the platform.
     */
    public fun console(): Audience

    /**
     * Returns the currently connected player audiences for the platform.
     */
    public fun players(): Iterable<Audience>

    /**
     * Returns the aggregate audience representing every target on the platform.
     */
    public fun all(): Audience

    /**
     * Schedules [task] on the platform and returns a handle that can cancel it.
     */
    public fun schedule(task: () -> Unit): PlatformTask
}
