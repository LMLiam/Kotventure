package io.github.lmliam.kotventure.core.translatable

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.addChild
import net.kyori.adventure.text.Component

/**
 * Builds an Adventure translatable [Component] from a Kotventure DSL block.
 */
public fun translatable(
    key: String,
    init: TranslatableScope.() -> Unit = {},
): Component = buildTranslatableComponent(key, init)

internal fun buildTranslatableComponent(
    key: String,
    init: TranslatableScope.() -> Unit = {},
): Component = TranslatableComponentBuilder(key).apply(init).build()

/**
 * Appends a nested translatable child with [key] as its translation key.
 */
public fun ComponentScope.translatable(
    key: String,
    init: TranslatableScope.() -> Unit = {},
) {
    addChild(buildTranslatableComponent(key, init))
}
