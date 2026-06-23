package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.`object`.ObjectContents

/**
 * Builds an Adventure object [Component] from [contents] and a Kotventure DSL block.
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
 * Appends a nested object child with [contents].
 */
public fun ComponentScope.display(
    contents: ObjectContents,
    init: ObjectScope.() -> Unit = {},
) {
    append(buildObjectComponent(contents, init))
}
