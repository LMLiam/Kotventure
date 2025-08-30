package org.eventhorizonlab.kyoriadventuredsl.api

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.eventhorizonlab.kyoriadventuredsl.AdventureDsl

@AdventureDsl
interface TextComponentScope {
    fun content(content: String)
    fun color(color: NamedTextColor)
    fun decorate(vararg decorations: TextDecoration)
    fun text(init: TextComponentScope.() -> Unit)
}