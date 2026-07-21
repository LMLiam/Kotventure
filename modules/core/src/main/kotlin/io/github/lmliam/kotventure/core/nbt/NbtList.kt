package io.github.lmliam.kotventure.core.nbt

/**
 * An NBT list value (`TAG_List`).
 *
 * Minecraft requires all elements of an NBT list to have the same type. The scalar [list] overload accepts mixed
 * supported Kotlin types and does not enforce this rule. Supply values of one NBT type. The compound-block overload
 * always creates a list of compounds.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
public class NbtList internal constructor(
    internal val elements: List<NbtValue>,
)
