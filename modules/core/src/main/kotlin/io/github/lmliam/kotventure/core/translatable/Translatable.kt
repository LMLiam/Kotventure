package io.github.lmliam.kotventure.core.translatable

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Builds a translatable [Component] — text the client renders from a translation key in the player's locale.
 *
 * @sample io.github.lmliam.kotventure.core.translatable.translatableSample
 *
 * @param key the translation key, such as `"item.minecraft.diamond"`.
 * @param init supplies translation arguments and an optional fallback, and appends any children.
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
 * Appends a translatable child to this scope, for use inside a `component { }` or other component block.
 *
 * @param key the translation key, such as `"item.minecraft.diamond"`.
 * @param init supplies translation arguments and an optional fallback, and appends any children.
 */
public fun ComponentScope.translatable(
    key: String,
    init: TranslatableScope.() -> Unit = {},
) {
    append(buildTranslatableComponent(key, init))
}
