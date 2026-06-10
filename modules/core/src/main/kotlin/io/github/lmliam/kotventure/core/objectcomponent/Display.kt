package io.github.lmliam.kotventure.core.objectcomponent

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.`object`.ObjectContents

/**
 * Builds an Adventure object [Component] from [contents] and a Kotventure DSL block.
 */
public fun display(
    contents: ObjectContents,
    init: ObjectScope.() -> Unit = {},
): Component = ObjectComponentBuilder(contents).apply(init).build()
