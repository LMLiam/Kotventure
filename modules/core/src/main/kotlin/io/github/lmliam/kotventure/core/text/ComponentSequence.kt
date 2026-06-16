package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorType

/**
 * Returns a lazy depth-first sequence over this component and all of its descendants.
 *
 * The sequence yields this component first, then walks each child in declaration order, descending into a child's
 * own subtree before moving on to its siblings (depth-first, pre-order). It delegates to Adventure's own
 * [Component.iterable] traversal, so it preserves Adventure's component semantics and child ordering and visits
 * every node in the tree — including [net.kyori.adventure.text.ObjectComponent]s — rather than silently dropping
 * any. Translatable arguments and hover contents are not children, so they are not traversed; use
 * [Component.iterable] directly with the relevant `ComponentIteratorFlag`s if you need them.
 *
 * Because the result is a [Sequence], the whole Kotlin standard library is available for traversal and reduction —
 * `fold`, `map`, `filter`, `filterIsInstance`, `any`, `firstOrNull`, `sumOf`, and so on. Operations are lazy, so
 * short-circuiting terminals such as [Sequence.any] or [Sequence.firstOrNull] stop walking as soon as they can.
 */
public fun Component.asSequence(): Sequence<Component> = iterable(ComponentIteratorType.DEPTH_FIRST).asSequence()

/**
 * Counts this component and every descendant in its tree.
 *
 * A convenience for `asSequence().count()`: it counts all nodes, including this root and every nested child, of
 * any component type. Use [asSequence] with a predicate (for example `asSequence().count { … }`) to count a subset.
 */
public fun Component.count(): Int = asSequence().count()

/**
 * Applies [action] to this component and every descendant, depth-first and pre-order.
 *
 * A convenience for `asSequence().forEach(action)`, intended for side-effecting walks such as logging or
 * validation. Use [asSequence] directly when you need to transform, filter, or reduce the tree instead.
 */
public fun Component.forEach(action: (Component) -> Unit) {
    asSequence().forEach(action)
}
