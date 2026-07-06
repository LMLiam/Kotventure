package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.dsl.SingleAssignmentGuard
import io.github.lmliam.kotventure.core.style.StyleBuilder
import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State
import net.kyori.adventure.text.ComponentBuilder as AdventureComponentBuilder

internal open class ComponentBuilder<C : Component, B : AdventureComponentBuilder<C, B>>(
    protected val builder: B,
) : ComponentScope {
    protected val singleAssignments: SingleAssignmentGuard = SingleAssignmentGuard()

    override fun color(color: TextColor?) {
        singleAssignments.assign("color")
        builder.color(color)
    }

    override fun shadow(color: ShadowColor?) {
        singleAssignments.assign("shadow")
        builder.style { styleBuilder -> styleBuilder.shadowColor(color) }
    }

    override fun font(font: Key?) {
        singleAssignments.assign("font")
        builder.style { styleBuilder -> styleBuilder.font(font) }
    }

    override fun insertion(insertion: String?) {
        singleAssignments.assign("insertion")
        builder.style { styleBuilder -> styleBuilder.insertion(insertion) }
    }

    override fun style(style: Style) {
        singleAssignments.assign("style")
        builder.style(style)
    }

    override fun style(init: StyleScope.() -> Unit) {
        singleAssignments.assign("style")
        builder.style { styleBuilder -> StyleBuilder(styleBuilder).init() }
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
        decoration(decoration, State.byBoolean(flag))
    }

    override fun decoration(
        decoration: TextDecoration,
        state: State,
    ) {
        singleAssignments.assign("decoration '$decoration'")
        builder.decoration(decoration, state)
    }

    override fun <T : ComponentLike> append(component: T) {
        builder.append(component.asComponent())
    }

    override fun newline() {
        builder.append(Component.newline())
    }

    internal open fun build(): Component = builder.build()
}
