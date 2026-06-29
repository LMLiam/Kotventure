package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component

/**
 * Builds a block-NBT [Component] — text the client resolves from a block entity's NBT data at a position.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.blockNbtSample
 *
 * @param pos the block position to read, e.g. from [blockPos].
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
 * Appends a block-NBT child to this scope, for use inside a `component { }` or other component block.
 *
 * @param pos the block position to read, e.g. from [blockPos].
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
