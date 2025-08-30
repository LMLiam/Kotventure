package org.eventhorizonlab.kyoriadventuredsl.core

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.eventhorizonlab.core.api.TextComponentScope
import org.eventhorizonlab.spi.ServiceProvider

internal class DefaultTextComponentScope(
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
        DefaultTextComponentScope(childBuilder).init()
        builder.append(childBuilder.build())
    }

    fun build() = builder.build()

    @ServiceProvider(TextComponentScope.Factory::class)
    class Factory : TextComponentScope.Factory {
        override fun create(init: TextComponentScope.() -> Unit): TextComponent {
            val builder = Component.text()
            return DefaultTextComponentScope(builder).apply(init).build()
        }
    }
}