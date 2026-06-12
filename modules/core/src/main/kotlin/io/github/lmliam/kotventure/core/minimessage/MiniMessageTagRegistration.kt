package io.github.lmliam.kotventure.core.minimessage

import io.github.lmliam.kotventure.core.registry.AdventureDsl

/**
 * Registers this MiniMessage tag provider with Kotventure's startup registry and returns it.
 *
 * Registering another provider with the same name replaces the previous registration.
 */
public fun <T : MiniMessageTagProvider> T.register(): T =
    apply {
        AdventureDsl.registerMiniMessageTag(this)
    }
