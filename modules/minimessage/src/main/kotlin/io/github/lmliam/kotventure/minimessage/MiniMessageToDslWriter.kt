package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.Locale

internal class MiniMessageToDslWriter {
    fun write(component: Component): String {
        if (component.isEmptyDslComponent()) {
            return "component {}"
        }

        val lines = mutableListOf("component {")
        appendRoot(component, lines)
        lines += "}"
        return lines.joinToString(separator = "\n")
    }

    private fun appendRoot(
        component: Component,
        lines: MutableList<String>,
    ) {
        if (component is TextComponent && component.content().isEmpty() && !component.style().hasDslOutput()) {
            component.children().forEach { child -> appendComponent(child, 1, lines) }
            return
        }

        appendComponent(component, 1, lines)
    }

    private fun appendComponent(
        component: Component,
        indent: Int,
        lines: MutableList<String>,
    ) {
        require(component is TextComponent) {
            "miniToDsl slice 1 supports only text component trees, but found ${component::class.simpleName}."
        }

        val text = component.content()
        val hasBlockBody = component.style().hasDslOutput() || component.children().isNotEmpty()

        if (!hasBlockBody) {
            lines += "${indent(indent)}text(\"${escapeString(text)}\")"
            return
        }

        val openingLine =
            if (text.isEmpty()) {
                "${indent(indent)}text {"
            } else {
                "${indent(indent)}text(\"${escapeString(text)}\") {"
            }
        lines += openingLine
        appendStyle(component.style(), indent + 1, lines)
        component.children().forEach { child -> appendComponent(child, indent + 1, lines) }
        lines += "${indent(indent)}}"
    }

    private fun appendStyle(
        style: Style,
        indent: Int,
        lines: MutableList<String>,
    ) {
        style.color()?.let { color ->
            lines += "${indent(indent)}color(${formatColor(color)})"
        }

        decorationFunctions.forEach { (decoration, functionName) ->
            if (style.decoration(decoration) == TextDecoration.State.TRUE) {
                lines += "${indent(indent)}$functionName()"
            }
        }
    }

    private fun formatColor(color: TextColor): String =
        if (color is NamedTextColor) {
            "NamedTextColor.${color.toString().uppercase(Locale.ROOT)}"
        } else {
            "TextColor.color(0x${color.asHexString().removePrefix("#").uppercase(Locale.ROOT)})"
        }

    private fun escapeString(value: String): String =
        buildString {
            value.forEach { character ->
                when (character) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\t' -> append("\\t")
                    '$' -> append("\\$")
                    else -> append(character)
                }
            }
        }

    private fun Style.hasDslOutput(): Boolean =
        color() != null ||
            decorationFunctions.keys.any { decoration -> decoration(decoration) == TextDecoration.State.TRUE }

    private fun Component.isEmptyDslComponent(): Boolean =
        this is TextComponent &&
            content().isEmpty() &&
            children().isEmpty() &&
            !style().hasDslOutput()

    private fun indent(level: Int): String = "    ".repeat(level)

    private companion object {
        private val decorationFunctions: LinkedHashMap<TextDecoration, String> =
            linkedMapOf(
                TextDecoration.BOLD to "bold",
                TextDecoration.ITALIC to "italic",
                TextDecoration.UNDERLINED to "underlined",
                TextDecoration.STRIKETHROUGH to "strikethrough",
                TextDecoration.OBFUSCATED to "obfuscated",
            )
    }
}
