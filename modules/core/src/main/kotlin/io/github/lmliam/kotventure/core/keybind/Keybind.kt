package io.github.lmliam.kotventure.core.keybind

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Creates a keybind [Component] for the client's current binding of [keybind].
 *
 * The function only constructs a value. The client resolves the displayed text when it renders the component.
 *
 * @sample io.github.lmliam.kotventure.core.keybind.keybindSample
 *
 * @param keybind the Minecraft keybind identifier, such as `"key.jump"`.
 * @param init styles the component and appends any children.
 * @throws IllegalStateException when [init] assigns a write-once style slot more than once.
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
 * Appends a keybind child for the client's current binding of [keybind].
 *
 * @param keybind the Minecraft keybind identifier, such as `"key.jump"`.
 * @param init styles the child and appends any of its own children.
 * @throws IllegalStateException when [init] assigns a write-once style slot more than once.
 */
public fun ComponentScope.keybind(
    keybind: String,
    init: ComponentScope.() -> Unit = {},
) {
    append(buildKeybindComponent(keybind, init))
}
