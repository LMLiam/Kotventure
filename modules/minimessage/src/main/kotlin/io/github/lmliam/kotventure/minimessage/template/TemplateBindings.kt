package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessageResolverBuilder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Collects the placeholder bindings for a single [MiniTemplate] render and turns them into a resolver.
 */
internal class TemplateBindings(
    private val template: MiniTemplate,
) : MiniTemplateBindingScope {
    private val builder = MiniMessageResolverBuilder()
    private val boundNames = mutableSetOf<String>()

    override fun <T : Any> bind(
        placeholder: MiniMessagePlaceholder<T>,
        value: T,
    ) {
        require(template.placeholders[placeholder.name] === placeholder) {
            "Placeholder '${placeholder.name}' is not declared on this template. " +
                    "Declared placeholders: ${template.placeholders.keys}."
        }
        require(boundNames.add(placeholder.name)) {
            "Placeholder '${placeholder.name}' is already bound in this template render."
        }
        builder.resolve(placeholder, value)
    }

    /** Fails if any declared placeholder was not bound during the render block. */
    fun requireComplete() {
        val missing = template.placeholders.keys - boundNames
        require(missing.isEmpty()) {
            "Template is missing required placeholder(s): $missing."
        }
    }

    fun resolver(): TagResolver = builder.build()
}
