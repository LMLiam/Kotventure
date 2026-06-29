package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.selector.EntitySelector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent

/**
 * Builds an entity-NBT [Component] — text the client resolves from the NBT of entities matched by a selector.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.entityNbtSample
 *
 * @param selector the entity selector whose NBT is read, constructed via [io.github.lmliam.kotventure.core.selector.self] or friends.
 * @param nbtPath the NBT path within each entity, constructed via [nbtPath].
 * @param init sets [NbtScope.interpret]/[NbtScope.separator] and appends any children.
 */
public fun entityNbt(
    selector: EntitySelector,
    nbtPath: NbtPath,
    init: NbtScope.() -> Unit = {},
): Component = buildEntityNbtComponent(selector, nbtPath, init)

internal fun buildEntityNbtComponent(
    selector: EntitySelector,
    nbtPath: NbtPath,
    init: NbtScope.() -> Unit = {},
): Component =
    NbtComponentBuilder(
        Component.entityNBT().selector(selector.asString()).nbtPath(nbtPath.asString()),
    ).apply(init).build()

/**
 * Appends an entity-NBT child to this scope, for use inside a `component { }` or other component block.
 *
 * @param selector the entity selector whose NBT is read, constructed via [io.github.lmliam.kotventure.core.selector.self] or friends.
 * @param nbtPath the NBT path within each entity, constructed via [nbtPath].
 * @param init sets [NbtScope.interpret]/[NbtScope.separator] and appends any children.
 */
public fun ComponentScope.entityNbt(
    selector: EntitySelector,
    nbtPath: NbtPath,
    init: NbtScope.() -> Unit = {},
) {
    append(buildEntityNbtComponent(selector, nbtPath, init))
}
