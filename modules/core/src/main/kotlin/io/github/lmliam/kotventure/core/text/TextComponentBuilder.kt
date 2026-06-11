package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.color.ColorGradient
import io.github.lmliam.kotventure.core.component.ComponentScopeBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import io.github.lmliam.kotventure.core.color.gradient as colorGradient
import io.github.lmliam.kotventure.core.color.gradientText as gradientComponent

internal class TextComponentBuilder :
    ComponentScopeBuilder<TextComponent, TextComponent.Builder>(Component.text()),
    TextScope {
    private var gradient: ColorGradient? = null

    override fun content(value: String) {
        builder.content(value)
    }

    override fun gradient(gradient: ColorGradient) {
        this.gradient = gradient
    }

    override fun gradient(vararg stops: TextColor) {
        gradient(colorGradient(*stops))
    }

    override fun build(): Component {
        val component = builder.build()
        val gradient = gradient ?: return component
        val content = component.content()
        if (content.isEmpty()) {
            return component
        }

        val builder = Component.text().style(component.style())
        gradientComponent(content, gradient).children().forEach { child -> builder.append(child) }
        component.children().forEach { child -> builder.append(child) }
        return builder.build()
    }
}
