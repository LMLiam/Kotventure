package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.`object`.ObjectContents

/**
 * Matches an object component whose contents are [expected]. Combine with `and`/`or` or negate with `shouldNot`.
 */
public fun haveObjectContents(expected: ObjectContents): Matcher<ObjectComponent> =
    Matcher { value ->
        val actual = value.contents()
        MatcherResult(
            actual == expected,
            { "Expected object contents <$expected>, but was <$actual>." },
            { "Expected object contents not to be <$expected>." },
        )
    }

/**
 * Matches an object component whose fallback component is [expected].
 */
public fun haveObjectFallback(expected: Component): Matcher<ObjectComponent> =
    Matcher { value ->
        val actual = value.fallback()
        MatcherResult(
            actual == expected,
            { "Expected object fallback <$expected>, but was <${actual ?: "null"}>." },
            { "Expected object fallback not to be <$expected>." },
        )
    }

/**
 * Matches an object component that has no fallback component.
 */
public fun haveNoObjectFallback(): Matcher<ObjectComponent> =
    Matcher { value ->
        val actual = value.fallback()
        MatcherResult(
            actual == null,
            { "Expected object fallback to be absent, but was <${actual ?: "null"}>." },
            { "Expected object fallback to be present." },
        )
    }

/**
 * Asserts that this component is an [ObjectComponent] and returns it typed.
 */
public fun Component.shouldBeObjectComponent(): ObjectComponent = asComponentType("object")

/**
 * Asserts that this object component has [expected] as its contents.
 */
public infix fun ObjectComponent.shouldHaveObjectContents(expected: ObjectContents): ObjectComponent =
    apply {
        this should haveObjectContents(expected)
    }

/**
 * Asserts that this object component has [expected] as its fallback component.
 */
public infix fun ObjectComponent.shouldHaveObjectFallback(expected: Component): ObjectComponent =
    apply {
        this should haveObjectFallback(expected)
    }

/**
 * Asserts that this object component has no fallback component.
 */
public fun ObjectComponent.shouldNotHaveObjectFallback(): ObjectComponent =
    apply {
        this should haveNoObjectFallback()
    }
