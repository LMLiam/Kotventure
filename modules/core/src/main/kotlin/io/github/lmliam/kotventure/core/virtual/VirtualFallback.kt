package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style

/**
 * An immutable snapshot of a virtual component's fallback text, style, and children.
 */
internal data class VirtualFallback(
    val content: String = "",
    val style: Style = Style.empty(),
    val children: List<Component> = emptyList(),
)

internal fun buildVirtualFallback(init: ComponentScope.() -> Unit): VirtualFallback {
    val component = component(init)

    return VirtualFallback(
        style = component.style(),
        children = component.children(),
    )
}
