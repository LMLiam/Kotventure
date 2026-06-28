package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Builds a selector [Component] — text the client expands to the names matched by an entity selector.
 *
 * ```kotlin
 * val nearby = selector(entities { distance(atMost(10.0)) }) { separator { content(", ") } }
 * ```
 *
 * @param selector the entity selector, constructed via [self], [entities], or friends.
 * @param init configures the selector (e.g. its separator) and appends any children.
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
 * Appends a selector child to this scope, for use inside a `component { }` or other component block.
 *
 * @param selector the entity selector, constructed via [self], [entities], or friends.
 * @param init configures the selector (e.g. its separator) and appends any children.
 */
public fun ComponentScope.selector(
    selector: EntitySelector,
    init: SelectorScope.() -> Unit = {},
) {
    append(buildSelectorComponent(selector, init))
}
