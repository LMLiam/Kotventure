package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent
import kotlin.jvm.javaObjectType

/**
 * Creates a [VirtualComponent] that resolves from a render context of type [C].
 *
 * The [build] block configures two appearances. A [VirtualScope.render] block builds the content that a platform shows
 * at display time. A [VirtualScope.fallback] sets the stand-in that a serialiser or a console shows when no platform
 * renders the component. The function only creates the component. Use [Component.render] to render it in `core`.
 *
 * @sample io.github.lmliam.kotventure.core.virtual.virtualSample
 *
 * @param C the render context type.
 * @param build configures the [VirtualScope.fallback] and [VirtualScope.render] slots.
 * @throws IllegalStateException when [build] sets no render block, or sets a write-once slot more than once.
 */
public inline fun <reified C : Any> virtual(noinline build: VirtualScope<C>.() -> Unit): VirtualComponent =
    buildVirtual(C::class.javaObjectType, build)

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
    append(buildVirtual(C::class.javaObjectType, build))
}

/**
 * Renders virtual components with [context] and [additionalContexts].
 *
 * Contexts are checked in call order, and the first context accepted by each virtual renderer supplies its result.
 * The operation recursively renders every component-bearing slot, including components introduced by a renderer. A
 * virtual component with no matching context stays unchanged and can be rendered by a later call.
 * A matching result replaces the complete virtual component, including its fallback style and children.
 * Exceptions thrown by a virtual renderer propagate unchanged.
 *
 * @param context the first render context.
 * @param additionalContexts the other render contexts, in selection order.
 * @throws IllegalStateException when a virtual renderer returns `null`.
 * @sample io.github.lmliam.kotventure.core.virtual.virtualRenderingSample
 */
public fun Component.render(
    context: Any,
    vararg additionalContexts: Any,
): Component = VirtualTreeRenderer.render(this, VirtualRenderState(context, additionalContexts))

@PublishedApi
internal fun <C : Any> buildVirtual(
    contextType: Class<C>,
    build: VirtualScope<C>.() -> Unit,
): VirtualComponent = VirtualBuilder(contextType).apply(build).build()
