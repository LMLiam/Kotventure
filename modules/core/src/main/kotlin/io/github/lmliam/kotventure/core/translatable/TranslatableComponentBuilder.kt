package io.github.lmliam.kotventure.core.translatable

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument

internal class TranslatableComponentBuilder(
    key: String,
) : ComponentBuilder<TranslatableComponent, TranslatableComponent.Builder>(
    Component.translatable().key(key),
),
    TranslatableScope {
    private val arguments = mutableListOf<TranslationArgument>()

    override fun fallback(fallback: String) {
        builder.fallback(fallback)
    }

    override fun arg(value: ComponentLike) {
        arguments += TranslationArgument.component(value)
    }

    override fun arg(value: Boolean) {
        arguments += TranslationArgument.bool(value)
    }

    override fun arg(value: Number) {
        arguments += TranslationArgument.numeric(value)
    }

    override fun args(vararg values: ComponentLike) {
        arguments += values.map(TranslationArgument::component)
    }

    override fun args(vararg values: Boolean) {
        arguments += values.map(TranslationArgument::bool)
    }

    override fun args(vararg values: Number) {
        arguments += values.map(TranslationArgument::numeric)
    }

    override fun build(): Component = builder.arguments(arguments).build()
}
