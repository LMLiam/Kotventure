package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.component.ComponentScopeBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

internal class TextComponentBuilder :
    ComponentScopeBuilder<TextComponent, TextComponent.Builder>(Component.text()),
    TextScope {
    override fun content(value: String) {
        builder.content(value)
    }
}
