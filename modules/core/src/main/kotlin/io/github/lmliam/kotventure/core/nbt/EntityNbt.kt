package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent

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
): Component =
    NbtComponentBuilder<EntityNBTComponent, EntityNBTComponent.Builder>(
        Component.entityNBT().selector(selector).nbtPath(nbtPath),
    ).apply(init).build()

/**
 * Appends a nested entity NBT child with [selector] and [nbtPath].
 */
public fun ComponentScope.entityNbt(
    selector: String,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
) {
    append(buildEntityNbtComponent(selector, nbtPath, init))
}
