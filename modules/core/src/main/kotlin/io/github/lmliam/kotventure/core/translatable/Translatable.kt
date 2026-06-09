package io.github.lmliam.kotventure.core.translatable

import net.kyori.adventure.text.Component

/**
 * Builds an Adventure translatable [Component] from a Kotventure DSL block.
 */
public fun translatable(
    key: String,
    init: TranslatableScope.() -> Unit = {},
): Component = TranslatableComponentBuilder(key).apply(init).build()
