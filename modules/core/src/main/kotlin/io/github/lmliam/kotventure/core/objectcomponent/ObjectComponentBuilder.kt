package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.ComponentScopeBuilder
import io.github.lmliam.kotventure.core.text.TextComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.`object`.ObjectContents

internal class ObjectComponentBuilder(
    contents: ObjectContents,
) : ComponentScopeBuilder<ObjectComponent, ObjectComponent.Builder>(Component.`object`().contents(contents)),
    ObjectScope {
    override fun fallback(fallback: ComponentLike?) {
        builder.fallback(fallback)
    }

    override fun fallback(init: ComponentScope.() -> Unit) {
        builder.fallback(TextComponentBuilder().apply(init).build())
    }
}
