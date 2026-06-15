package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

internal object MiniMessageToDslSupport {
    val decorations: List<Pair<TextDecoration, String>> =
        listOf(
            TextDecoration.BOLD to "bold",
            TextDecoration.ITALIC to "italic",
            TextDecoration.UNDERLINED to "underlined",
            TextDecoration.STRIKETHROUGH to "strikethrough",
            TextDecoration.OBFUSCATED to "obfuscated",
        )

    fun requireSupported(component: Component) {
        require(component is TextComponent) {
            "miniToDsl supports only text component trees, but found ${component::class.simpleName}."
        }
        requireSupported(component.style())
        component.children().forEach(::requireSupported)
    }

    fun requireSupported(style: Style) {
        require(style.insertion() == null) {
            "miniToDsl does not yet support insertion text."
        }
        require(style.font() == null) {
            "miniToDsl does not yet support font styles."
        }
        require(style.shadowColor() == null) {
            "miniToDsl does not yet support shadow colours."
        }
        style.hoverEvent()?.let { hover -> requireSupportedHoverPayload(hover) }
    }

    /**
     * Validates the nested component a hover event renders, so unsupported styles or non-text payloads are rejected up
     * front instead of being silently dropped or triggering a cast failure while the DSL is written.
     */
    private fun requireSupportedHoverPayload(hover: HoverEvent<*>) {
        when (hover.action()) {
            HoverEvent.Action.SHOW_TEXT -> requireSupported(hover.value() as Component)
            HoverEvent.Action.SHOW_ENTITY -> {
                val entity = hover.value() as HoverEvent.ShowEntity
                entity.name()?.let { name -> requireSupported(name) }
            }

            else -> Unit
        }
    }

    fun hasDslOutput(style: Style): Boolean =
        style.color() != null ||
                style.clickEvent() != null ||
                style.hoverEvent() != null ||
                decorations.any { (decoration) -> style.decoration(decoration) != State.NOT_SET }
}
