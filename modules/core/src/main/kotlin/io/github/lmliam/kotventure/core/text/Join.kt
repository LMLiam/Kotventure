package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * Joins these components into one, with the separators and prefix/suffix configured by [init].
 *
 * ```kotlin
 * val list = arrayOf(text("a"), text("b"), text("c")).join { separator(text(", ")) }
 * ```
 *
 * @param init configures the separators (including last-element and empty-list cases) and prefix/suffix.
 */
public fun <T : ComponentLike> Array<T>.join(init: JoinScope.() -> Unit = {}): Component = this.asIterable().join(init)

/**
 * Joins these components into one, with the separators and prefix/suffix configured by [init].
 *
 * ```kotlin
 * val list = listOf(text("a"), text("b")).join { separator(text(", ")) }
 * ```
 *
 * @param init configures the separators (including last-element and empty-list cases) and prefix/suffix.
 */
public fun <T : ComponentLike> Iterable<T>.join(init: JoinScope.() -> Unit = {}): Component =
    Component.join(JoinBuilder().apply(init).build(), this)
