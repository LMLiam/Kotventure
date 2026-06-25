package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.text.format.TextDecoration.OBFUSCATED
import net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH
import net.kyori.adventure.text.format.TextDecoration.State
import net.kyori.adventure.text.format.TextDecoration.UNDERLINED

private val DECORATIONS: List<TextDecoration> = listOf(BOLD, ITALIC, UNDERLINED, STRIKETHROUGH, OBFUSCATED)

private fun TextDecoration.dslFunction(): String = name.lowercase()

internal fun hasDslOutput(style: Style): Boolean =
    style.color() != null ||
            style.font() != null ||
            style.insertion() != null ||
            style.shadowColor() != null ||
            style.clickEvent() != null ||
            style.hoverEvent() != null ||
            DECORATIONS.any { decoration -> style.decoration(decoration) != State.NOT_SET }

internal fun KotlinSourceBuilder.appendStyle(style: Style) {
    style.color()?.let { color -> line("color(${colorLiteral(color)})") }
    style.shadowColor()?.let { shadow -> line("shadow(${shadowColorLiteral(shadow)})") }

    DECORATIONS.forEach { decoration ->
        if (style.decoration(decoration) == State.TRUE) {
            line("${decoration.dslFunction()}()")
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
        DECORATIONS.filter { decoration -> style.decoration(decoration) == State.FALSE }

    if (font == null && insertion == null && disabledDecorations.isEmpty()) {
        return
    }

    block("style") {
        font?.let { line("font(${keyLiteral(it)})") }
        insertion?.let { line("insertion(\"${escapeKotlinString(it)}\")") }
        disabledDecorations.forEach { decoration -> line("${decoration.dslFunction()}(false)") }
    }
}
