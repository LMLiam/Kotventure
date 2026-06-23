package io.github.lmliam.kotventure.core.nbt

import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component

internal class BlockNbtComponentBuilder(
    pos: BlockNBTComponent.Pos,
    nbtPath: String,
) : NbtComponentBuilder<BlockNBTComponent, BlockNBTComponent.Builder>(
    Component.blockNBT().pos(pos).nbtPath(nbtPath),
)
