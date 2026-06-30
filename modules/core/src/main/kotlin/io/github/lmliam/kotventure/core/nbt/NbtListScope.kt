package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Scope for building a homogeneous list of NBT compounds (e.g. `Lore`, enchantments, attribute
 * modifiers).
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@KotventureDslMarker
public interface NbtListScope {
    /** Appends a compound element built by [init]. */
    public fun element(init: NbtCompoundScope.() -> Unit)
}
