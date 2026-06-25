package io.github.lmliam.kotventure.core

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal fun componentLike(component: Component): ComponentLike =
    object : ComponentLike {
        override fun asComponent(): Component = component
    }
