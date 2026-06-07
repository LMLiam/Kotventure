package io.github.lmliam.kotventure.core.animation

/**
 * Schedules and advances animation frames for Kotventure animation abstractions.
 */
public interface AnimationDriver {
    /**
     * Unique registration name for this animation driver.
     */
    public val name: String

    /**
     * Starts the animation identified by [animationId] and invokes [onTick] for scheduled frames.
     */
    public fun start(
        animationId: String,
        onTick: () -> Unit,
    ): Unit

    /**
     * Advances the animation identified by [animationId] once.
     */
    public fun tick(animationId: String): Unit

    /**
     * Stops the animation identified by [animationId].
     */
    public fun stop(animationId: String): Unit
}
