package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component

/**
 * Builds an Adventure text [Component] with [value] as its content, configured by [init].
 */
public fun text(
    value: String,
    init: TextScope.() -> Unit = {},
): Component =
    text {
        content(value)
        init()
    }

/**
 * Builds an Adventure text [Component] from a Kotventure text DSL block.
 */
public fun text(init: TextScope.() -> Unit): Component = TextComponentBuilder().apply(init).build()
