package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.selector.EntitySelector
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.SelectorComponent

/**
 * Returns a matcher that compares the selector pattern with [expected].
 */
public fun haveSelectorPattern(expected: String): Matcher<SelectorComponent> =
    Matcher { value ->
        val actual = value.pattern()
        MatcherResult(
            actual == expected,
            { "Expected selector pattern <$expected>, but was <$actual>." },
            { "Expected selector pattern not to be <$expected>." },
        )
    }

/**
 * Returns a matcher that compares the selector pattern with [expected].
 */
public fun haveSelectorPattern(expected: EntitySelector): Matcher<SelectorComponent> =
    haveSelectorPattern(expected.asString())

/**
 * Returns a matcher that compares the selector separator with [expected].
 */
public fun <T : ComponentLike> haveSelectorSeparator(expected: T): Matcher<SelectorComponent> =
    Matcher { value ->
        val actual = value.separator()
        val expectedComponent = expected.asComponent()
        MatcherResult(
            actual == expectedComponent,
            { "Expected selector separator <$expectedComponent>, but was <${actual ?: "null"}>." },
            { "Expected selector separator not to be <$expectedComponent>." },
        )
    }

/**
 * Returns a matcher that accepts a selector component without a separator.
 */
public fun haveNoSelectorSeparator(): Matcher<SelectorComponent> =
    Matcher { value ->
        val actual = value.separator()
        MatcherResult(
            actual == null,
            { "Expected selector separator to be absent, but was <${actual ?: "null"}>." },
            { "Expected selector separator to be present." },
        )
    }

/**
 * Verifies that this component is a [SelectorComponent].
 *
 * @return this component as a [SelectorComponent].
 * @throws AssertionError when this component has a different type.
 */
public fun Component.shouldBeSelectorComponent(): SelectorComponent = asComponentType("selector")

/**
 * Verifies that this selector component has the pattern [expected].
 */
public infix fun SelectorComponent.shouldHaveSelectorPattern(expected: String): SelectorComponent =
    apply {
        this should haveSelectorPattern(expected)
    }

/**
 * Verifies that this selector component has the pattern [expected].
 */
public infix fun SelectorComponent.shouldHaveSelectorPattern(expected: EntitySelector): SelectorComponent =
    apply {
        this should haveSelectorPattern(expected)
    }

/**
 * Verifies that this selector component has the separator [expected].
 */
public infix fun <T : ComponentLike> SelectorComponent.shouldHaveSelectorSeparator(expected: T): SelectorComponent =
    apply {
        this should haveSelectorSeparator(expected)
    }

/**
 * Verifies that this selector component has no separator.
 */
public fun SelectorComponent.shouldNotHaveSelectorSeparator(): SelectorComponent =
    apply {
        this should haveNoSelectorSeparator()
    }
