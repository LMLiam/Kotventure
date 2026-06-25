package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorType

/**
 * Returns a lazy depth-first (pre-order) [Sequence] over this component and every descendant: this component
 * first, then each child's subtree in declaration order.
 *
 * Being a [Sequence], it composes with the standard library and stays lazy, so short-circuiting terminals stop
 * walking as early as they can:
 *
 * ```kotlin
 * val mentionsAlex = root.asSequence().any { it is TextComponent && "Alex" in it.content() }
 * ```
 *
 * Traversal follows Adventure's own [Component.iterable], visiting every node in the tree (including object
 * components). Translatable arguments and hover contents are not children and are not visited; pass the relevant
 * `ComponentIteratorFlag`s to [Component.iterable] directly if you need them.
 */
public fun Component.asSequence(): Sequence<Component> = iterable(ComponentIteratorType.DEPTH_FIRST).asSequence()
