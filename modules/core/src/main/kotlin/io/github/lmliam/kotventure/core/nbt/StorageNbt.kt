package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.StorageNBTComponent

/**
 * Builds an Adventure storage NBT [Component] from a Kotventure DSL block.
 */
public fun storageNbt(
    storage: Key,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
): Component = buildStorageNbtComponent(storage, nbtPath, init)

internal fun buildStorageNbtComponent(
    storage: Key,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
): Component =
    NbtComponentBuilder<StorageNBTComponent, StorageNBTComponent.Builder>(
        Component.storageNBT().storage(storage).nbtPath(nbtPath),
    ).apply(init).build()

/**
 * Appends a nested storage NBT child with [storage] and [nbtPath].
 */
public fun ComponentScope.storageNbt(
    storage: Key,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
) {
    append(buildStorageNbtComponent(storage, nbtPath, init))
}
