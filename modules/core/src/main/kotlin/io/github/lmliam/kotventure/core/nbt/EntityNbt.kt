package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.addChild
import net.kyori.adventure.text.Component

/**
 * Builds an Adventure entity NBT [Component] from a Kotventure DSL block.
 */
public fun entityNbt(
    selector: String,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
): Component = buildEntityNbtComponent(selector, nbtPath, init)

internal fun buildEntityNbtComponent(
    selector: String,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
): Component = EntityNbtComponentBuilder(selector, nbtPath).apply(init).build()

/**
 * Appends a nested entity NBT child with [selector] and [nbtPath].
 */
public fun ComponentScope.entityNbt(
    selector: String,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
) {
    addChild(buildEntityNbtComponent(selector, nbtPath, init))
}
