package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

/**
 * Builds the rendered content for one [VirtualRenderScope] call.
 *
 * The builder starts from an empty text root, so the render block adds style and children in the same way as a
 * `component { }` block. It exposes the [context] that the platform supplied for this render.
 */
internal class VirtualRenderBuilder<C : Any>(
    override val context: C,
) : ComponentBuilder<TextComponent, TextComponent.Builder>(Component.text()),
    VirtualRenderScope<C>
