package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent
import kotlin.jvm.javaObjectType

/**
 * Creates a [VirtualComponent] that resolves from a render context of type [C].
 *
 * The [init] block configures two appearances. [VirtualScope.render] builds the content shown after rendering, while
 * [VirtualScope.fallback] configures the static representation used before rendering. This function only constructs a
 * component. Use [Component.render] to resolve it in `core`.
 *
 * @sample io.github.lmliam.kotventure.core.virtual.virtualSample
 *
 * @param C the render context type.
 * @param init configures the fallback and render slots.
 * @throws IllegalStateException when [init] omits the render block or assigns a write-once slot more than once.
 */
public inline fun <reified C : Any> virtual(noinline init: VirtualScope<C>.() -> Unit): VirtualComponent =
    buildVirtualComponent(C::class.javaObjectType, init)

/**
 * Creates a virtual component and appends it as the next child of this scope.
 *
 * The [init] block configures the same [VirtualScope.render] and [VirtualScope.fallback] slots as [virtual].
 *
 * @param C the render context type.
 * @param init configures the fallback and render slots.
 * @throws IllegalStateException when [init] omits the render block or assigns a write-once slot more than once.
 */
public inline fun <reified C : Any> ComponentScope.virtual(noinline init: VirtualScope<C>.() -> Unit) {
    append(buildVirtualComponent(C::class.javaObjectType, init))
}

/**
 * Renders virtual components with [context] and [additionalContexts].
 *
 * Contexts are checked in call order, and the first context accepted by each virtual renderer supplies its result.
 * The operation recursively renders every component-bearing slot, including components introduced by a renderer. A
 * virtual component with no matching context stays unchanged and can be rendered by a later call. A matching result
 * replaces the complete virtual component, including its fallback style and children. Exceptions thrown by a virtual
 * renderer propagate unchanged.
 *
 * @param context the first render context.
 * @param additionalContexts the other render contexts, in selection order.
 * @throws IllegalStateException when a virtual renderer returns `null`.
 * @sample io.github.lmliam.kotventure.core.virtual.virtualRenderingSample
 */
public fun Component.render(
    context: Any,
    vararg additionalContexts: Any,
): Component = VirtualTreeRenderer.render(this, VirtualRenderState(context, *additionalContexts))

@PublishedApi
internal fun <C : Any> buildVirtualComponent(
    contextType: Class<C>,
    init: VirtualScope<C>.() -> Unit,
): VirtualComponent = VirtualBuilder(contextType).apply(init).build()
