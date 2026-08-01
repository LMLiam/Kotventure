package io.github.lmliam.kotventure.minimessage.placeholder

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

internal class MiniMessageResolverBuilder : MiniMessageResolverScope {
    private val resolversByName =
        linkedMapOf<String, TagResolver>()

    override fun parsed(
        name: String,
        value: String,
    ) = addResolver(name) {
        Placeholder.parsed(name, value)
    }

    override fun unparsed(
        name: String,
        value: String,
    ) = addResolver(name) {
        Placeholder.unparsed(name, value)
    }

    override fun component(
        name: String,
        value: ComponentLike,
    ) = addResolver(name) {
        Placeholder.component(name, value)
    }

    override fun component(
        name: String,
        init: ComponentScope.() -> Unit,
    ) = component(
        name = name,
        value = component(init),
    )

    override fun <T : Any> resolve(
        placeholder: MiniMessagePlaceholder<T>,
        value: T,
    ) = addResolver(placeholder.name) {
        placeholder.toTagResolver(value)
    }

    internal fun build(): TagResolver = TagResolver.resolver(resolversByName.values)

    private inline fun addResolver(
        name: String,
        createResolver: () -> TagResolver,
    ) {
        name.requireValidMiniMessageTagName()

        require(name !in resolversByName) {
            "MiniMessage resolver '$name' is already defined."
        }

        resolversByName[name] = createResolver()
    }
}
