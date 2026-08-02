package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.text.format.TextDecoration.OBFUSCATED
import net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH
import net.kyori.adventure.text.format.TextDecoration.State
import net.kyori.adventure.text.format.TextDecoration.UNDERLINED

private val standardDecorations: List<TextDecoration> =
    listOf(
        BOLD,
        ITALIC,
        UNDERLINED,
        STRIKETHROUGH,
        OBFUSCATED,
    )

private val TextDecoration.dslFunctionName: String
    get() = name.lowercase()

/** Returns whether [style] requires any generated DSL output. */
internal fun hasDslOutput(style: Style): Boolean =
    style.color() != null ||
            style.shadowColor() != null ||
            style.font() != null ||
            style.insertion() != null ||
            style.clickEvent() != null ||
            style.hoverEvent() != null ||
            style.hasExplicitDecorationState()

/** Emits the complete Kotventure DSL representation of [style]. */
internal fun KotlinSourceBuilder.appendStyle(style: Style) {
    style.color()?.let {
        line("color(${colorLiteral(it)})")
    }

    style.shadowColor()?.let {
        line("shadow(${shadowColorLiteral(it)})")
    }

    style.decorationsIn(State.TRUE).forEach {
        line("${it.dslFunctionName}()")
    }

    appendStyleOverrides(style)

    style.clickEvent()?.let {
        appendClickEvent(it)
    }

    style.hoverEvent()?.let {
        appendHoverEvent(it)
    }
}

/**
 * Emits properties that must appear inside the explicit `style` block.
 *
 * This includes font, insertion, and decoration states set explicitly to `false`.
 */
private fun KotlinSourceBuilder.appendStyleOverrides(style: Style) {
    val font = style.font()
    val insertion = style.insertion()
    val disabledDecorations = style.decorationsIn(State.FALSE)

    if (font == null && insertion == null && disabledDecorations.isEmpty()) {
        return
    }

    block("style") {
        font?.let {
            line("font(${keyLiteral(it)})")
        }

        insertion?.let {
            line("insertion(${quoted(it)})")
        }

        disabledDecorations.forEach {
            line("${it.dslFunctionName}(false)")
        }
    }
}

private fun Style.hasExplicitDecorationState(): Boolean =
    standardDecorations.any {
        decoration(it) != State.NOT_SET
    }

private fun Style.decorationsIn(state: State): List<TextDecoration> =
    standardDecorations.filter {
        decoration(it) == state
    }
