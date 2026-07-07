package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.dsl.SingleAssignSet
import io.github.lmliam.kotventure.core.dsl.singleAssign
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

internal class StyleBuilder(
    private val builder: Style.Builder,
) : StyleScope {
    private var color: TextColor? by singleAssign()
    private var shadow: ShadowColor? by singleAssign()
    private var font: Key? by singleAssign()
    private var insertion: String? by singleAssign()
    private var click: ClickEvent<*>? by singleAssign()
    private var hover: HoverEventSource<*>? by singleAssign()
    private val decorations = SingleAssignSet<TextDecoration>()

    override fun color(color: TextColor?) {
        this.color = color
        builder.color(color)
    }

    override fun shadow(color: ShadowColor?) {
        this.shadow = color
        builder.shadowColor(color)
    }

    override fun font(font: Key?) {
        this.font = font
        builder.font(font)
    }

    override fun insertion(insertion: String?) {
        this.insertion = insertion
        builder.insertion(insertion)
    }

    override fun click(event: ClickEvent<*>?) {
        this.click = event
        builder.clickEvent(event)
    }

    override fun hover(source: HoverEventSource<*>?) {
        this.hover = source
        builder.hoverEvent(source)
    }

    override fun decoration(
        decoration: TextDecoration,
        flag: Boolean?,
    ) {
        decoration(decoration, flag.toDecorationState())
    }

    override fun decoration(
        decoration: TextDecoration,
        state: State,
    ) {
        decorations.assign(decoration)
        builder.decoration(decoration, state)
    }
}

private fun Boolean?.toDecorationState(): State =
    when (this) {
        true -> State.TRUE
        false -> State.FALSE
        null -> State.NOT_SET
    }
