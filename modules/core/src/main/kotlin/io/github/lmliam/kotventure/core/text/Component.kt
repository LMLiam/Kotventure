package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Builds an Adventure [Component] directly from a Kotventure component DSL block.
 */
public fun component(init: ComponentScope.() -> Unit): Component = TextComponentBuilder().apply(init).build()
