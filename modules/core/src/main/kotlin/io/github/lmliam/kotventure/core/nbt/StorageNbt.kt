package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.StorageNBTComponent

/**
 * Creates a storage-NBT [Component]. The client resolves its text from command-storage NBT under [storage]. This
 * function only creates the component. It does not send the component to an audience.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.storageNbtSample
 *
 * @param storage the command-storage key to read.
 * @param nbtPath the NBT path within that storage, constructed via [nbtPath].
 * @param init sets [NbtScope.interpret]/[NbtScope.separator] and appends any children.
 */
public fun storageNbt(
    storage: Key,
    nbtPath: NbtPath,
    init: NbtScope.() -> Unit = {},
): Component = buildStorageNbtComponent(storage, nbtPath, init)

internal fun buildStorageNbtComponent(
    storage: Key,
    nbtPath: NbtPath,
    init: NbtScope.() -> Unit = {},
): Component =
    NbtComponentBuilder(
        Component.storageNBT().storage(storage).nbtPath(nbtPath.asString()),
    ).apply(init).build()

/**
 * Creates a storage-NBT component and appends it as the next child of this scope.
 *
 * @param storage the command-storage key to read.
 * @param nbtPath the NBT path within that storage, constructed via [nbtPath].
 * @param init sets [NbtScope.interpret]/[NbtScope.separator] and appends any children.
 */
public fun ComponentScope.storageNbt(
    storage: Key,
    nbtPath: NbtPath,
    init: NbtScope.() -> Unit = {},
) {
    append(buildStorageNbtComponent(storage, nbtPath, init))
}
