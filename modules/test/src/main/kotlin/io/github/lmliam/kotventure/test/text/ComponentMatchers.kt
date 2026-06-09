package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
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
 * Asserts that this component is a keybind component and has [expected] as its keybind.
 */
public infix fun Component.shouldHaveKeybind(expected: String): Component =
    apply {
        this should haveKeybind(expected)
    }

/**
 * Asserts that this component is a score component and has [expected] as its score name.
 */
public infix fun Component.shouldHaveScoreName(expected: String): Component =
    apply {
        this should haveScoreName(expected)
    }

/**
 * Asserts that this component is a score component and has [expected] as its score objective.
 */
public infix fun Component.shouldHaveScoreObjective(expected: String): Component =
    apply {
        this should haveScoreObjective(expected)
    }

/**
 * Asserts that this component is a selector component and has [expected] as its selector pattern.
 */
public infix fun Component.shouldHaveSelectorPattern(expected: String): Component =
    apply {
        this should haveSelectorPattern(expected)
    }

/**
 * Asserts that this component is a selector component and has [expected] as its separator.
 */
public infix fun Component.shouldHaveSeparator(expected: Component): Component =
    apply {
        this should haveSeparator(expected)
    }

/**
 * Asserts that this component is a selector component and has no separator.
 */
public fun Component.shouldNotHaveSeparator(): Component =
    apply {
        this should haveNoSeparator()
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

private fun haveKeybind(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.keybindOrNull()?.keybind()
        MatcherResult(
            actual == expected,
            { "Expected keybind <$expected>, but was <${actual ?: "not keybind"}>." },
            { "Expected keybind not to be <$expected>." },
        )
    }

private fun haveScoreName(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.scoreOrNull()?.name()
        MatcherResult(
            actual == expected,
            { "Expected score name <$expected>, but was <${actual ?: "not score"}>." },
            { "Expected score name not to be <$expected>." },
        )
    }

private fun haveScoreObjective(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.scoreOrNull()?.objective()
        MatcherResult(
            actual == expected,
            { "Expected score objective <$expected>, but was <${actual ?: "not score"}>." },
            { "Expected score objective not to be <$expected>." },
        )
    }

private fun haveSelectorPattern(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.selectorOrNull()?.pattern()
        MatcherResult(
            actual == expected,
            { "Expected selector pattern <$expected>, but was <${actual ?: "not selector"}>." },
            { "Expected selector pattern not to be <$expected>." },
        )
    }

private fun haveSeparator(expected: Component): Matcher<Component> =
    Matcher { value ->
        val selector = value.selectorOrNull()
        val actual = selector?.separator()
        val actualDescription = if (selector == null) "not selector" else actual ?: "null"
        MatcherResult(
            selector != null && actual == expected,
            { "Expected selector separator <$expected>, but was <$actualDescription>." },
            { "Expected selector separator not to be <$expected>." },
        )
    }

private fun haveNoSeparator(): Matcher<Component> =
    Matcher { value ->
        val selector = value.selectorOrNull()
        val actual = selector?.separator()
        val actualDescription = if (selector == null) "not selector" else actual ?: "null"
        MatcherResult(
            selector != null && actual == null,
            { "Expected selector separator to be absent, but was <$actualDescription>." },
            { "Expected selector separator to be present." },
        )
    }

private fun Component.keybindOrNull(): KeybindComponent? = this as? KeybindComponent

private fun Component.scoreOrNull(): ScoreComponent? = this as? ScoreComponent

private fun Component.selectorOrNull(): SelectorComponent? = this as? SelectorComponent

private fun Component.translatableOrNull(): TranslatableComponent? = this as? TranslatableComponent
