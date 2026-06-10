package io.github.lmliam.kotventure.core.nbt

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.StorageNBTComponent

internal class StorageNbtComponentBuilder(
    storage: Key,
    nbtPath: String,
) : NbtComponentBuilder<StorageNBTComponent, StorageNBTComponent.Builder>(
        Component
            .storageNBT()
            .storage(storage)
            .nbtPath(nbtPath),
    ),
    StorageNbtScope
