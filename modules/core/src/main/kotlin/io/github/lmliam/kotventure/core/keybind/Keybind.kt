package io.github.lmliam.kotventure.core.keybind

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.addChild
import net.kyori.adventure.text.Component

/**
 * Builds an Adventure keybind [Component] from a Kotventure DSL block.
 */
public fun keybind(
    keybind: String,
    init: ComponentScope.() -> Unit = {},
): Component = buildKeybindComponent(keybind, init)

internal fun buildKeybindComponent(
    keybind: String,
    init: ComponentScope.() -> Unit = {},
): Component = KeybindComponentBuilder(keybind).apply(init).build()

/**
 * Appends a nested keybind child with [keybind] as its keybind identifier.
 */
public fun ComponentScope.keybind(
    keybind: String,
    init: ComponentScope.() -> Unit = {},
) {
    addChild(buildKeybindComponent(keybind, init))
}
