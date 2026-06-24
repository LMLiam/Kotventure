package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.`object`.ObjectContents

/**
 * Builds an object [Component] — an inline rendered object such as a player head or atlas sprite.
 *
 * ```kotlin
 * val head = display(head(uuid)) { fallback(Component.text("?")) }
 * ```
 *
 * @param contents what to render, built with the object-contents helpers (`sprite(...)`, `head(...)`).
 * @param init sets a fallback, styles the component, and appends any children.
 */
public fun display(
    contents: ObjectContents,
    init: ObjectScope.() -> Unit = {},
): Component = buildObjectComponent(contents, init)

internal fun buildObjectComponent(
    contents: ObjectContents,
    init: ObjectScope.() -> Unit = {},
): Component = ObjectComponentBuilder(contents).apply(init).build()

/**
 * Appends an object child to this scope, for use inside a `component { }` or other component block.
 *
 * @param contents what to render, built with the object-contents helpers (`sprite(...)`, `head(...)`).
 * @param init sets a fallback, styles the child, and appends any of its own children.
 */
public fun ComponentScope.display(
    contents: ObjectContents,
    init: ObjectScope.() -> Unit = {},
) {
    append(buildObjectComponent(contents, init))
}
