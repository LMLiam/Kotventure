package io.github.lmliam.kotventure.core.animation

import io.github.lmliam.kotventure.core.registry.AdventureDsl

/**
 * Registers this animation driver with Kotventure's startup registry and returns it.
 *
 * Registering another driver with the same name replaces the previous registration.
 *
 * @throws IllegalArgumentException when the driver name is blank.
 */
public fun <T : AnimationDriver> T.register(): T =
    apply {
        AdventureDsl.registerAnimationDriver(this)
    }
