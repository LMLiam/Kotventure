package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * Joins this sequence of components into a single Adventure [Component], configured by [init].
 */
public fun <T : ComponentLike> Array<T>.join(init: JoinScope.() -> Unit = {}): Component = this.asIterable().join(init)

/**
 * Joins this sequence of components into a single Adventure [Component], configured by [init].
 */
public fun <T : ComponentLike> Iterable<T>.join(init: JoinScope.() -> Unit = {}): Component =
    Component.join(JoinBuilder().apply(init).build(), this)
