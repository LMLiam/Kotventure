package io.github.lmliam.kotventure.core.nbt

import net.kyori.adventure.text.BlockNBTComponent

/**
 * Builds an absolute block NBT position from world coordinates.
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
 * Builds a relative block NBT position from offsets from the current position.
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
 * Parses [coords] as an Adventure block NBT position.
 */
public fun blockPos(coords: String): BlockNBTComponent.Pos = BlockNBTComponent.Pos.fromString(coords)
