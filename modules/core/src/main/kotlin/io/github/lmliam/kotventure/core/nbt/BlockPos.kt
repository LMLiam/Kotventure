package io.github.lmliam.kotventure.core.nbt

import net.kyori.adventure.text.BlockNBTComponent

/**
 * Creates an absolute block-NBT position from the world coordinates [x], [y], and [z].
 */
public fun blockPos(
    x: Int,
    y: Int,
    z: Int,
): BlockNBTComponent.Pos =
    BlockNBTComponent.WorldPos.worldPos(
        BlockNBTComponent.WorldPos.Coordinate.absolute(x),
        BlockNBTComponent.WorldPos.Coordinate.absolute(y),
        BlockNBTComponent.WorldPos.Coordinate.absolute(z),
    )

/**
 * Creates a block-NBT position relative to the current position.
 *
 * The default offset for each axis is zero.
 */
public fun relativeBlockPos(
    dx: Int = 0,
    dy: Int = 0,
    dz: Int = 0,
): BlockNBTComponent.Pos =
    BlockNBTComponent.WorldPos.worldPos(
        BlockNBTComponent.WorldPos.Coordinate.relative(dx),
        BlockNBTComponent.WorldPos.Coordinate.relative(dy),
        BlockNBTComponent.WorldPos.Coordinate.relative(dz),
    )

/**
 * Parses [coords] as an Adventure block-NBT position.
 *
 * This function accepts the coordinate syntax that [BlockNBTComponent.Pos.fromString] accepts.
 *
 * @throws IllegalArgumentException when [coords] is not a valid Adventure block position.
 */
public fun blockPos(coords: String): BlockNBTComponent.Pos = BlockNBTComponent.Pos.fromString(coords)
