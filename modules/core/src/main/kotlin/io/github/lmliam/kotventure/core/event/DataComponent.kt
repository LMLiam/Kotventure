package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.DataComponentValue

/**
 * Returns the Adventure marker that removes a data component from an item hover payload.
 */
public fun removed(): DataComponentValue.Removed = DataComponentValue.removed()
