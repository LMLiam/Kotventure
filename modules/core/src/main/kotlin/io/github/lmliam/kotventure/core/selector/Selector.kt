package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Creates a selector [Component]. The client expands it to the names that [selector] matches.
 *
 * This function only creates the component. It does not resolve the selector or send the component to an audience.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorSample
 *
 * @param selector The entity selector, for example a selector from [self] or [entities].
 * @param init Configures the separator, style, and children.
 */
public fun selector(
    selector: EntitySelector,
    init: SelectorScope.() -> Unit = {},
): Component = buildSelectorComponent(selector, init)

internal fun buildSelectorComponent(
    selector: EntitySelector,
    init: SelectorScope.() -> Unit = {},
): Component = SelectorComponentBuilder(selector.asString()).apply(init).build()

/**
 * Creates a selector component and appends it as the next child of this scope.
 *
 * @param selector The entity selector, for example a selector from [self] or [entities].
 * @param init Configures the separator, style, and children.
 */
public fun ComponentScope.selector(
    selector: EntitySelector,
    init: SelectorScope.() -> Unit = {},
) {
    append(buildSelectorComponent(selector, init))
}
