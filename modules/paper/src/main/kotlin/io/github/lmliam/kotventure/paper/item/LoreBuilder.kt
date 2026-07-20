package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class LoreBuilder : LoreScope {
    val lines: List<Component>
        field = mutableListOf()

    override fun String.unaryPlus() {
        lines += text(this).nonItalicByDefault()
    }

    override fun String.invoke(init: TextScope.() -> Unit) {
        lines += text(this, init).nonItalicByDefault()
    }

    override fun ComponentLike.unaryPlus() {
        lines += asComponent().nonItalicByDefault()
    }

    override fun blank() {
        lines += Component.empty()
    }

    internal fun build(): List<Component> = lines
}
