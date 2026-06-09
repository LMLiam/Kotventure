package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

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
 * Asserts that this component has [expected] enabled on its root style.
 */
public infix fun Component.shouldHaveDecoration(expected: TextDecoration): Component =
    apply {
        this should haveDecoration(expected)
    }

/**
 * Asserts that this component has no explicit [expected] state on its root style.
 */
public infix fun Component.shouldNotHaveDecoration(expected: TextDecoration): Component =
    apply {
        this should haveDecorationState(expected, State.NOT_SET)
    }

/**
 * Asserts that this component has exactly [expected] direct child components.
 */
public infix fun Component.shouldHaveChildCount(expected: Int): Component =
    apply {
        this should haveChildCount(expected)
    }

/**
 * Asserts that this component is translatable and has [expected] as its translation key.
 */
public infix fun Component.shouldHaveTranslationKey(expected: String): Component =
    apply {
        this should haveTranslationKey(expected)
    }

/**
 * Asserts that this component is translatable and has [expected] as its fallback text.
 */
public infix fun Component.shouldHaveFallback(expected: String): Component =
    apply {
        this should haveFallback(expected)
    }

/**
 * Asserts that this component is translatable and has no fallback text.
 */
public fun Component.shouldNotHaveFallback(): Component =
    apply {
        this should haveNoFallback()
    }

/**
 * Asserts that this component is translatable and has exactly [expected] translation arguments.
 */
public infix fun Component.shouldHaveArgumentCount(expected: Int): Component =
    apply {
        this should haveArgumentCount(expected)
    }

/**
 * Asserts that this component is translatable and has exactly [expected] translation arguments in order.
 */
public fun Component.shouldHaveArguments(vararg expected: TranslationArgument): Component =
    apply {
        this should haveArguments(expected.toList())
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

private fun haveDecoration(expected: TextDecoration): Matcher<Component> = haveDecorationState(expected, State.TRUE)

private fun haveDecorationState(
    expected: TextDecoration,
    state: State,
): Matcher<Component> =
    Matcher { value ->
        val actual = value.style().decoration(expected)
        MatcherResult(
            actual == state,
            {
                "Expected component decoration <$expected> to be <${state.name}>, " +
                        "but was <${actual.name}>."
            },
            { "Expected component decoration <$expected> not to be <${state.name}>." },
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

private fun haveTranslationKey(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.translatableOrNull()?.key()
        MatcherResult(
            actual == expected,
            { "Expected translation key <$expected>, but was <${actual ?: "not translatable"}>." },
            { "Expected translation key not to be <$expected>." },
        )
    }

private fun haveFallback(expected: String): Matcher<Component> =
    Matcher { value ->
        val translatable = value.translatableOrNull()
        val actual = translatable?.fallback()
        val actualDescription = if (translatable == null) "not translatable" else actual ?: "null"
        MatcherResult(
            translatable != null && actual == expected,
            { "Expected translatable fallback <$expected>, but was <$actualDescription>." },
            { "Expected translatable fallback not to be <$expected>." },
        )
    }

private fun haveNoFallback(): Matcher<Component> =
    Matcher { value ->
        val translatable = value.translatableOrNull()
        val actual = translatable?.fallback()
        MatcherResult(
            translatable != null && actual == null,
            { "Expected translatable fallback to be absent, but was <${actual ?: "not translatable"}>." },
            { "Expected translatable fallback to be present." },
        )
    }

private fun haveArgumentCount(expected: Int): Matcher<Component> =
    Matcher { value ->
        val actual = value.translatableOrNull()?.arguments()?.size
        MatcherResult(
            actual == expected,
            { "Expected <$expected> translation arguments, but found <${actual ?: "not translatable"}>." },
            { "Expected translation argument count not to be <$expected>." },
        )
    }

private fun haveArguments(expected: List<TranslationArgument>): Matcher<Component> =
    Matcher { value ->
        val actual = value.translatableOrNull()?.arguments()
        MatcherResult(
            actual == expected,
            { "Expected translation arguments <$expected>, but found <${actual ?: "not translatable"}>." },
            { "Expected translation arguments not to be <$expected>." },
        )
    }

private fun Component.translatableOrNull(): TranslatableComponent? = this as? TranslatableComponent
