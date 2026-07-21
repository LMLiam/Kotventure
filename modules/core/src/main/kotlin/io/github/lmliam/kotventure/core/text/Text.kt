package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Creates a text [Component] with literal [value], then applies [init].
 *
 * The function only constructs a value. It does not send the component to an audience.
 *
 * @sample io.github.lmliam.kotventure.core.text.textSample
 *
 * @param value the literal text content.
 * @param init styles the component and appends any children.
 * @throws IllegalStateException when [init] sets the content again, assigns another write-once slot twice, or applies a
 *   gradient to empty content.
 */
public fun text(
    value: String,
    init: TextScope.() -> Unit = {},
): Component = buildTextComponent(value, init)

internal fun buildTextComponent(
    value: String,
    init: TextScope.() -> Unit = {},
): Component =
    buildTextComponent {
        content(value)
        init()
    }

/**
 * Creates a text [Component] whose content, style, and children come from [init].
 *
 * If [init] does not set content, the result has empty literal content. The function only constructs a value.
 *
 * @throws IllegalStateException when [init] assigns a write-once slot more than once or applies a gradient to empty
 *   content.
 */
public fun text(init: TextScope.() -> Unit): Component = buildTextComponent(init)

internal fun buildTextComponent(init: TextScope.() -> Unit): Component = TextBuilder().apply(init).build()

/**
 * Appends a text child with literal [value] to this component scope.
 *
 * @param value the literal text content.
 * @param init styles the child and appends any of its own children.
 * @throws IllegalStateException when [init] sets the content again, assigns another write-once slot twice, or applies a
 *   gradient to empty content.
 */
public fun ComponentScope.text(
    value: String,
    init: TextScope.() -> Unit = {},
) {
    append(buildTextComponent(value, init))
}

/**
 * Appends a text child whose content, style, and children come from [init].
 *
 * @throws IllegalStateException when [init] assigns a write-once slot more than once or applies a gradient to empty
 *   content.
 */
public fun ComponentScope.text(init: TextScope.() -> Unit) {
    append(buildTextComponent(init))
}

/**
 * Appends a styled text child to the surrounding component, using the string literal as its content.
 *
 * This is the string-literal short form of [text]. `"Hello" { color(gold) }` is the same as
 * `text("Hello") { color(gold) }`. Thus, the content starts the call in a `component { }` block. For plain text with no
 * style, use [unaryPlus] as `+"Hello"`. This function requires [init], so `"Hello"()` does not compile.
 *
 * @sample io.github.lmliam.kotventure.core.text.stringInvokeSample
 *
 * @param init styles the child and appends any of its own children.
 */
context(scope: ComponentScope)
public operator fun String.invoke(init: TextScope.() -> Unit) {
    scope.text(this, init)
}

/**
 * Appends a plain text child to the surrounding component, using the string literal as its content.
 *
 * This is the documented shorthand for an unstyled text child inside a `component { }` block: `+"Hello"` is
 * exactly `text("Hello")`. Reach for the [invoke] form (`"Hello" { ... }`) when the child needs styling.
 *
 * @sample io.github.lmliam.kotventure.core.text.stringUnaryPlusSample
 */
context(scope: ComponentScope)
public operator fun String.unaryPlus() {
    scope.text(this)
}
