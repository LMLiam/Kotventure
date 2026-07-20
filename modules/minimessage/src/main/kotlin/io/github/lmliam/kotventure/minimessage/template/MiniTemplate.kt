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
 * Typed, reusable MiniMessage template with declared placeholders.
 *
 * Subclass and declare each placeholder as a delegated property so the property name is the tag
 * name (compile-checked at call sites). Render with the [invoke] operator and bind values with
 * [bind][MiniTemplateBindingScope.bind]:
 *
 * @sample io.github.lmliam.kotventure.minimessage.template.miniTemplateRenderSample
 *
 * @param markup the MiniMessage markup string that this template renders. It must not be blank.
 * @throws IllegalArgumentException when [markup] is blank.
 */
public abstract class MiniTemplate(
    internal val markup: String,
) {
    init {
        require(markup.isNotBlank()) { "MiniMessage template markup must not be blank." }
    }

    @PublishedApi
    internal val placeholders: LinkedHashMap<String, MiniMessagePlaceholder<*>> = LinkedHashMap()

    private val miniMessage: MiniMessage = MiniMessage.miniMessage()

    internal val validation: ValidationResult by lazy(LazyThreadSafetyMode.PUBLICATION) {
        runValidation(markup, placeholders.values.toList())
    }

    /**
     * Declares a required placeholder whose MiniMessage tag name is this property's name.
     *
     * Prefer this form so the Kotlin property and the markup tag cannot drift:
     * `val player by placeholder<Component>()`.
     *
     * @throws IllegalArgumentException when the property name is not a valid MiniMessage tag, is
     *   already declared, or [T] is unsupported.
     */
    protected inline fun <reified T : Any> placeholder():
            PropertyDelegateProvider<MiniTemplate, ReadOnlyProperty<MiniTemplate, MiniMessagePlaceholder<T>>> =
        PropertyDelegateProvider { template, property ->
            val registered = template.register(createPlaceholder<T>(property.name))
            ReadOnlyProperty { _, _ -> registered }
        }

    /**
     * Declares a required placeholder with an explicit MiniMessage tag [name].
     *
     * Use only when the tag must differ from the Kotlin property name (interop / legacy markup).
     * Prefer [placeholder] with no name so the property name is the tag.
     *
     * @throws IllegalArgumentException when [name] is invalid or already declared, or [T] is unsupported.
     */
    protected inline fun <reified T : Any> placeholder(name: String): MiniMessagePlaceholder<T> =
        register(createPlaceholder<T>(name))

    @PublishedApi
    internal fun <T : Any> register(descriptor: MiniMessagePlaceholder<T>): MiniMessagePlaceholder<T> {
        require(placeholders.put(descriptor.name, descriptor) == null) {
            "Duplicate placeholder '${descriptor.name}' in template."
        }
        return descriptor
    }

    internal fun deserialize(resolver: TagResolver): Component = miniMessage.deserialize(markup, resolver)
}

/**
 * Renders this template after [block] binds every declared placeholder.
 *
 * @throws IllegalArgumentException when the template definition is invalid, or a placeholder is missing,
 *   foreign, or bound more than once.
 */
public operator fun <T : MiniTemplate> T.invoke(block: context(MiniTemplateBindingScope) T.() -> Unit): Component {
    require(validation is ValidationResult.Success) { "MiniMessage template is invalid: $validation." }

    val bindings = TemplateBindings(this)
    context(bindings) { block() }
    bindings.requireComplete()

    return deserialize(bindings.resolver())
}
