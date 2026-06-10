package io.github.lmliam.kotventure.core.nbt

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent

internal class EntityNbtComponentBuilder(
    selector: String,
    nbtPath: String,
) : NbtComponentBuilder<EntityNBTComponent, EntityNBTComponent.Builder>(
    Component.entityNBT().selector(selector).nbtPath(nbtPath),
),
    EntityNbtScope
