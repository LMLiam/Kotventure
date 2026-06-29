package io.github.lmliam.kotventure.core.nbt

import net.kyori.adventure.nbt.api.BinaryTagHolder

/**
 * Builds a [BinaryTagHolder] from compound DSL calls, rendering the compound to SNBT.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtSample
 */
public fun nbt(init: NbtCompoundScope.() -> Unit): BinaryTagHolder {
    val compound = NbtCompoundBuilder().apply(init).build()
    return BinaryTagHolder.binaryTagHolder(renderCompound(compound))
}

/**
 * Wraps a raw SNBT [snbt] string as a [BinaryTagHolder].
 *
 * Use when SNBT cannot be expressed through the typed compound DSL.
 */
public fun nbt(snbt: String): BinaryTagHolder = BinaryTagHolder.binaryTagHolder(snbt)
