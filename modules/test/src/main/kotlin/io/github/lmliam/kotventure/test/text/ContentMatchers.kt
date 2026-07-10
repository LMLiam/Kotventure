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
 * Contrast Adventure's
 * [net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer] (and
 * Kotventure's `toPlainText()` wrapper around it): those walk serializers and may
 * emit keys, scores, or other resolved forms. Prefer these matchers when asserting
 * DSL/builder payload shape; use plain-text serialization when asserting what a
 * player would read after full serialization.
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
 * Same flattening rules as [containText]: every [TextComponent] node contributes
 * its content (not only leaves); this is exact equality over that concatenation, not
 * [net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer] output.
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
 * (depth-first). Non-[TextComponent] nodes are skipped (not plain-text serialized).
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
