package io.github.lmliam.kotventure.core.component

import net.kyori.adventure.text.Component

/**
 * Returns Adventure's empty [Component] (empty content, no children, no style).
 *
 * Prefer this over [Component.empty] in Kotventure call sites, and over an empty
 * [component] `{ }` block: the empty builder happens to collapse to the same singleton today, but
 * that is an implementation detail of Adventure's text builder, not the intended API for "no text".
 *
 * @sample io.github.lmliam.kotventure.core.component.emptyComponentSample
 */
public fun emptyComponent(): Component = Component.empty()

/**
 * Builds an Adventure [Component] from a Kotventure component DSL block.
 *
 * The block runs against a [ComponentScope] rooted at an empty text component, so its children and
 * styling become the returned component's.
 *
 * @sample io.github.lmliam.kotventure.core.component.componentScopeSample
 */
public fun component(init: ComponentScope.() -> Unit): Component =
    ComponentBuilder(Component.text()).apply(init).build()
