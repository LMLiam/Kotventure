package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component

/**
 * Builds an Adventure [Component] directly from a Kotventure text DSL block.
 */
public fun component(init: TextScope.() -> Unit): Component = TextComponentBuilder().apply(init).build()
