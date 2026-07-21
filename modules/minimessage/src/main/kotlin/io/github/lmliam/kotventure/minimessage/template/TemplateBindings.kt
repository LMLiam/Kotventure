package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessageResolverBuilder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Collects the bindings for one [MiniTemplate] render.
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

    /** Fails if a declared placeholder has no binding. */
    fun requireComplete() {
        val missing = template.placeholders.keys - boundNames
        require(missing.isEmpty()) {
            "Template is missing required placeholder(s): $missing."
        }
    }

    fun resolver(): TagResolver = builder.build()
}
