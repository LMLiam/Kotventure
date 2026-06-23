package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Builds an Adventure text [Component] with [value] as its initial content, configured by [init].
 */
public fun text(
    value: String,
    init: TextScope.() -> Unit = {},
): Component = buildTextComponent(value, init)

internal fun buildTextComponent(
    value: String,
    init: TextScope.() -> Unit = {},
): Component =
    buildTextComponent {
        content(value)
        init()
    }

/**
 * Builds an Adventure text [Component] from a Kotventure text DSL block.
 */
public fun text(init: TextScope.() -> Unit): Component = buildTextComponent(init)

internal fun buildTextComponent(init: TextScope.() -> Unit): Component = TextBuilder().apply(init).build()

/**
 * Appends a nested text child with [value] as its initial content.
 */
public fun ComponentScope.text(
    value: String,
    init: TextScope.() -> Unit = {},
) {
    append(buildTextComponent(value, init))
}

/**
 * Appends a nested text child.
 */
public fun ComponentScope.text(init: TextScope.() -> Unit) {
    append(buildTextComponent(init))
}
