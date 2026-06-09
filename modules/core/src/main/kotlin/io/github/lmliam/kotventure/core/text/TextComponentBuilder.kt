package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.component.AbstractComponentScopeBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

internal class TextComponentBuilder :
    AbstractComponentScopeBuilder<TextComponent, TextComponent.Builder>(Component.text()),
    TextScope {
    override fun content(value: String) {
        builder.content(value)
    }
}
