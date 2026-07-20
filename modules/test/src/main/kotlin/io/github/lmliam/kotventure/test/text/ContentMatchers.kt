package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

/**
 * Matches a component whose flattened text content contains [substring].
 *
 * Flattened text is the concatenation of every [TextComponent.content] in this tree
 * (self and descendants, depth-first — including non-leaf text nodes that also have
 * children). Non-text nodes (translatable, keybind, score, selector, NBT, object, …)
 * contribute **no** characters — only nested [TextComponent] nodes do. This is
 * intentional structure-aware matching, not a client plain-text render.
 *
 * Compare these matchers with Adventure's plain-text serialiser
 * (`net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer`) and
 * and Kotventure's `toPlainText()` wrapper. Those operations use serialisers and can emit keys, scores, or other
 * resolved forms. Use these matchers to examine the DSL or builder payload. Use plain-text serialisation to examine
 * what a player reads after complete serialisation.
 *
 * Combine with `and`/`or` or negate with `shouldNot`.
 */
public fun containText(substring: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.flattenedText()
        MatcherResult(
            substring in actual,
            { "Expected component text to contain <$substring>, but was <$actual>." },
            { "Expected component text not to contain <$substring>." },
        )
    }

/**
 * Matches a component whose flattened text content equals [text] exactly.
 *
 * Uses the same flattening rules as [containText]. Each [TextComponent] node supplies its content, not only leaf nodes.
 * This matcher compares the exact concatenated value and not Adventure plain-text serialiser output.
 */
public fun haveContent(text: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.flattenedText()
        MatcherResult(
            actual == text,
            { "Expected component text to be <$text>, but was <$actual>." },
            { "Expected component text not to be <$text>." },
        )
    }

/**
 * Asserts that this component tree contains [expected] in its flattened
 * [TextComponent] content (see [containText]).
 */
public infix fun Component.shouldContainText(expected: String): Component =
    apply {
        this should containText(expected)
    }

/**
 * Asserts that this component tree does NOT contain [expected] in its flattened
 * [TextComponent] content (see [containText]).
 */
public infix fun Component.shouldNotContainText(expected: String): Component =
    apply {
        this shouldNot containText(expected)
    }

/**
 * Asserts that this component's flattened [TextComponent] content equals
 * [expected] exactly (see [haveContent]).
 */
public infix fun Component.shouldHaveContent(expected: String): Component =
    apply {
        this should haveContent(expected)
    }

/**
 * Asserts that this component's flattened [TextComponent] content does NOT
 * equal [expected] (see [haveContent]).
 */
public infix fun Component.shouldNotHaveContent(expected: String): Component =
    apply {
        this shouldNot haveContent(expected)
    }

/**
 * Concatenates [TextComponent.content] for every [TextComponent] in this tree
 * in depth-first order. The function skips non-[TextComponent] nodes and does not serialise them as plain text.
 */
private fun Component.flattenedText(): String =
    buildString {
        appendFlattenedText(this@flattenedText)
    }

private fun StringBuilder.appendFlattenedText(component: Component) {
    if (component is TextComponent) {
        append(component.content())
    }
    component.children().forEach { child -> appendFlattenedText(child) }
}
