package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorType

/**
 * Returns a lazy depth-first sequence over this component and all of its descendants.
 *
 * The sequence yields this component first, then walks each child in declaration order, descending into a child's
 * own subtree before moving on to its siblings (depth-first, pre-order). It delegates to Adventure's own
 * [Component.iterable] traversal, so it preserves Adventure's component semantics and child ordering and visits
 * every node in the tree — including `ObjectComponent`s — rather than silently dropping any. Translatable
 * arguments and hover contents are not children, so they are not traversed; use [Component.iterable] directly
 * with the relevant `ComponentIteratorFlag`s if you need them.
 *
 * Because the result is a [Sequence], the whole Kotlin standard library is available for traversal and reduction —
 * `fold`, `map`, `filter`, `filterIsInstance`, `any`, `firstOrNull`, `sumOf`, and so on. Operations are lazy, so
 * short-circuiting terminals such as [Sequence.any] or [Sequence.firstOrNull] stop walking as soon as they can.
 */
public fun Component.asSequence(): Sequence<Component> = iterable(ComponentIteratorType.DEPTH_FIRST).asSequence()
