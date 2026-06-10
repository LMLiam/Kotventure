package io.github.lmliam.kotventure.core.nbt

import net.kyori.adventure.text.Component

/**
 * Builds an Adventure entity NBT [Component] from a Kotventure DSL block.
 */
public fun entityNbt(
    selector: String,
    nbtPath: String,
    init: EntityNbtScope.() -> Unit = {},
): Component = EntityNbtComponentBuilder(selector, nbtPath).apply(init).build()
