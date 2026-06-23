package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component

/**
 * Builds an Adventure block NBT [Component] from a Kotventure DSL block.
 */
public fun blockNbt(
    pos: BlockNBTComponent.Pos,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
): Component = buildBlockNbtComponent(pos, nbtPath, init)

internal fun buildBlockNbtComponent(
    pos: BlockNBTComponent.Pos,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
): Component =
    NbtComponentBuilder<BlockNBTComponent, BlockNBTComponent.Builder>(
        Component.blockNBT().pos(pos).nbtPath(nbtPath),
    ).apply(init).build()

/**
 * Appends a nested block NBT child with [pos] and [nbtPath].
 */
public fun ComponentScope.blockNbt(
    pos: BlockNBTComponent.Pos,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
) {
    append(buildBlockNbtComponent(pos, nbtPath, init))
}
