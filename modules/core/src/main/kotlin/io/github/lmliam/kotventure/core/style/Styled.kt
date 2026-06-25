package io.github.lmliam.kotventure.core.style

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style

/**
 * Returns a copy of this component with [style] applied, replacing any existing style.
 *
 * ```kotlin
 * val highlighted = component { text("important") } styled heading
 * ```
 */
public infix fun Component.styled(style: Style): Component = style(style)
