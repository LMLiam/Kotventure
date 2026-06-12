package io.github.lmliam.kotventure.core.animation

import io.github.lmliam.kotventure.core.registry.AdventureDsl

/**
 * Returns the animation driver registered as [name], or null when none exists.
 */
public fun animationDriver(name: String): AnimationDriver? = AdventureDsl.animationDriver(name)
