package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Builds a selector [Component] — text the client expands to the names matched by an entity selector.
 *
 * ```kotlin
 * val nearby = selector("@e[distance=..10]") { separator(Component.text(", ")) }
 * ```
 *
 * @param pattern the entity-selector pattern, such as `"@a"` or `"@e[type=zombie]"`.
 * @param init configures the selector (e.g. its separator) and appends any children.
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
 * Appends a selector child to this scope, for use inside a `component { }` or other component block.
 *
 * @param pattern the entity-selector pattern, such as `"@a"` or `"@e[type=zombie]"`.
 * @param init configures the selector (e.g. its separator) and appends any children.
 */
public fun ComponentScope.selector(
    pattern: String,
    init: SelectorScope.() -> Unit = {},
) {
    append(buildSelectorComponent(pattern, init))
}
