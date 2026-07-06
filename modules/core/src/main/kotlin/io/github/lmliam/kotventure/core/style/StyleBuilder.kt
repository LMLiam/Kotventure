package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.dsl.SingleAssignmentGuard
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
    private val singleAssignments = SingleAssignmentGuard()

    override fun color(color: TextColor?) {
        singleAssignments.assign("color")
        builder.color(color)
    }

    override fun shadow(color: ShadowColor?) {
        singleAssignments.assign("shadow")
        builder.shadowColor(color)
    }

    override fun font(font: Key?) {
        singleAssignments.assign("font")
        builder.font(font)
    }

    override fun insertion(insertion: String?) {
        singleAssignments.assign("insertion")
        builder.insertion(insertion)
    }

    override fun click(event: ClickEvent<*>?) {
        singleAssignments.assign("click")
        builder.clickEvent(event)
    }

    override fun hover(source: HoverEventSource<*>?) {
        singleAssignments.assign("hover")
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
        singleAssignments.assign("decoration '$decoration'")
        builder.decoration(decoration, state)
    }
}

private fun Boolean?.toDecorationState(): State =
    when (this) {
        true -> State.TRUE
        false -> State.FALSE
        null -> State.NOT_SET
    }
