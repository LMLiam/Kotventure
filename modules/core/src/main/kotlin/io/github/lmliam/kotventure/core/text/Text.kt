package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Builds a text [Component] with [value] as its literal content.
 *
 * @sample io.github.lmliam.kotventure.core.text.textSample
 *
 * @param value the literal text content.
 * @param init styles the component and appends any children.
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
 * Builds a text [Component] whose content is set inside [init] via `content(...)`, for when content and
 * styling are configured together.
 */
public fun text(init: TextScope.() -> Unit): Component = buildTextComponent(init)

internal fun buildTextComponent(init: TextScope.() -> Unit): Component = TextBuilder().apply(init).build()

/**
 * Appends a text child with [value] as its content, for use inside a `component { }` or other component block.
 *
 * @param value the literal text content.
 * @param init styles the child and appends any of its own children.
 */
public fun ComponentScope.text(
    value: String,
    init: TextScope.() -> Unit = {},
) {
    append(buildTextComponent(value, init))
}

/**
 * Appends a text child whose content is set inside [init] via `content(...)`.
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
