package io.github.lmliam.kotventure.minimessage.placeholder

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

internal class MiniMessageResolverBuilder : MiniMessageResolverScope {
    private val resolvers: MutableList<TagResolver> = mutableListOf()
    private val names: MutableSet<String> = mutableSetOf()

    override fun parsed(
        name: String,
        value: String,
    ) {
        requireUniqueName(name)
        resolvers += Placeholder.parsed(name, value)
    }

    override fun unparsed(
        name: String,
        value: String,
    ) {
        requireUniqueName(name)
        resolvers += Placeholder.unparsed(name, value)
    }

    override fun component(
        name: String,
        value: ComponentLike,
    ) {
        requireUniqueName(name)
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

    private fun requireUniqueName(name: String) {
        requireValidMiniMessageTagName(name)
        require(names.add(name)) { "MiniMessage resolver '$name' is already defined." }
    }
}
