package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.text.component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

internal class MiniMessageResolverBuilder : MiniMessageResolverScope {
    private val resolvers: MutableList<TagResolver> = mutableListOf()

    override fun parsed(
        name: String,
        value: String,
    ) {
        resolvers += Placeholder.parsed(name, value)
    }

    override fun unparsed(
        name: String,
        value: String,
    ) {
        resolvers += Placeholder.unparsed(name, value)
    }

    override fun component(
        name: String,
        value: ComponentLike,
    ) {
        resolvers += Placeholder.component(name, value)
    }

    override fun component(
        name: String,
        init: ComponentScope.() -> Unit,
    ) {
        component(name, component(init))
    }

    override fun <T : Any> resolve(
        placeholder: MiniMessagePlaceholder<T>,
        value: T,
    ) {
        when (placeholder.strategy) {
            MiniMessagePlaceholderStrategy.COMPONENT -> {
                val componentValue =
                    value as? ComponentLike
                        ?: throw IllegalArgumentException(
                            "Placeholder '${placeholder.name}' expects a ComponentLike value.",
                        )

                component(placeholder.name, componentValue)
            }
            MiniMessagePlaceholderStrategy.LITERAL -> unparsed(placeholder.name, value.toString())
        }
    }

    internal fun build(): TagResolver = TagResolver.resolver(resolvers)
}
