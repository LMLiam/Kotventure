package org.eventhorizonlab.core.api

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.*

@DslMarker
internal annotation class AdventureDsl

@AdventureDsl
interface TextComponentScope {
    fun content(content: String)

    fun color(color: NamedTextColor)

    fun decorate(vararg decorations: TextDecoration)

    fun text(init: TextComponentScope.() -> Unit)

    interface Factory {
        fun create(init: TextComponentScope.() -> Unit): TextComponent
    }
}

fun textComponent(init: TextComponentScope.() -> Unit): TextComponent {
    val loader = ServiceLoader.load(TextComponentScope.Factory::class.java)
    val apiClassLoader = TextComponentScope::class.java.classLoader

    val factory = loader.firstOrNull {
        it.javaClass.classLoader != apiClassLoader
    } ?: loader.firstOrNull()
    ?: error("No TextComponentScope.Factory implementation found")

    return factory.create(init)
}
