package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Base class for typed, reusable MiniMessage templates.
 *
 * Subclass as an `object` to declare a template with compile-checked typed placeholder properties:
 *
 * ```kotlin
 * object WelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages") {
 *     val player = placeholder<Component>("player")
 *     val count = placeholder<Int>("count")
 * }
 *
 * val forAlex = WelcomeTemplate {
 *     bind(WelcomeTemplate.player, Component.text("Alex"))
 *     bind(WelcomeTemplate.count, 3)
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
    private val markup: String,
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
     * `inline reified` so it can delegate to the public top-level [placeholder] factory from #26,
     * forwarding the reified type without requiring a `Class` or `KClass` argument. The member is
     * non-`open`, satisfying the Kotlin requirement that `inline` members are not virtual.
     *
     * @param T the value type that must be supplied when binding this placeholder; must belong to
     *   one of the supported families: [net.kyori.adventure.text.ComponentLike], [String],
     *   [Number], or [Boolean].
     * @param name the MiniMessage tag name that this placeholder resolves in the markup.
     * @return the registered [MiniMessagePlaceholder] descriptor.
     * @throws IllegalArgumentException when [name] is already declared on this template, when [T]
     *   is outside the supported value families, or when [name] is not a valid MiniMessage tag name.
     */
    protected inline fun <reified T : Any> placeholder(name: String): MiniMessagePlaceholder<T> =
        // Fully-qualified call to the public top-level factory avoids the unqualified call resolving
        // back to this member inside a subclass body (members win over top-level functions in scope).
        register(
            io.github.lmliam.kotventure.minimessage
                .placeholder<T>(name),
        )

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
     * Renders this template by binding every required placeholder via [bind], then deserializing the
     * markup with the built [net.kyori.adventure.text.minimessage.tag.resolver.TagResolver].
     *
     * Validates that every declared placeholder was bound before rendering; throws
     * [IllegalArgumentException] listing the missing name(s). Also rejects binding a placeholder not
     * declared on this template.
     *
     * Double-binding the same placeholder within one call follows first-wins semantics — the natural
     * default of [net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.resolver].
     *
     * @param bind lambda that receives a [MiniTemplateBindingScope] to supply placeholder values.
     * @return a fresh [Component] for this render call; independent of all prior and future renders.
     * @throws IllegalArgumentException when any declared placeholder is not bound, or when [bind]
     *   attempts to bind a placeholder not declared on this template.
     */
    public operator fun invoke(bind: MiniTemplateBindingScope.() -> Unit): Component {
        val builder = MiniMessageResolverBuilder()
        val boundNames = mutableSetOf<String>()

        val scope =
            object : MiniTemplateBindingScope {
            override fun <T : Any> bind(
                placeholder: MiniMessagePlaceholder<T>,
                value: T,
            ) {
                require(placeholders.containsKey(placeholder.name)) {
                    "Placeholder '${placeholder.name}' is not declared on this template. " +
                        "Declared placeholders: ${placeholders.keys}."
                }
                // Record first-bind only; double-bind is first-wins via TagResolver.resolver(list).
                if (boundNames.add(placeholder.name)) {
                    builder.resolve(placeholder, value)
                }
            }
        }

        scope.bind()

        val missing = placeholders.keys - boundNames
        require(missing.isEmpty()) {
            "Template is missing required placeholder(s): $missing."
        }

        return miniMessage.deserialize(markup, builder.build())
    }

    /**
     * The names of every placeholder this template declares, in declaration order.
     *
     * Useful for introspection or for generating helpful error messages in higher-level utilities.
     */
    public val requiredPlaceholders: Set<String>
        get() = placeholders.keys.toSet()
}
