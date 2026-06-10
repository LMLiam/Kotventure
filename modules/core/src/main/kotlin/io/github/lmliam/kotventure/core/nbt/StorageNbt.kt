package io.github.lmliam.kotventure.core.nbt

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * Builds an Adventure storage NBT [Component] from a Kotventure DSL block.
 */
public fun storageNbt(
    storage: Key,
    nbtPath: String,
    init: StorageNbtScope.() -> Unit = {},
): Component = StorageNbtComponentBuilder(storage, nbtPath).apply(init).build()
