package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.addChild
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
): Component = BlockNbtComponentBuilder(pos, nbtPath).apply(init).build()

/**
 * Appends a nested block NBT child with [pos] and [nbtPath].
 */
public fun ComponentScope.blockNbt(
    pos: BlockNBTComponent.Pos,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
) {
    addChild(buildBlockNbtComponent(pos, nbtPath, init))
}
