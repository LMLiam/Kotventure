package org.eventhorizonlab.kyoriadventuredsl.core

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.eventhorizonlab.core.api.TextComponentScope

internal class TextComponentScope(
    private val builder: TextComponent.Builder
) : TextComponentScope {
    override fun content(content: String) {
        builder.content(content)
    }

    override fun color(color: NamedTextColor) {
        builder.color(color)
    }

    override fun decorate(vararg decorations: TextDecoration) {
        decorations.forEach { builder.decoration(it, true) }
    }

    override fun text(init: TextComponentScope.() -> Unit) {
        val childBuilder = Component.text()
        TextComponentScope(childBuilder).init()
        builder.append(childBuilder.build())
    }

    fun build() = builder.build()

    class Factory : TextComponentScope.Factory {
        override fun create(init: TextComponentScope.() -> Unit): TextComponent {
            val builder = Component.text()
            return TextComponentScope(builder).apply(init).build()
        }
    }
}