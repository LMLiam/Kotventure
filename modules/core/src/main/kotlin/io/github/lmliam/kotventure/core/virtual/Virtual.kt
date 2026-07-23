package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent

/**
 * Creates a [VirtualComponent] that resolves from a render context of type [C].
 *
 * The platform calls [render] at display time with the current context, for example the viewing player. The block can
 * run more than once. The function only creates the component. The `core` module never calls the renderer.
 *
 * A serialiser shows [fallback] when no platform renders the component, for example on a console or in stored data.
 *
 * @sample io.github.lmliam.kotventure.core.virtual.virtualSample
 *
 * @param C the render context type.
 * @param fallback the text to show when no platform renders the component.
 * @param render builds the component content from the [VirtualRenderScope.context].
 */
public inline fun <reified C : Any> virtual(
    fallback: String = "",
    noinline render: VirtualRenderScope<C>.() -> Unit,
): VirtualComponent = Component.virtual(C::class.java, VirtualScopeRenderer(render, fallback))

/**
 * Creates a virtual component and appends it as the next child of this scope.
 *
 * The platform calls [render] at display time with the current context of type [C]. A serialiser shows [fallback] when
 * no platform renders the component.
 *
 * @param C the render context type.
 * @param fallback the text to show when no platform renders the component.
 * @param render builds the child content from the [VirtualRenderScope.context].
 */
public inline fun <reified C : Any> ComponentScope.virtual(
    fallback: String = "",
    noinline render: VirtualRenderScope<C>.() -> Unit,
) {
    append(Component.virtual(C::class.java, VirtualScopeRenderer(render, fallback)))
}
