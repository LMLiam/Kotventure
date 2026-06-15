package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.State
import java.util.Locale

internal object MiniMessageToDslWriter {
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
        if (component is TextComponent &&
            component.content().isEmpty() &&
            !MiniMessageToDslSupport.hasDslOutput(component.style())
        ) {
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
        MiniMessageToDslSupport.requireSupported(component)
        component as TextComponent

        val text = component.content()
        val hasBlockBody = MiniMessageToDslSupport.hasDslOutput(component.style()) || component.children().isNotEmpty()

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
        MiniMessageToDslSupport.requireSupported(style)

        style.color()?.let { color ->
            lines += "${indent(indent)}color(${formatColor(color)})"
        }

        MiniMessageToDslSupport.decorations.forEach { (decoration, functionName) ->
            if (style.decoration(decoration) == State.TRUE) {
                lines += "${indent(indent)}$functionName()"
            }
        }

        val disabledDecorations =
            MiniMessageToDslSupport.decorations.filter { (decoration) -> style.decoration(decoration) == State.FALSE }
        if (disabledDecorations.isNotEmpty()) {
            lines += "${indent(indent)}style {"
            disabledDecorations.forEach { (_, functionName) ->
                lines += "${indent(indent + 1)}$functionName(false)"
            }
            lines += "${indent(indent)}}"
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
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    '$' -> append('\\').append('$')
                    else -> append(character)
                }
            }
        }

    private fun Component.isEmptyDslComponent(): Boolean =
        this is TextComponent &&
                content().isEmpty() &&
                children().isEmpty() &&
                !MiniMessageToDslSupport.hasDslOutput(style())

    private fun indent(level: Int): String = "    ".repeat(level)
}
