package io.github.lmliam.kotventure.core.nbt

import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component

/**
 * Builds an Adventure block NBT [Component] from a Kotventure DSL block.
 */
public fun blockNbt(
    pos: BlockNBTComponent.Pos,
    nbtPath: String,
    init: BlockNbtScope.() -> Unit = {},
): Component = BlockNbtComponentBuilder(pos, nbtPath).apply(init).build()
