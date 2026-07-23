package io.github.lmliam.kotventure.core.virtual

import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.StorageNBTComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.VirtualComponent
import net.kyori.adventure.text.VirtualComponentRenderer
import net.kyori.adventure.text.renderer.AbstractComponentRenderer

internal object VirtualTreeRenderer : AbstractComponentRenderer<VirtualRenderState>() {
    override fun render(
        component: Component,
        context: VirtualRenderState,
    ): Component {
        if (component !is VirtualComponent) return super.render(component, context)
        return context.renderOnce(component) { super.render(component, context) }
    }

    override fun renderText(
        component: TextComponent,
        context: VirtualRenderState,
    ): Component = renderNested(component, context)

    override fun renderTranslatable(
        component: TranslatableComponent,
        context: VirtualRenderState,
    ): Component {
        val renderedArguments = component.arguments().map { renderArgument(it, context) }

        return renderNested(component.arguments(renderedArguments), context)
    }

    override fun renderKeybind(
        component: KeybindComponent,
        context: VirtualRenderState,
    ): Component = renderNested(component, context)

    override fun renderScore(
        component: ScoreComponent,
        context: VirtualRenderState,
    ): Component = renderNested(component, context)

    override fun renderSelector(
        component: SelectorComponent,
        context: VirtualRenderState,
    ): Component {
        val separator = component.separator()
        val rendered = if (separator == null) component else component.separator(render(separator, context))

        return renderNested(rendered, context)
    }

    override fun renderBlockNbt(
        component: BlockNBTComponent,
        context: VirtualRenderState,
    ): Component = renderNbt(component, context)

    override fun renderEntityNbt(
        component: EntityNBTComponent,
        context: VirtualRenderState,
    ): Component = renderNbt(component, context)

    override fun renderStorageNbt(
        component: StorageNBTComponent,
        context: VirtualRenderState,
    ): Component = renderNbt(component, context)

    override fun renderObject(
        component: ObjectComponent,
        context: VirtualRenderState,
    ): Component {
        val fallback = component.fallback()
        val rendered = if (fallback == null) component else component.fallback(render(fallback, context))

        return renderNested(rendered, context)
    }

    override fun renderVirtual(
        component: VirtualComponent,
        context: VirtualRenderState,
    ): Component {
        val matchingContext = context.firstMatching(component.contextType()) ?: return component
        val rendered = component.renderWith(matchingContext)

        return if (rendered is VirtualComponent) render(rendered, context) else rendered
    }

    private fun renderArgument(
        argument: TranslationArgument,
        context: VirtualRenderState,
    ): TranslationArgument {
        val component = argument.value() as? Component ?: return argument
        return TranslationArgument.component(render(component, context))
    }

    private fun <C : NBTComponent<C>> renderNbt(
        component: C,
        context: VirtualRenderState,
    ): Component {
        val separator = component.separator()
        val rendered = if (separator == null) component else component.separator(render(separator, context))

        return renderNested(rendered, context)
    }

    private fun renderNested(
        component: Component,
        context: VirtualRenderState,
    ): Component {
        val hoverEvent = component.hoverEvent()
        val withRenderedHover =
            if (hoverEvent == null) component else component.hoverEvent(hoverEvent.withRenderedValue(this, context))
        val children = withRenderedHover.children()

        return if (children.isEmpty()) {
            withRenderedHover
        } else {
            withRenderedHover.children(children.map { render(it, context) })
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun VirtualComponent.renderWith(context: Any): Component {
        val renderer = renderer() as VirtualComponentRenderer<Any>
        val result: ComponentLike? = renderer.apply(context)
        return checkNotNull(result) { "The virtual component renderer returned null." }.asComponent()
    }
}
