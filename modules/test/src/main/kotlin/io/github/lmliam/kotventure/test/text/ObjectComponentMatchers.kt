package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.`object`.ObjectContents

/**
 * Returns a matcher that compares object contents with [expected].
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
 * Returns a matcher that compares the fallback component with [expected].
 */
public fun <T : ComponentLike> haveObjectFallback(expected: T): Matcher<ObjectComponent> =
    Matcher { value ->
        val actual = value.fallback()
        val expectedComponent = expected.asComponent()
        MatcherResult(
            actual == expectedComponent,
            { "Expected object fallback <$expectedComponent>, but was <${actual ?: "null"}>." },
            { "Expected object fallback not to be <$expectedComponent>." },
        )
    }

/**
 * Returns a matcher that accepts an object component without a fallback.
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
 * Verifies that this component is an [ObjectComponent].
 *
 * @return this component as an [ObjectComponent].
 * @throws AssertionError when this component has a different type.
 */
public fun Component.shouldBeObjectComponent(): ObjectComponent = asComponentType("object")

/**
 * Verifies that this object component has [expected] as its contents.
 */
public infix fun ObjectComponent.shouldHaveObjectContents(expected: ObjectContents): ObjectComponent =
    apply {
        this should haveObjectContents(expected)
    }

/**
 * Verifies that this object component has [expected] as its fallback.
 */
public infix fun <T : ComponentLike> ObjectComponent.shouldHaveObjectFallback(expected: T): ObjectComponent =
    apply {
        this should haveObjectFallback(expected)
    }

/**
 * Verifies that this object component has no fallback.
 */
public fun ObjectComponent.shouldNotHaveObjectFallback(): ObjectComponent =
    apply {
        this should haveNoObjectFallback()
    }
