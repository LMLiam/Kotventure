package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component

/**
 * Creates a block-NBT [Component]. The client resolves its text from the NBT data of the block entity at [pos].
 * This function only creates the component. It does not send the component to an audience.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.blockNbtSample
 *
 * @param pos the block position to read, for example a position from [blockPos].
 * @param nbtPath the NBT path within the block entity, constructed via [nbtPath].
 * @param init sets [NbtScope.interpret]/[NbtScope.separator] and appends any children.
 */
public fun blockNbt(
    pos: BlockNBTComponent.Pos,
    nbtPath: NbtPath,
    init: NbtScope.() -> Unit = {},
): Component = buildBlockNbtComponent(pos, nbtPath, init)

internal fun buildBlockNbtComponent(
    pos: BlockNBTComponent.Pos,
    nbtPath: NbtPath,
    init: NbtScope.() -> Unit = {},
): Component =
    NbtComponentBuilder(
        Component.blockNBT().pos(pos).nbtPath(nbtPath.asString()),
    ).apply(init).build()

/**
 * Creates a block-NBT component and appends it as the next child of this scope.
 *
 * @param pos the block position to read, for example a position from [blockPos].
 * @param nbtPath the NBT path within the block entity, constructed via [nbtPath].
 * @param init sets [NbtScope.interpret]/[NbtScope.separator] and appends any children.
 */
public fun ComponentScope.blockNbt(
    pos: BlockNBTComponent.Pos,
    nbtPath: NbtPath,
    init: NbtScope.() -> Unit = {},
) {
    append(buildBlockNbtComponent(pos, nbtPath, init))
}
