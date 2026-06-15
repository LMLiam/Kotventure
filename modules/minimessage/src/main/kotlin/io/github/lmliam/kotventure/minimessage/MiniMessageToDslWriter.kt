package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.State
import java.util.Locale

internal object MiniMessageToDslWriter {
    fun write(component: Component): String {
        MiniMessageToDslSupport.requireSupported(component)

        if (component.isEmptyDslComponent()) {
            return "component {}"
        }

        val lines = mutableListOf("component {")
        appendRoot(component, 1, lines)
        lines += "}"
        return lines.joinToString(separator = "\n")
    }

    private fun appendRoot(
        component: Component,
        indent: Int,
        lines: MutableList<String>,
    ) {
        if (component is TextComponent &&
            component.content().isEmpty() &&
            !MiniMessageToDslSupport.hasDslOutput(component.style())
        ) {
            component.children().forEach { child -> appendComponent(child, indent, lines) }
            return
        }

        appendComponent(component, indent, lines)
    }

    private fun appendComponent(
        component: Component,
        indent: Int,
        lines: MutableList<String>,
    ) {
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

        style.clickEvent()?.let { event ->
            appendClickEvent(event, indent, lines)
        }

        style.hoverEvent()?.let { event ->
            appendHoverEvent(event, indent, lines)
        }
    }

    private fun appendClickEvent(
        event: ClickEvent<*>,
        indent: Int,
        lines: MutableList<String>,
    ) {
        lines += "${indent(indent)}click {"
        when (event.action()) {
            ClickEvent.Action.OPEN_URL -> {
                val payload = event.payload() as ClickEvent.Payload.Text
                lines += "${indent(indent + 1)}openUrl(\"${escapeString(payload.value())}\")"
            }
            ClickEvent.Action.OPEN_FILE -> {
                val payload = event.payload() as ClickEvent.Payload.Text
                lines += "${indent(indent + 1)}openFile(\"${escapeString(payload.value())}\")"
            }
            ClickEvent.Action.RUN_COMMAND -> {
                val payload = event.payload() as ClickEvent.Payload.Text
                lines += "${indent(indent + 1)}run(\"${escapeString(payload.value())}\")"
            }
            ClickEvent.Action.SUGGEST_COMMAND -> {
                val payload = event.payload() as ClickEvent.Payload.Text
                lines += "${indent(indent + 1)}suggest(\"${escapeString(payload.value())}\")"
            }
            ClickEvent.Action.CHANGE_PAGE -> {
                val payload = event.payload() as ClickEvent.Payload.Int
                lines += "${indent(indent + 1)}changePage(${payload.integer()})"
            }
            ClickEvent.Action.COPY_TO_CLIPBOARD -> {
                val payload = event.payload() as ClickEvent.Payload.Text
                lines += "${indent(indent + 1)}copy(\"${escapeString(payload.value())}\")"
            }
            else -> {
                lines += "${indent(indent + 1)}// callback not representable"
            }
        }
        lines += "${indent(indent)}}"
    }

    private fun appendHoverEvent(
        event: HoverEvent<*>,
        indent: Int,
        lines: MutableList<String>,
    ) {
        lines += "${indent(indent)}hover {"
        when (event.action()) {
            HoverEvent.Action.SHOW_TEXT -> {
                val payload = event.value() as Component
                lines += "${indent(indent + 1)}text {"
                appendRoot(payload, indent + 2, lines)
                lines += "${indent(indent + 1)}}"
            }
            HoverEvent.Action.SHOW_ITEM -> appendShowItem(event.value() as HoverEvent.ShowItem, indent + 1, lines)
            HoverEvent.Action.SHOW_ENTITY -> appendShowEntity(event.value() as HoverEvent.ShowEntity, indent + 1, lines)
            else -> error("miniToDsl slice 2 does not support hover action ${event.action().name()}.")
        }
        lines += "${indent(indent)}}"
    }

    private fun appendShowItem(
        item: HoverEvent.ShowItem,
        indent: Int,
        lines: MutableList<String>,
    ) {
        require(item.nbt() == null) {
            "miniToDsl slice 2 does not support legacy show-item NBT payloads."
        }

        val argumentLines = mutableListOf("${indent(indent + 1)}key = ${formatKey(item.item())}")
        if (item.count() != 1) {
            argumentLines += "${indent(indent + 1)}count = ${item.count()}"
        }
        if (item.dataComponents().isNotEmpty()) {
            argumentLines += buildDataComponentsArgument(item.dataComponents(), indent)
        }

        if (argumentLines.size == 1) {
            lines += "${indent(indent)}item(${formatKey(item.item())})"
            return
        }

        lines += "${indent(indent)}item("
        lines += argumentLines.joinToString(separator = ",\n")
        lines += "${indent(indent)})"
    }

    private fun appendShowEntity(
        entity: HoverEvent.ShowEntity,
        indent: Int,
        lines: MutableList<String>,
    ) {
        val header =
            listOf(
                "${indent(indent + 1)}type = ${formatKey(entity.type())}",
                "${indent(indent + 1)}id = UUID.fromString(\"${entity.id()}\")",
            ).joinToString(separator = ",\n")

        entity.name()?.let { name ->
            lines += "${indent(indent)}entity("
            lines += header
            lines += "${indent(indent)}) {"
            appendRoot(name, indent + 1, lines)
            lines += "${indent(indent)}}"
            return
        }

        lines += "${indent(indent)}entity("
        lines += header
        lines += "${indent(indent)})"
    }

    private fun buildDataComponentsArgument(
        dataComponents: Map<Key, DataComponentValue>,
        indent: Int,
    ): String {
        val entries =
            dataComponents.entries.joinToString(separator = ",\n") { (key, value) ->
                "${indent(indent + 2)}${formatKey(key)} to ${formatDataComponentValue(value)}"
            }
        return buildString {
            append("${indent(indent + 1)}dataComponents = mapOf(\n")
            append(entries)
            append("\n${indent(indent + 1)})")
        }
    }

    private fun formatDataComponentValue(value: DataComponentValue): String =
        when (value) {
            is BinaryTagHolder -> "BinaryTagHolder.binaryTagHolder(\"${escapeString(value.string())}\")"
            is DataComponentValue.TagSerializable ->
                "BinaryTagHolder.binaryTagHolder(\"${escapeString(value.asBinaryTag().string())}\")"
            is DataComponentValue.Removed -> "DataComponentValue.removed()"
            else -> error("miniToDsl slice 2 does not support data component value ${value::class.qualifiedName}.")
        }

    private fun formatKey(key: Key): String =
        "key(\"${escapeString(key.namespace())}\", \"${escapeString(key.value())}\")"

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
