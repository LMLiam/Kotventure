package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import io.github.lmliam.kotventure.minimessage.placeholder as createPlaceholder

/**
 * Base class for typed, reusable MiniMessage templates.
 *
 * Subclass as an `object` to declare a template with compile-checked typed placeholder properties:
 *
 * ```kotlin
 * import io.github.lmliam.kotventure.minimessage.MiniTemplate
 * import io.github.lmliam.kotventure.minimessage.bind
 * import io.github.lmliam.kotventure.minimessage.invoke
 * import net.kyori.adventure.text.Component
 *
 * object WelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages") {
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
 * The [MiniMessage] instance and the template's declared-placeholder metadata and markup string are
 * created once per template instance and reused on every render; each [invoke] call returns a fresh,
 * independent [Component].
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

    /**
     * Ordered map of placeholder name to descriptor, populated at object-construction time by calls
     * to [placeholder]. [LinkedHashMap] preserves declaration order so error messages list missing
     * placeholder names in the order they were declared.
     *
     * `@PublishedApi internal` because the `inline reified` [placeholder] member is inlined into
     * subclass call sites, which means it cannot touch a `private` field.
     */
    @PublishedApi
    internal val placeholders: LinkedHashMap<String, MiniMessagePlaceholder<*>> = LinkedHashMap()

    /** Cached MiniMessage instance, reused across every render of this template. */
    private val miniMessage: MiniMessage = MiniMessage.miniMessage()

    /**
     * Declares a required placeholder on this template. Call at object-construction time:
     * `val x = placeholder<T>("x")`.
     *
     * Returns the descriptor unchanged so it can be stored as a `val` and referenced at render time.
     * `inline reified` so it can delegate to the top-level [createPlaceholder] factory from #26,
     * forwarding the reified type without requiring a `Class` or `KClass` argument. The member is
     * non-`open`, satisfying the Kotlin requirement that `inline` members are not virtual.
     *
     * @param T the value type that must be supplied when binding this placeholder; must belong to
     *   one of the supported families: [net.kyori.adventure.text.ComponentLike], [String],
     *   [Number], or [Boolean].
     * @param name the string argument that defines both the MiniMessage tag resolved in the markup
     *   and the name by which the descriptor is tracked; the returned `val` gives compile-checked
     *   scoped access to the descriptor.
     * @return the registered [MiniMessagePlaceholder] descriptor.
     * @throws IllegalArgumentException when [name] is already declared on this template, when [T]
     *   is outside the supported value families, or when [name] is not a valid MiniMessage tag name.
     */
    protected inline fun <reified T : Any> placeholder(name: String): MiniMessagePlaceholder<T> =
        // Delegates to the top-level factory via import alias to avoid shadowing.
        register(createPlaceholder<T>(name))

    /**
     * Records [descriptor] in the declared-placeholder set, rejecting duplicate names. Non-inline
     * so the mutation and duplicate check stay encapsulated behind a single call site in the inline
     * [placeholder] hook.
     *
     * @param descriptor the placeholder to register.
     * @return [descriptor] unchanged.
     * @throws IllegalArgumentException when a placeholder with the same name is already declared.
     */
    @PublishedApi
    internal fun <T : Any> register(descriptor: MiniMessagePlaceholder<T>): MiniMessagePlaceholder<T> {
        require(placeholders.put(descriptor.name, descriptor) == null) {
            "Duplicate placeholder '${descriptor.name}' in template."
        }
        return descriptor
    }

    /**
     * Deserializes [markup] with [resolver] and returns a fresh [Component].
     *
     * Called by the top-level [invoke] extension after validating all bindings. The method is
     * `internal` rather than `private` so the extension function can reach it without reflection,
     * while keeping the low-level deserialization detail out of the public surface.
     */
    internal fun deserialize(resolver: TagResolver): Component = miniMessage.deserialize(markup, resolver)

    /**
     * The names of every placeholder this template declares, in declaration order.
     *
     * Useful for introspection or for generating helpful error messages in higher-level utilities.
     */
    public val declaredPlaceholders: Set<String>
        get() = placeholders.keys.toSet()
}

/**
 * Binds [value] to [placeholder] inside a [MiniTemplate] render lambda.
 *
 * The concrete template is the extension receiver, which keeps template placeholder properties in
 * scope, while the context parameter supplies the per-render state used by
 * [MiniTemplateBindingScope.bind].
 *
 * @param placeholder a descriptor declared on this template.
 * @param value the value to substitute for [placeholder].
 */
context(_: MiniTemplateBindingScope) public fun <T : Any> MiniTemplate.bind(
    placeholder: MiniMessagePlaceholder<T>,
    value: T,
): Unit = contextOf<MiniTemplateBindingScope>().bind(placeholder, value)

/**
 * Renders this template by binding every declared placeholder via [bind], then deserializing the
 * markup with the built [TagResolver].
 *
 * The render lambda uses the concrete template as its receiver and carries a
 * [MiniTemplateBindingScope] context parameter, so placeholder descriptors declared on the template
 * (e.g. `player`, `count`) are accessible unqualified while [bind] is available in the same block.
 *
 * Per-render state (the resolver builder and bound-name set) is created fresh on each call and
 * never touches the shared template object, preserving thread-safety and declare-once semantics.
 *
 * Validates that every declared placeholder was bound before rendering; throws
 * [IllegalArgumentException] listing the missing name(s). Also rejects binding a placeholder not
 * declared on this template, or a descriptor from a different template that happens to share a name.
 *
 * @param block lambda that receives the template [T] as receiver and a [MiniTemplateBindingScope]
 *   as context.
 * @return a fresh [Component] for this render call; independent of all prior and future renders.
 * @throws IllegalArgumentException when any declared placeholder is not bound, or when [block]
 *   attempts to bind a placeholder not declared on this template (checked by descriptor identity),
 *   or when the same placeholder is bound more than once in a single render.
 */
public operator fun <T : MiniTemplate> T.invoke(block: context(MiniTemplateBindingScope) T.() -> Unit): Component {
    val builder = MiniMessageResolverBuilder()
    val boundNames = mutableSetOf<String>()

    val scope =
        object : MiniTemplateBindingScope {
            override fun <V : Any> bind(
                placeholder: MiniMessagePlaceholder<V>,
                value: V,
            ) {
                require(placeholders[placeholder.name] === placeholder) {
                    "Placeholder '${placeholder.name}' is not declared on this template. " +
                        "Declared placeholders: ${placeholders.keys}."
                }
                require(boundNames.add(placeholder.name)) {
                    "Placeholder '${placeholder.name}' is already bound in this template render."
                }
                builder.resolve(placeholder, value)
            }
        }

    context(scope) { block() }

    val missing = placeholders.keys - boundNames
    require(missing.isEmpty()) {
        "Template is missing required placeholder(s): $missing."
    }

    return deserialize(builder.build())
}
