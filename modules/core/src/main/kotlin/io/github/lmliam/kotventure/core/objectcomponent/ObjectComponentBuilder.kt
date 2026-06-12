package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.text.TextBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.`object`.ObjectContents

internal class ObjectComponentBuilder(
    contents: ObjectContents,
) : ComponentBuilder<ObjectComponent, ObjectComponent.Builder>(Component.`object`().contents(contents)),
    ObjectScope {
    override fun fallback(fallback: ComponentLike?) {
        builder.fallback(fallback)
    }

    override fun fallback(init: ComponentScope.() -> Unit) {
        builder.fallback(TextBuilder().apply(init).build())
    }
}
