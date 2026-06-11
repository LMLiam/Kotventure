package io.github.lmliam.kotventure.core.style

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

internal class StyleScopeBuilder(
    private val builder: Style.Builder,
) : StyleScope {
    override fun color(color: TextColor?) {
        builder.color(color)
    }

    override fun font(font: Key?) {
        builder.font(font)
    }

    override fun insertion(insertion: String?) {
        builder.insertion(insertion)
    }

    override fun click(event: ClickEvent<*>?) {
        builder.clickEvent(event)
    }

    override fun hover(source: HoverEventSource<*>?) {
        builder.hoverEvent(source)
    }

    override fun decoration(
        decoration: TextDecoration,
        flag: Boolean?,
    ) {
        builder.decoration(decoration, flag.toDecorationState())
    }

    override fun decoration(
        decoration: TextDecoration,
        state: State,
    ) {
        builder.decoration(decoration, state)
    }
}

private fun Boolean?.toDecorationState(): State =
    when (this) {
        true -> State.TRUE
        false -> State.FALSE
        null -> State.NOT_SET
    }
