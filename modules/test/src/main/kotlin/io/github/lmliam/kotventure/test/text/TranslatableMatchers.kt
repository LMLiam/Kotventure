package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument

/**
 * Matches a translatable component whose translation key is [expected]. Combine with `and`/`or` or negate with
 * `shouldNot`.
 */
public fun haveTranslationKey(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.translatableOrNull()?.key()
        MatcherResult(
            actual == expected,
            { "Expected translation key <$expected>, but was <${actual ?: "not translatable"}>." },
            { "Expected translation key not to be <$expected>." },
        )
    }

/**
 * Matches a translatable component whose fallback text is [expected].
 */
public fun haveFallback(expected: String): Matcher<Component> =
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

/**
 * Matches a translatable component that has no fallback text.
 */
public fun haveNoFallback(): Matcher<Component> =
    Matcher { value ->
        val translatable = value.translatableOrNull()
        val actual = translatable?.fallback()
        MatcherResult(
            translatable != null && actual == null,
            { "Expected translatable fallback to be absent, but was <${actual ?: "not translatable"}>." },
            { "Expected translatable fallback to be present." },
        )
    }

/**
 * Matches a translatable component with exactly [expected] translation arguments.
 */
public fun haveArgumentCount(expected: Int): Matcher<Component> =
    Matcher { value ->
        val actual = value.translatableOrNull()?.arguments()?.size
        MatcherResult(
            actual == expected,
            { "Expected <$expected> translation arguments, but found <${actual ?: "not translatable"}>." },
            { "Expected translation argument count not to be <$expected>." },
        )
    }

/**
 * Matches a translatable component whose translation arguments equal [expected] in order.
 */
public fun haveArguments(expected: List<TranslationArgument>): Matcher<Component> =
    Matcher { value ->
        val actual = value.translatableOrNull()?.arguments()
        MatcherResult(
            actual == expected,
            { "Expected translation arguments <$expected>, but found <${actual ?: "not translatable"}>." },
            { "Expected translation arguments not to be <$expected>." },
        )
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

private fun Component.translatableOrNull(): TranslatableComponent? = this as? TranslatableComponent
