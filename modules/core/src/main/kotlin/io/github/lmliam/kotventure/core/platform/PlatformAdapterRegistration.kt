package io.github.lmliam.kotventure.core.platform

import io.github.lmliam.kotventure.core.registry.AdventureDsl

/**
 * Registers this platform adapter as the active platform adapter and returns it.
 *
 * Registering another adapter replaces the previous registration; only one platform adapter is
 * active at a time.
 */
public fun <T : PlatformAdapter> T.register(): T =
    apply {
        AdventureDsl.registerPlatformAdapter(this)
    }
