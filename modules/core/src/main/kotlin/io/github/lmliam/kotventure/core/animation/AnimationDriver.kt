package io.github.lmliam.kotventure.core.animation

/**
 * Schedules and advances animation frames for Kotventure animation abstractions.
 *
 * Implementations own the lifecycle contract for each [animationId]: [start] registers
 * scheduled execution, [tick] performs a synchronous manual frame, and [stop] cancels
 * scheduled execution.
 */
public interface AnimationDriver {
    /**
     * Unique registration name for this animation driver.
     */
    public val name: String

    /**
     * Starts or replaces the animation identified by [animationId] and invokes [onTick]
     * for scheduled frames until [stop] is called.
     *
     * Calling [start] again with the same [animationId] replaces the previous schedule and
     * callback. Implementations must not run callbacks for the same [animationId] concurrently.
     */
    public fun start(
        animationId: String,
        onTick: () -> Unit,
    )

    /**
     * Advances the animation identified by [animationId] once, synchronously.
     *
     * [tick] may be used with an animation that is already running; in that case it invokes
     * the callback registered by [start] exactly once in addition to any scheduled frames.
     * Calling [tick] for an unknown [animationId] is a no-op.
     */
    public fun tick(animationId: String)

    /**
     * Stops the animation identified by [animationId].
     *
     * Calling [stop] for an unknown [animationId] is a no-op.
     */
    public fun stop(animationId: String)
}
