package io.github.lmliam.kotventure.core.component

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

/**
 * Builds an Adventure [Component] directly from a Kotventure component DSL block.
 */
public fun component(init: ComponentScope.() -> Unit): Component = RootComponentBuilder().apply(init).build()

private class RootComponentBuilder : ComponentBuilder<TextComponent, TextComponent.Builder>(Component.text())
