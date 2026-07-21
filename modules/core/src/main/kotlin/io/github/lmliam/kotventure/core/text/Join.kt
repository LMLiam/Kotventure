package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * Combines these components in iteration order with the join rules from [init].
 *
 * An empty input produces an empty component unless a prefix or suffix is set. A one-element input does not use a
 * separator. The function does not modify the input components.
 *
 * @sample io.github.lmliam.kotventure.core.text.joinArraySample
 *
 * @param init configures the separators (including last-element and empty-list cases) and prefix/suffix.
 */
public fun <T : ComponentLike> Array<T>.join(init: JoinScope.() -> Unit = {}): Component = this.asIterable().join(init)

/**
 * Combines these components in iteration order with the join rules from [init].
 *
 * An empty input produces an empty component unless a prefix or suffix is set. A one-element input does not use a
 * separator. The function does not modify the input components.
 *
 * @sample io.github.lmliam.kotventure.core.text.joinIterableSample
 *
 * @param init configures the separators (including last-element and empty-list cases) and prefix/suffix.
 */
public fun <T : ComponentLike> Iterable<T>.join(init: JoinScope.() -> Unit = {}): Component =
    Component.join(JoinBuilder().apply(init).build(), this)
