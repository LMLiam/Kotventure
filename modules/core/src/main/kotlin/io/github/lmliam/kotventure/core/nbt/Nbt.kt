package io.github.lmliam.kotventure.core.nbt

import net.kyori.adventure.nbt.api.BinaryTagHolder

/**
 * Creates a [BinaryTagHolder] from a compound NBT block.
 *
 * The function renders the compound as SNBT. It rejects duplicate keys. This package models SNBT text for selector
 * arguments and NBT components. It is not a binary NBT library.
 *
 * @throws IllegalStateException when [init] sets the same key more than one time in one compound.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtSample
 */
public fun nbt(init: NbtCompoundScope.() -> Unit): BinaryTagHolder {
    val compound = NbtCompoundBuilder().apply(init).build()
    return BinaryTagHolder.binaryTagHolder(renderCompound(compound))
}

/**
 * Wraps the raw [snbt] string in a [BinaryTagHolder].
 *
 * This function does not parse or validate the string. Use it when the compound DSL cannot express the required
 * SNBT.
 */
public fun nbt(snbt: String): BinaryTagHolder = BinaryTagHolder.binaryTagHolder(snbt)
