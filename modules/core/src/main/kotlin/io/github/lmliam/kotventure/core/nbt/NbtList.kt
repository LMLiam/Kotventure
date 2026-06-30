package io.github.lmliam.kotventure.core.nbt

/**
 * A homogeneous NBT list (`TAG_List`).
 *
 * Every element shares one NBT type — Minecraft rejects mixed-type lists. Homogeneity is guaranteed
 * by construction: build one with [listOf] (scalars) or [listOf] with an element block (compounds),
 * then assign it with [NbtCompoundScope.eq].
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
public class NbtList internal constructor(
    internal val elements: List<NbtValue>,
)
