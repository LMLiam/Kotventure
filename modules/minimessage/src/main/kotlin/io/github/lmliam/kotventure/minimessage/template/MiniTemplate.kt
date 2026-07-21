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
 * Subclass this type and declare each placeholder during construction. Prefer delegated properties because the Kotlin
 * property name then becomes the MiniMessage tag name. The placeholder type makes each binding type-safe at the call
 * site. Use [invoke] to render a new component.
 *
 * @sample io.github.lmliam.kotventure.minimessage.template.miniTemplateRenderSample
 *
 * Validation is lazy and cached. Do not declare more placeholders after the first call to [invoke] or
 * [validate][io.github.lmliam.kotventure.minimessage.validate]. A fully constructed template supports concurrent
 * validation and rendering. Each render has independent bindings.
 *
 * @param markup The MiniMessage markup that this template renders. It must contain a non-whitespace character.
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
     * Prefer this form so the Kotlin property and markup tag have the same name.
     *
     * @throws IllegalArgumentException when [T] is unsupported, or when the property name is invalid or already
     * declared.
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
     * Use this string bridge only for an external or legacy template. The tag can differ from the Kotlin property
     * name. Otherwise, use [placeholder] with no name.
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
 * Renders a new component after [block] binds every declared placeholder.
 *
 * The function validates the template before it runs [block]. Each placeholder must be bound exactly one time with
 * the descriptor instance that this template declared. Scalar values become literal text. Component values retain
 * their structure. The function does not retain bindings after it returns.
 *
 * @throws IllegalArgumentException when the template definition is invalid, a binding is missing, a descriptor belongs
 * to another template, or a placeholder is bound more than one time.
 */
public operator fun <T : MiniTemplate> T.invoke(block: context(MiniTemplateBindingScope) T.() -> Unit): Component {
    require(validation is ValidationResult.Success) { "MiniMessage template is invalid: $validation." }

    val bindings = TemplateBindings(this)
    context(bindings) { block() }
    bindings.requireComplete()

    return deserialize(bindings.resolver())
}
