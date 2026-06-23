package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessageResolverBuilder
import io.github.lmliam.kotventure.minimessage.validation.ValidationResult
import io.github.lmliam.kotventure.minimessage.validation.runValidation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import io.github.lmliam.kotventure.minimessage.placeholder.placeholder as createPlaceholder

/**
 * Typed, reusable MiniMessage template with declared placeholders.
 *
 * ```kotlin
 * import net.kyori.adventure.text.Component
 *
 * object WelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
 *     val player = placeholder<Component>("player")
 *     val count = placeholder<Int>("count")
 * }
 *
 * val forAlex = WelcomeTemplate {
 *     bind(player, Component.text("Alex"))
 *     bind(count, 3)
 * }
 * ```
 *
 * @param markup the MiniMessage markup string this template renders; must not be blank.
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

    private val validation: ValidationResult by lazy(LazyThreadSafetyMode.PUBLICATION) {
        runValidation(markup, placeholders.values.toList())
    }

    /**
     * Declares a required placeholder on this template.
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

    internal fun validateDefinition(): ValidationResult = validation
}

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

    fun requireComplete() {
        val missing = template.placeholders.keys - boundNames
        require(missing.isEmpty()) {
            "Template is missing required placeholder(s): $missing."
        }
    }

    fun resolver(): TagResolver = builder.build()
}

/** Binds [value] to [placeholder] inside a [MiniTemplate] render lambda. */
context(_: MiniTemplateBindingScope) public fun <T : Any> MiniTemplate.bind(
    placeholder: MiniMessagePlaceholder<T>,
    value: T,
): Unit = contextOf<MiniTemplateBindingScope>().bind(placeholder, value)

/**
 * Renders this template after [block] binds every declared placeholder.
 *
 * @throws IllegalArgumentException when the template is invalid, a placeholder is missing, a foreign
 *   placeholder is bound, or a placeholder is bound more than once.
 */
public operator fun <T : MiniTemplate> T.invoke(block: context(MiniTemplateBindingScope) T.() -> Unit): Component {
    val validation = validateDefinition()
    require(validation is ValidationResult.Success) { "MiniMessage template is invalid: $validation." }

    val bindings = TemplateBindings(this)

    context(bindings) { block() }
    bindings.requireComplete()

    return deserialize(bindings.resolver())
}
