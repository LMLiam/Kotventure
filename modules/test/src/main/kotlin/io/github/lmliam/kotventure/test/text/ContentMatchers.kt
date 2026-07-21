package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

/**
 * Returns a matcher that searches the flattened text content for [substring].
 *
 * Flattened text contains [TextComponent.content] from the root and descendants in depth-first
 * order. It includes text components that have children. Non-text components do not contribute
 * text, but their text-component descendants do contribute text.
 *
 * This matcher examines component structure. It does not use Adventure's plain-text serialiser.
 * Thus, it does not emit translation keys, score names, or other representations of non-text
 * components.
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
 * Returns a matcher that compares flattened text content with [text].
 *
 * This matcher uses the flattening rules of [containText]. It compares the exact concatenated
 * value and does not use Adventure's plain-text serialiser.
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
 * Verifies that the flattened text of this component tree contains [expected].
 *
 * @return this component, for chained assertions.
 * @throws AssertionError when the flattened text does not contain [expected].
 */
public infix fun Component.shouldContainText(expected: String): Component =
    apply {
        this should containText(expected)
    }

/**
 * Verifies that the flattened text of this component tree does not contain [expected].
 *
 * @return this component, for chained assertions.
 * @throws AssertionError when the flattened text contains [expected].
 */
public infix fun Component.shouldNotContainText(expected: String): Component =
    apply {
        this shouldNot containText(expected)
    }

/**
 * Verifies that the flattened text of this component tree equals [expected].
 *
 * @return this component, for chained assertions.
 * @throws AssertionError when the flattened text differs from [expected].
 */
public infix fun Component.shouldHaveContent(expected: String): Component =
    apply {
        this should haveContent(expected)
    }

/**
 * Verifies that the flattened text of this component tree differs from [expected].
 *
 * @return this component, for chained assertions.
 * @throws AssertionError when the flattened text equals [expected].
 */
public infix fun Component.shouldNotHaveContent(expected: String): Component =
    apply {
        this shouldNot haveContent(expected)
    }

/**
 * Concatenates the content of all text components in depth-first order.
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
