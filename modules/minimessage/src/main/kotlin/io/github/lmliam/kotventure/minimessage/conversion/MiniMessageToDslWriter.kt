package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

internal object MiniMessageToDslWriter {
    fun write(component: Component): String {
        if (component.isEmptyComponent()) return "component {}"

        return buildKotlinSource {
            block("component") {
                appendRoot(component)
            }
        }
    }

    private fun Component.isEmptyComponent(): Boolean =
        this is TextComponent &&
                content().isEmpty() &&
                children().isEmpty() &&
                !hasDslOutput(style())
}
