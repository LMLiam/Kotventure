package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.text.TextBuilder
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.SelectorComponent

internal class SelectorComponentBuilder(
    pattern: String,
) : ComponentBuilder<SelectorComponent, SelectorComponent.Builder>(Component.selector().pattern(pattern)),
    SelectorScope {
    override fun separator(separator: ComponentLike) {
        builder.separator(separator)
    }

    override fun separator(init: TextScope.() -> Unit) {
        builder.separator(TextBuilder().apply(init).build())
    }
}
