package io.github.lmliam.kotventure.core.keybind

import net.kyori.adventure.text.Component

/**
 * Builds an Adventure keybind [Component] from a Kotventure DSL block.
 */
public fun keybind(
    keybind: String,
    init: KeybindScope.() -> Unit = {},
): Component = KeybindComponentBuilder(keybind).apply(init).build()
