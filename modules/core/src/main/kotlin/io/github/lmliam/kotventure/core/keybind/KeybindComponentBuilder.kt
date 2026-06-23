package io.github.lmliam.kotventure.core.keybind

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.KeybindComponent

internal class KeybindComponentBuilder(
    keybind: String,
) : ComponentBuilder<KeybindComponent, KeybindComponent.Builder>(Component.keybind().keybind(keybind))
