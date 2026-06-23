package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

internal val decorations: List<Pair<TextDecoration, String>> =
    listOf(
        TextDecoration.BOLD to "bold",
        TextDecoration.ITALIC to "italic",
        TextDecoration.UNDERLINED to "underlined",
        TextDecoration.STRIKETHROUGH to "strikethrough",
        TextDecoration.OBFUSCATED to "obfuscated",
    )

/**
 * Whether [style] carries anything that opens a component block.
 */
internal fun hasDslOutput(style: Style): Boolean =
    style.color() != null ||
            style.font() != null ||
            style.insertion() != null ||
            style.shadowColor() != null ||
            style.clickEvent() != null ||
            style.hoverEvent() != null ||
            decorations.any { (decoration) -> style.decoration(decoration) != State.NOT_SET }

internal fun KotlinSourceBuilder.appendStyle(style: Style) {
    style.color()?.let { color -> line("color(${colorLiteral(color)})") }
    style.shadowColor()?.let { shadow -> line("shadow(${shadowColorLiteral(shadow)})") }

    decorations.forEach { (decoration, functionName) ->
        if (style.decoration(decoration) == State.TRUE) {
            line("$functionName()")
        }
    }

    appendStyleBlock(style)

    style.clickEvent()?.let { event -> appendClickEvent(event) }
    style.hoverEvent()?.let { event -> appendHoverEvent(event) }
}

/**
 * Emits a `style { ... }` block for the attributes that have no shorthand directly on a component scope.
 */
private fun KotlinSourceBuilder.appendStyleBlock(style: Style) {
    val font = style.font()
    val insertion = style.insertion()
    val disabledDecorations =
        decorations.filter { (decoration) -> style.decoration(decoration) == State.FALSE }

    if (font == null && insertion == null && disabledDecorations.isEmpty()) {
        return
    }

    block("style") {
        font?.let { line("font(${keyLiteral(it)})") }
        insertion?.let { line("insertion(\"${escapeKotlinString(it)}\")") }
        disabledDecorations.forEach { (_, functionName) -> line("$functionName(false)") }
    }
}
