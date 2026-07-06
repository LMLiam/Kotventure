package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.color.ColorGradient
import io.github.lmliam.kotventure.core.component.ComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import io.github.lmliam.kotventure.core.color.gradient as colorGradient
import io.github.lmliam.kotventure.core.color.gradientText as gradientComponent

internal class TextBuilder :
    ComponentBuilder<TextComponent, TextComponent.Builder>(Component.text()),
    TextScope {
    private var gradient: ColorGradient? = null

    override fun content(value: String) {
        singleAssignments.assign("content")
        builder.content(value)
    }

    override fun gradient(gradient: ColorGradient) {
        singleAssignments.assign("gradient")
        this.gradient = gradient
    }

    override fun gradient(vararg stops: TextColor) {
        gradient(colorGradient(*stops))
    }

    override fun build(): Component {
        val component = builder.build()
        val gradient = gradient ?: return component
        val content = component.content()
        check(content.isNotEmpty()) { "'gradient' is set but 'content' is empty; a gradient needs text to color." }

        val builder = Component.text().style(component.style())
        gradientComponent(content, gradient).children().forEach { child -> builder.append(child) }
        component.children().forEach { child -> builder.append(child) }
        return builder.build()
    }
}
