package io.github.lmliam.kotventure.core.platform

import io.github.lmliam.kotventure.core.registry.AdventureDsl

/**
 * Returns the active platform adapter, or null when no platform has registered one.
 */
public fun platformAdapter(): PlatformAdapter? = AdventureDsl.platformAdapter()
