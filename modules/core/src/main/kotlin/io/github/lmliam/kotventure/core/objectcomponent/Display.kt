package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.`object`.ObjectContents

/**
 * Creates an object [Component], such as a player head or an atlas sprite.
 *
 * This function only creates the component. It does not render a fallback or send the component to an audience.
 *
 * @sample io.github.lmliam.kotventure.core.objectcomponent.displaySample
 *
 * @param contents the object to render, for example contents from [sprite] or [head].
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
 * Creates an object component and appends it as the next child of this scope.
 *
 * @param contents the object to render, for example contents from [sprite] or [head].
 * @param init sets a fallback, styles the child, and appends any of its own children.
 */
public fun ComponentScope.display(
    contents: ObjectContents,
    init: ObjectScope.() -> Unit = {},
) {
    append(buildObjectComponent(contents, init))
}
