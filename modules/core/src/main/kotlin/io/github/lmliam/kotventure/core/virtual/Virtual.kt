package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.VirtualComponent

/**
 * Creates a [VirtualComponent] that resolves from a render context of type [C].
 *
 * The [build] block configures two appearances. A [VirtualScope.render] block builds the content that a platform shows
 * at display time. A [VirtualScope.fallback] sets the stand-in that a serialiser or a console shows when no platform
 * renders the component. The function only creates the component. The `core` module never calls the render block.
 *
 * @sample io.github.lmliam.kotventure.core.virtual.virtualSample
 *
 * @param C the render context type.
 * @param build configures the [VirtualScope.fallback] and [VirtualScope.render] slots.
 * @throws IllegalStateException when [build] sets no render block, or sets a write-once slot more than once.
 */
public inline fun <reified C : Any> virtual(noinline build: VirtualScope<C>.() -> Unit): VirtualComponent =
    buildVirtual(C::class.java, build)

/**
 * Creates a virtual component and appends it as the next child of this scope.
 *
 * The [build] block configures the same [VirtualScope.render] and [VirtualScope.fallback] slots as the top-level
 * [virtual] function.
 *
 * @param C the render context type.
 * @param build configures the [VirtualScope.fallback] and [VirtualScope.render] slots.
 * @throws IllegalStateException when [build] sets no render block, or sets a write-once slot more than once.
 */
public inline fun <reified C : Any> ComponentScope.virtual(noinline build: VirtualScope<C>.() -> Unit) {
    append(buildVirtual(C::class.java, build))
}

@PublishedApi
internal fun <C : Any> buildVirtual(
    contextType: Class<C>,
    build: VirtualScope<C>.() -> Unit,
): VirtualComponent = VirtualBuilder(contextType).apply(build).build()
