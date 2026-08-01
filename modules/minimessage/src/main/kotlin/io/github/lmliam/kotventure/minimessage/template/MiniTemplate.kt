package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.validation.ValidationResult
import io.github.lmliam.kotventure.minimessage.validation.runValidation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import io.github.lmliam.kotventure.minimessage.placeholder.placeholder as createPlaceholder

/**
 * A typed, reusable MiniMessage template with required placeholders.
 *
 * Subclass this type and declare each placeholder during construction. Prefer delegated properties so the Kotlin
 * property name becomes the MiniMessage tag name. Each descriptor carries its value type, making template bindings
 * type-safe at the call site.
 *
 * Use [invoke] to render a component.
 *
 * @sample io.github.lmliam.kotventure.minimessage.template.miniTemplateRenderSample
 *
 * Validation is lazy, cached, and thread-safe. The first validation or render freezes the template definition.
 * Placeholder declarations made after that point are rejected.
 *
 * @param markup the MiniMessage markup rendered by this template.
 *
 * @throws IllegalArgumentException when [markup] is blank.
 */
public abstract class MiniTemplate(
    internal val markup: String,
) {
    init {
        require(markup.isNotBlank()) {
            "MiniMessage template markup must not be blank."
        }
    }

    /**
     * The placeholders declared by this template, in declaration order.
     *
     * The explicit backing field allows mutation only inside this class while exposing a read-only map elsewhere in
     * the module.
     */
    internal val declaredPlaceholders: Map<String, MiniMessagePlaceholder<*>>
        field: LinkedHashMap<String, MiniMessagePlaceholder<*>> = linkedMapOf()

    @Volatile
    private var declarationsFrozen = false

    private val parser: MiniMessage =
        MiniMessage.miniMessage()

    internal val validation: ValidationResult by lazy {
        declarationsFrozen = true

        runValidation(
            input = markup,
            placeholders = declaredPlaceholders.values.toList(),
        )
    }

    /**
     * Declares a required placeholder whose tag name is the delegated property's name.
     *
     * Prefer this form when the Kotlin property and MiniMessage tag use the same name.
     *
     * @throws IllegalArgumentException when [T] is unsupported or the property name is invalid or duplicated.
     * @throws IllegalStateException when template validation has already started.
     */
    protected inline fun <reified T : Any> placeholder(): PropertyDelegateProvider<
            MiniTemplate,
            ReadOnlyProperty<MiniTemplate, MiniMessagePlaceholder<T>>,
            > =
        PropertyDelegateProvider { template, property ->
            val descriptor =
                template.register(
                    createPlaceholder<T>(property.name),
                )

            ReadOnlyProperty { _, _ -> descriptor }
        }

    /**
     * Declares a required placeholder with an explicit MiniMessage tag [name].
     *
     * Use this form when an external or legacy tag name differs from the Kotlin property name. Otherwise, prefer the
     * delegated [placeholder] form.
     *
     * @throws IllegalArgumentException when [T] is unsupported or [name] is invalid or duplicated.
     * @throws IllegalStateException when template validation has already started.
     */
    protected inline fun <reified T : Any> placeholder(name: String): MiniMessagePlaceholder<T> =
        register(
            createPlaceholder<T>(name),
        )

    @PublishedApi
    internal fun <T : Any> register(descriptor: MiniMessagePlaceholder<T>): MiniMessagePlaceholder<T> {
        check(!declarationsFrozen) {
            "Cannot declare placeholder '${descriptor.name}' after template validation has started."
        }

        require(
            declaredPlaceholders.put(
                key = descriptor.name,
                value = descriptor,
            ) == null,
        ) {
            "Duplicate placeholder '${descriptor.name}' in template."
        }

        return descriptor
    }

    internal fun requireValidDefinition() {
        val result = validation

        require(result is ValidationResult.Success) {
            "MiniMessage template is invalid: $result."
        }
    }

    internal fun deserialize(resolver: TagResolver): Component =
        parser.deserialize(
            markup,
            resolver,
        )
}
