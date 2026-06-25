package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.StorageNBTComponent

/**
 * Builds a storage-NBT [Component] — text the client resolves from command-storage NBT under a key.
 *
 * ```kotlin
 * val score = storageNbt(key("myplugin", "scores"), "top.player")
 * ```
 *
 * @param storage the command-storage key to read, e.g. from `key(...)`.
 * @param nbtPath the NBT path within that storage, such as `"top.player"`.
 * @param init sets `interpret`/`separator` and appends any children.
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
 * Appends a storage-NBT child to this scope, for use inside a `component { }` or other component block.
 *
 * @param storage the command-storage key to read, e.g. from `key(...)`.
 * @param nbtPath the NBT path within that storage, such as `"top.player"`.
 * @param init sets `interpret`/`separator` and appends any children.
 */
public fun ComponentScope.storageNbt(
    storage: Key,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
) {
    append(buildStorageNbtComponent(storage, nbtPath, init))
}
