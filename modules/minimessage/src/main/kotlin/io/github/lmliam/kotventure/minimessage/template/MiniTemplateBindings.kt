package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessageResolverBuilder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Collects and validates the bindings for one template render.
 */
internal class MiniTemplateBindings(
    private val declaredPlaceholders: Map<String, MiniMessagePlaceholder<*>>,
) : MiniTemplateBindingScope {
    private val resolverBuilder =
        MiniMessageResolverBuilder()

    private val unboundNames =
        LinkedHashSet(declaredPlaceholders.keys)

    override fun <T : Any> bind(
        placeholder: MiniMessagePlaceholder<T>,
        value: T,
    ) {
        val declared = declaredPlaceholders[placeholder.name]

        require(declared === placeholder) {
            "Placeholder '${placeholder.name}' is not declared by this template. " +
                    "Declared placeholders: ${declaredPlaceholders.keys}."
        }

        require(unboundNames.remove(placeholder.name)) {
            "Placeholder '${placeholder.name}' is already bound in this template render."
        }

        resolverBuilder.resolve(
            placeholder = placeholder,
            value = value,
        )
    }

    /**
     * Verifies that every declared placeholder was bound and builds the resulting resolver.
     */
    internal fun build(): TagResolver {
        require(unboundNames.isEmpty()) {
            "Template is missing required placeholder(s): $unboundNames"
        }

        return resolverBuilder.build()
    }
}
