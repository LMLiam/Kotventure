package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.text.Component

/**
 * Builds an Adventure selector [Component] from a Kotventure DSL block.
 */
public fun selector(
    pattern: String,
    init: SelectorScope.() -> Unit = {},
): Component = SelectorComponentBuilder(pattern).apply(init).build()
