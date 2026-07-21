package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * Returns a matcher that accepts a component with [expected] direct children.
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
 * Returns a matcher that compares direct children with [expected] in order.
 *
 * The matcher uses structural component equality.
 */
public fun <T : ComponentLike> haveChildren(vararg expected: T): Matcher<Component> =
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
 * Returns a matcher that searches the complete component tree for [expected].
 *
 * The matcher includes the root and uses structural component equality.
 */
public fun <T : ComponentLike> containComponent(expected: T): Matcher<Component> =
    Matcher { value ->
        val expectedComponent = expected.asComponent()
        MatcherResult(
            value.containsComponent(expectedComponent),
            { "Expected component tree to contain $expectedComponent" },
            { "Expected component tree not to contain $expectedComponent" },
        )
    }

/**
 * Verifies that this component has [expected] direct children.
 *
 * @return this component, for chained assertions.
 * @throws AssertionError when the child count differs from [expected].
 */
public infix fun Component.shouldHaveChildCount(expected: Int): Component =
    apply {
        this should haveChildCount(expected)
    }

/**
 * Verifies that this component has no direct children.
 *
 * @return this component, for chained assertions.
 * @throws AssertionError when the component has a direct child.
 */
public fun Component.shouldHaveNoChildren(): Component =
    apply {
        this should haveChildCount(0)
    }

/**
 * Verifies that this component has the direct children [expected] in order.
 *
 * @return this component, for chained assertions.
 * @throws AssertionError when the direct children differ from [expected].
 */
public fun <T : ComponentLike> Component.shouldHaveChildren(vararg expected: T): Component =
    apply {
        this should haveChildren(*expected)
    }

/**
 * Verifies that this component tree contains [expected] by structural equality.
 *
 * @return this component, for chained assertions.
 * @throws AssertionError when the tree does not contain [expected].
 */
public infix fun <T : ComponentLike> Component.shouldContainComponent(expected: T): Component =
    apply {
        this should containComponent(expected)
    }

/**
 * Verifies that this component tree does not contain [expected] by structural equality.
 *
 * @return this component, for chained assertions.
 * @throws AssertionError when the tree contains [expected].
 */
public infix fun <T : ComponentLike> Component.shouldNotContainComponent(expected: T): Component =
    apply {
        this shouldNot containComponent(expected)
    }

/**
 * Returns the direct child at [index].
 *
 * @throws IllegalStateException when [index] is outside the direct-child list.
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
