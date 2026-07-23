package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent

/**
 * Returns a matcher that compares the render context type with [C].
 */
public inline fun <reified C : Any> haveContextType(): Matcher<VirtualComponent> =
    Matcher { value ->
        val expected = C::class.java
        val actual = value.contextType()
        MatcherResult(
            actual == expected,
            { "Expected context type <${expected.name}>, but was <${actual.name}>." },
            { "Expected context type not to be <${expected.name}>." },
        )
    }

/**
 * Returns a matcher that compares the fallback string with [expected].
 */
public fun haveFallbackString(expected: String): Matcher<VirtualComponent> =
    Matcher { value ->
        val actual = value.renderer().fallbackString()
        MatcherResult(
            actual == expected,
            { "Expected fallback string <$expected>, but was <$actual>." },
            { "Expected fallback string not to be <$expected>." },
        )
    }

/**
 * Verifies that this component is a [VirtualComponent].
 *
 * @return this component as a [VirtualComponent].
 * @throws AssertionError when this component has a different type.
 */
public fun Component.shouldBeVirtualComponent(): VirtualComponent = asComponentType("virtual")

/**
 * Verifies that this virtual component has the render context type [C].
 */
public inline fun <reified C : Any> VirtualComponent.shouldHaveContextType(): VirtualComponent =
    apply {
        this should haveContextType<C>()
    }

/**
 * Verifies that this virtual component has the fallback string [expected].
 */
public infix fun VirtualComponent.shouldHaveFallbackString(expected: String): VirtualComponent =
    apply {
        this should haveFallbackString(expected)
    }
