package io.github.lmliam.kotventure.minimessage.template

import net.kyori.adventure.text.Component

/**
 * Renders this template after [block] binds every declared placeholder.
 *
 * The template definition is validated before [block] runs. Each placeholder must be bound exactly once using the
 * descriptor instance declared by this template. Scalar values become literal text, while component values retain
 * their component structure.
 *
 * Bindings are local to this invocation and are discarded after rendering.
 *
 * @throws IllegalArgumentException when the template definition is invalid, a required binding is missing, a
 * descriptor belongs to another template, or a placeholder is bound more than once.
 */
public operator fun <T : MiniTemplate> T.invoke(block: context(MiniTemplateBindingScope) T.() -> Unit): Component {
    requireValidDefinition()

    val bindings =
        MiniTemplateBindings(
            declaredPlaceholders = declaredPlaceholders,
        )

    context(bindings) {
        block()
    }

    return deserialize(bindings.build())
}
