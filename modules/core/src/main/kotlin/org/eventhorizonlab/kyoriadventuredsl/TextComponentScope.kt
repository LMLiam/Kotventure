package org.eventhorizonlab.kyoriadventuredsl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

@DslMarker
annotation class AdventureDsl

@AdventureDsl
class TextComponentScope internal constructor(
    private val builder: TextComponent.Builder
) {
    fun content(content: String) {
        builder.content(content)
    }

    fun color(color: NamedTextColor) {
        builder.color(color)
    }

    fun decorate(vararg decorations: TextDecoration) {
        decorations.forEach { builder.decoration(it, true) }
    }

    fun text(init: TextComponentScope.() -> Unit) {
        val childBuilder = Component.text()
        TextComponentScope(childBuilder).init()
        builder.append(childBuilder.build())
    }

    internal fun build() = builder.build()
}

fun textComponent(init: TextComponentScope.() -> Unit): TextComponent {
    val builder = Component.text()
    return TextComponentScope(builder).apply(init).build()
}