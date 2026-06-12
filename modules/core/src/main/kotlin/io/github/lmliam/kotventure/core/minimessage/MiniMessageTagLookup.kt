package io.github.lmliam.kotventure.core.minimessage

import io.github.lmliam.kotventure.core.registry.AdventureDsl

/**
 * Returns the MiniMessage tag provider registered as [name], or null when none exists.
 */
public fun miniMessageTag(name: String): MiniMessageTagProvider? = AdventureDsl.miniMessageTag(name)
