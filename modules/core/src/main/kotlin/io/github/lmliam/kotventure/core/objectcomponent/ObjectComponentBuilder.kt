package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.singleAssign
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.`object`.ObjectContents

internal class ObjectComponentBuilder(
    contents: ObjectContents,
) : ComponentBuilder<ObjectComponent, ObjectComponent.Builder>(Component.`object`().contents(contents)),
    ObjectScope {
    private var fallback: ComponentLike? by singleAssign()

    override fun fallback(fallback: ComponentLike?) {
        this.fallback = fallback
        builder.fallback(fallback)
    }

    override fun fallback(init: ComponentScope.() -> Unit) {
        fallback(component(init))
    }
}
