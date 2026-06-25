package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent

/**
 * Builds an entity-NBT [Component] — text the client resolves from the NBT of entities matched by a selector.
 *
 * ```kotlin
 * val health = entityNbt("@s", "Health")
 * ```
 *
 * @param selector the entity selector whose NBT is read, such as `"@s"` or `"@e[type=zombie,limit=1]"`.
 * @param nbtPath the NBT path within each entity, such as `"Health"`.
 * @param init sets [NbtScope.interpret]/[NbtScope.separator] and appends any children.
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
 * Appends an entity-NBT child to this scope, for use inside a `component { }` or other component block.
 *
 * @param selector the entity selector whose NBT is read, such as `"@s"`.
 * @param nbtPath the NBT path within each entity, such as `"Health"`.
 * @param init sets [NbtScope.interpret]/[NbtScope.separator] and appends any children.
 */
public fun ComponentScope.entityNbt(
    selector: String,
    nbtPath: String,
    init: NbtScope.() -> Unit = {},
) {
    append(buildEntityNbtComponent(selector, nbtPath, init))
}
