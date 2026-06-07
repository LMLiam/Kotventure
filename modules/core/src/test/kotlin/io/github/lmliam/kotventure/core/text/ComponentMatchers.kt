package io.github.lmliam.kotventure.core.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor

internal infix fun Component.shouldContainText(expected: String): Component =
    apply {
        this should haveTextContent(expected)
    }

internal infix fun Component.shouldHaveColor(expected: TextColor): Component =
    apply {
        this should haveColor(expected)
    }

internal infix fun Component.shouldHaveStyle(expected: Style): Component =
    apply {
        this should haveStyle(expected)
    }

internal infix fun Component.shouldHaveChildCount(expected: Int): Component =
    apply {
        this should haveChildCount(expected)
    }

internal fun Component.childAt(index: Int): Component {
    val children = children()
    check(index in children.indices) {
        "Expected child at index <$index>, but component has <${children.size}> children."
    }
    return children[index]
}

private fun haveTextContent(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = (value as? TextComponent)?.content()
        MatcherResult(
            actual == expected,
            { "Expected text content <$expected>, but was <$actual>." },
            { "Expected text content not to be <$expected>." },
        )
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
