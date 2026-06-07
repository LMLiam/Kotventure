package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor

/**
 * Asserts that this component tree contains [expected] in its text content.
 */
public infix fun Component.shouldContainText(expected: String): Component =
    apply {
        this should haveTextContent(expected)
    }

/**
 * Asserts that this component has [expected] as its root color.
 */
public infix fun Component.shouldHaveColor(expected: TextColor): Component =
    apply {
        this should haveColor(expected)
    }

/**
 * Asserts that this component has exactly [expected] as its root Adventure style.
 */
public infix fun Component.shouldHaveStyle(expected: Style): Component =
    apply {
        this should haveStyle(expected)
    }

/**
 * Asserts that this component has exactly [expected] direct child components.
 */
public infix fun Component.shouldHaveChildCount(expected: Int): Component =
    apply {
        this should haveChildCount(expected)
    }

/**
 * Returns this component's child at [index], or fails with a readable test error.
 */
public fun Component.childAt(index: Int): Component {
    val children = children()
    check(index in children.indices) {
        "Expected child at index <$index>, but component has <${children.size}> children."
    }
    return children[index]
}

private fun haveTextContent(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.textContent()
        MatcherResult(
            expected in actual,
            { "Expected component text to contain <$expected>, but was <$actual>." },
            { "Expected component text not to contain <$expected>." },
        )
    }

private fun Component.textContent(): String =
    buildString {
        appendText(this@textContent)
    }

private fun StringBuilder.appendText(component: Component) {
    if (component is TextComponent) {
        append(component.content())
    }
    component.children().forEach { child -> appendText(child) }
}

private fun haveColor(expected: TextColor): Matcher<Component> =
    Matcher { value ->
        val actual = value.color()
        MatcherResult(
            actual == expected,
            { "Expected component color <$expected>, but was <$actual>." },
            { "Expected component color not to be <$expected>." },
        )
    }

private fun haveStyle(expected: Style): Matcher<Component> =
    Matcher { value ->
        val actual = value.style()
        MatcherResult(
            actual == expected,
            { "Expected component style <$expected>, but was <$actual>." },
            { "Expected component style not to be <$expected>." },
        )
    }

private fun haveChildCount(expected: Int): Matcher<Component> =
    Matcher { value ->
        val actual = value.children().size
        MatcherResult(
            actual == expected,
            { "Expected <$expected> child components, but found <$actual>." },
            { "Expected child component count not to be <$expected>." },
        )
    }
