package io.github.lmliam.kotventure.core.component

import net.kyori.adventure.text.Component

/**
 * Returns Adventure's empty [Component] (empty content, no children, no style).
 *
 * Use this function instead of [Component.empty] at Kotventure call sites. Also use it instead of an empty [component]
 * block. Adventure's empty text builder currently returns the same singleton, but that is an implementation detail.
 * This function is the specified Kotventure API for no text.
 *
 * @sample io.github.lmliam.kotventure.core.component.emptyComponentSample
 */
public fun emptyComponent(): Component = Component.empty()

/**
 * Returns Adventure's newline [Component].
 *
 * Use this function instead of [Component.newline] at Kotventure call sites. It is primarily a standalone value, such
 * as a `join { }` separator. In a component block, use [ComponentScope.newline].
 *
 * @sample io.github.lmliam.kotventure.core.component.newlineComponentSample
 */
public fun newlineComponent(): Component = Component.newline()

/**
 * Creates an empty-content [Component] and configures its style and children with [init].
 *
 * The function only constructs a value. It does not send the component to an audience.
 *
 * @throws IllegalStateException when [init] assigns a write-once style slot more than once.
 * @sample io.github.lmliam.kotventure.core.component.componentScopeSample
 */
public fun component(init: ComponentScope.() -> Unit): Component =
    ComponentBuilder(Component.text()).apply(init).build()
