package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.addChild
import net.kyori.adventure.text.Component

/**
 * Builds an Adventure selector [Component] from a Kotventure DSL block.
 */
public fun selector(
    pattern: String,
    init: SelectorScope.() -> Unit = {},
): Component = buildSelectorComponent(pattern, init)

internal fun buildSelectorComponent(
    pattern: String,
    init: SelectorScope.() -> Unit = {},
): Component = SelectorComponentBuilder(pattern).apply(init).build()

/**
 * Appends a nested selector child with [pattern] as its selector pattern.
 */
public fun ComponentScope.selector(
    pattern: String,
    init: SelectorScope.() -> Unit = {},
) {
    addChild(buildSelectorComponent(pattern, init))
}
