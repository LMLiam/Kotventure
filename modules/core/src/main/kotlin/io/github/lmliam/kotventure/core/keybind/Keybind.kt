package io.github.lmliam.kotventure.core.keybind

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.KeybindComponent

/**
 * Builds a keybind [Component] — text the client renders as the player's current binding for a game action.
 *
 * ```kotlin
 * val jump = keybind("key.jump") { color(aqua) }
 * ```
 *
 * @param keybind the Minecraft keybind identifier, such as `"key.jump"`.
 * @param init styles the component and appends any children.
 */
public fun keybind(
    keybind: String,
    init: ComponentScope.() -> Unit = {},
): Component = buildKeybindComponent(keybind, init)

internal fun buildKeybindComponent(
    keybind: String,
    init: ComponentScope.() -> Unit = {},
): Component =
    ComponentBuilder(
        Component.keybind().keybind(keybind),
    ).apply(init).build()

/**
 * Appends a keybind child to this scope, for use inside a `component { }` or other component block.
 *
 * @param keybind the Minecraft keybind identifier, such as `"key.jump"`.
 * @param init styles the child and appends any of its own children.
 */
public fun ComponentScope.keybind(
    keybind: String,
    init: ComponentScope.() -> Unit = {},
) {
    append(buildKeybindComponent(keybind, init))
}
