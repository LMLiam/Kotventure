package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.validation.ValidationResult
import io.github.lmliam.kotventure.minimessage.validation.runValidation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import io.github.lmliam.kotventure.minimessage.placeholder.placeholder as createPlaceholder

/**
 * Typed, reusable MiniMessage template with declared placeholders.
 *
 * Subclass and declare each placeholder as a property; rendering invokes the template and binds every
 * placeholder with the [bind][MiniTemplateBindingScope.bind] infix function:
 *
 * ```kotlin
 * object WelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
 *     val player = placeholder<Component>("player")
 *     val count = placeholder<Int>("count")
 * }
 *
 * val forAlex = WelcomeTemplate {
 *     player bind component { text("Alex") }
 *     count bind 3
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

    internal val validation: ValidationResult by lazy(LazyThreadSafetyMode.PUBLICATION) {
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
