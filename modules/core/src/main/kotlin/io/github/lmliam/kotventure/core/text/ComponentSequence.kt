package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorFlag
import net.kyori.adventure.text.ComponentIteratorType

/**
 * Returns a lazy depth-first (pre-order) [Sequence] over this component and every descendant: this component
 * first, then each child's subtree in declaration order.
 *
 * Being a [Sequence], it composes with the standard library and stays lazy, so short-circuiting terminals stop
 * walking as early as they can:
 *
 * @sample io.github.lmliam.kotventure.core.text.componentSequenceSample
 *
 * Traversal follows Adventure's [Component.iterable] and visits each node, including object components. Translatable
 * arguments and hover contents are not children, so traversal does not visit them. To include them, give the applicable
 * [ComponentIteratorFlag] values directly to [Component.iterable].
 */
public fun Component.asSequence(): Sequence<Component> = iterable(ComponentIteratorType.DEPTH_FIRST).asSequence()
