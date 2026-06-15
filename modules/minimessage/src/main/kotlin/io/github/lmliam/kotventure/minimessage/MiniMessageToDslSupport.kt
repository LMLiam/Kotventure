package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
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
            "miniToDsl slice 1 supports only text component trees, but found ${component::class.simpleName}."
        }
        requireSupported(component.style())
    }

    fun requireSupported(style: Style) {
        require(style.clickEvent() == null) {
            "miniToDsl slice 1 does not support click events."
        }
        require(style.hoverEvent() == null) {
            "miniToDsl slice 1 does not support hover events."
        }
        require(style.insertion() == null) {
            "miniToDsl slice 1 does not support insertion text."
        }
        require(style.font() == null) {
            "miniToDsl slice 1 does not support font styles."
        }
    }

    fun hasDslOutput(style: Style): Boolean =
        style.color() != null ||
            decorations.any { (decoration) -> style.decoration(decoration) != State.NOT_SET }
}
