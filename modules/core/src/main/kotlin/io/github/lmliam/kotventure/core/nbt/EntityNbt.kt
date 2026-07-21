package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.selector.EntitySelector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent

/**
 * Creates an entity-NBT [Component]. The client resolves its text from the NBT of the entities that [selector]
 * matches. This function only creates the component. It does not send the component to an audience.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.entityNbtSample
 *
 * @param selector the entity selector whose NBT is read.
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
 * Creates an entity-NBT component and appends it as the next child of this scope.
 *
 * @param selector the entity selector whose NBT is read.
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
