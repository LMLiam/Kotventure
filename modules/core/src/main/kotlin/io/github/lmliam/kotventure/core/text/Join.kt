package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component

/**
 * Joins this sequence of components into a single Adventure [Component], configured by [init].
 */
public fun Iterable<Component>.join(init: JoinScope.() -> Unit = {}): Component =
    Component.join(JoinBuilder().apply(init).build(), this)
