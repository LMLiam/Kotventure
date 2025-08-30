package org.eventhorizonlab.kyoriadventuredsl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.eventhorizonlab.kyoriadventuredsl.api.TextComponentScope

@DslMarker
internal annotation class AdventureDsl

@AdventureDsl
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
}

fun textComponent(init: TextComponentScope.() -> Unit): TextComponent {
    val builder = Component.text()
    return DefaultTextComponentScope(builder).apply(init).build()
}