package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.text.buildTextComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.SelectorComponent

internal class SelectorComponentBuilder(
    pattern: String,
) : ComponentBuilder<SelectorComponent, SelectorComponent.Builder>(Component.selector().pattern(pattern)),
    SelectorScope {
    private var separator: ComponentLike? by once()

    override fun separator(separator: ComponentLike) {
        this.separator = separator
        builder.separator(separator)
    }

    override fun separator(init: TextScope.() -> Unit) {
        separator(buildTextComponent(init))
    }
}
