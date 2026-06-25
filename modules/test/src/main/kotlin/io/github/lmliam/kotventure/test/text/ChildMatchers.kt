package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * Matches a component with exactly [expected] direct child components. Combine with `and`/`or` or negate with
 * `shouldNot`.
 */
public fun haveChildCount(expected: Int): Matcher<Component> =
    Matcher { value ->
        val actual = value.children().size
        MatcherResult(
            actual == expected,
            { "Expected <$expected> child components, but found <$actual>." },
            { "Expected child component count not to be <$expected>." },
        )
    }

/**
 * Matches a component whose direct children equal [expected] in the same order (structural equality).
 */
public fun haveChildren(vararg expected: ComponentLike): Matcher<Component> =
    Matcher { value ->
        val actual = value.children()
        val expectedChildren = expected.map(ComponentLike::asComponent)
        MatcherResult(
            actual == expectedChildren,
            { "Expected children <$expectedChildren>, but was <$actual>." },
            { "Expected children not to be <$expectedChildren>." },
        )
    }

/**
 * Matches a component whose tree contains [expected] anywhere (by structural equality).
 */
public fun containComponent(expected: ComponentLike): Matcher<Component> =
    Matcher { value ->
        val expectedComponent = expected.asComponent()
        MatcherResult(
            value.containsComponent(expectedComponent),
            { "Expected component tree to contain $expectedComponent" },
            { "Expected component tree not to contain $expectedComponent" },
        )
    }

/**
 * Asserts that this component has exactly [expected] direct child components.
 */
public infix fun Component.shouldHaveChildCount(expected: Int): Component =
    apply {
        this should haveChildCount(expected)
    }

/**
 * Asserts that this component has no direct child components.
 */
public fun Component.shouldHaveNoChildren(): Component =
    apply {
        this should haveChildCount(0)
    }

/**
 * Asserts that this component's direct children are exactly [expected] in order.
 */
public fun Component.shouldHaveChildren(vararg expected: ComponentLike): Component =
    apply {
        this should haveChildren(*expected)
    }

/**
 * Asserts that this component's tree contains [expected] (by structural equality).
 */
public infix fun Component.shouldContainComponent(expected: ComponentLike): Component =
    apply {
        this should containComponent(expected)
    }

/**
 * Asserts that this component's tree does NOT contain [expected] (by structural equality).
 */
public infix fun Component.shouldNotContainComponent(expected: ComponentLike): Component =
    apply {
        this shouldNot containComponent(expected)
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

private fun Component.containsComponent(expected: Component): Boolean =
    this == expected || children().any { it.containsComponent(expected) }
