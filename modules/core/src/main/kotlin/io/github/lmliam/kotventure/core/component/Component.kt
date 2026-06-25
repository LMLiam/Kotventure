package io.github.lmliam.kotventure.core.component

import net.kyori.adventure.text.Component

/**
 * Builds an Adventure [Component] from a Kotventure component DSL block.
 *
 * The block runs against a [ComponentScope] rooted at an empty text component, so its children and
 * styling become the returned component's.
 */
public fun component(init: ComponentScope.() -> Unit): Component =
    ComponentBuilder(Component.text()).apply(init).build()
