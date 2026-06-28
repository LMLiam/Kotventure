package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.selector.EntitySelector
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.SelectorComponent

/**
 * Matches a selector component whose pattern is [expected]. Combine with `and`/`or` or negate with `shouldNot`.
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
 * Matches a selector component whose pattern is [expected]. Combine with `and`/`or` or negate with `shouldNot`.
 */
public fun haveSelectorPattern(expected: EntitySelector): Matcher<SelectorComponent> =
    haveSelectorPattern(expected.asString())

/**
 * Matches a selector component whose separator is [expected].
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
 * Matches a selector component that has no separator.
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
 * Asserts that this component is a [SelectorComponent] and returns it typed.
 */
public fun Component.shouldBeSelectorComponent(): SelectorComponent = asComponentType("selector")

/**
 * Asserts that this selector component has [expected] as its selector pattern.
 */
public infix fun SelectorComponent.shouldHaveSelectorPattern(expected: String): SelectorComponent =
    apply {
        this should haveSelectorPattern(expected)
    }

/**
 * Asserts that this selector component has [expected] as its selector pattern.
 */
public infix fun SelectorComponent.shouldHaveSelectorPattern(expected: EntitySelector): SelectorComponent =
    apply {
        this should haveSelectorPattern(expected)
    }

/**
 * Asserts that this selector component has [expected] as its separator.
 */
public infix fun <T : ComponentLike> SelectorComponent.shouldHaveSelectorSeparator(expected: T): SelectorComponent =
    apply {
        this should haveSelectorSeparator(expected)
    }

/**
 * Asserts that this selector component has no separator.
 */
public fun SelectorComponent.shouldNotHaveSelectorSeparator(): SelectorComponent =
    apply {
        this should haveNoSelectorSeparator()
    }
