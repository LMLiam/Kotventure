package io.github.lmliam.kotventure.core.translatable

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Creates a translatable [Component] for [key].
 *
 * The client resolves the key in its locale. This function only creates the component. It does not resolve a
 * translation or send the component to an audience.
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
 * Creates a translatable component and appends it as the next child of this scope.
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
