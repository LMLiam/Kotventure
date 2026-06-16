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
 * The flattened text concatenates the [TextComponent] content of the component and every descendant, so the match
 * succeeds when [substring] appears anywhere in the rendered text. Combine with `and`/`or` or negate with `shouldNot`.
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
 * Unlike [containText] this is an exact-equality check over the concatenated [TextComponent] content of the component
 * and its descendants.
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
 * Asserts that this component tree contains [expected] in its flattened text content.
 */
public infix fun Component.shouldContainText(expected: String): Component =
    apply {
        this should containText(expected)
    }

/**
 * Asserts that this component tree does NOT contain [expected] in its flattened text content.
 */
public infix fun Component.shouldNotContainText(expected: String): Component =
    apply {
        this shouldNot containText(expected)
    }

/**
 * Asserts that this component's flattened text content equals [expected] exactly.
 */
public infix fun Component.shouldHaveContent(expected: String): Component =
    apply {
        this should haveContent(expected)
    }

/**
 * Asserts that this component's flattened text content does NOT equal [expected].
 */
public infix fun Component.shouldNotHaveContent(expected: String): Component =
    apply {
        this shouldNot haveContent(expected)
    }

/** Concatenates the [TextComponent] content of this component and every descendant in render order. */
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
