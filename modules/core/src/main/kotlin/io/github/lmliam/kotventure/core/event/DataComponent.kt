package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.DataComponentValue

/**
 * Returns a [DataComponentValue.Removed] marker indicating this data component should be removed.
 */
public fun removed(): DataComponentValue.Removed = DataComponentValue.removed()
