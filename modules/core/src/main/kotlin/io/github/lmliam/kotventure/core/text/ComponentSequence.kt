package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorFlag
import net.kyori.adventure.text.ComponentIteratorType

/**
 * Provides a lazy, depth-first sequence that starts with this component and then visits each child subtree in
 * declaration order.
 *
 * Short-circuiting sequence operations stop traversal as soon as they have a result.
 *
 * @sample io.github.lmliam.kotventure.core.text.componentSequenceSample
 *
 * Traversal follows Adventure's [Component.iterable] and includes object components. It does not include translatable
 * arguments or hover contents because they are not component children. To include them, call [Component.iterable]
 * with the applicable [ComponentIteratorFlag] values.
 */
public fun Component.asSequence(): Sequence<Component> = iterable(ComponentIteratorType.DEPTH_FIRST).asSequence()
