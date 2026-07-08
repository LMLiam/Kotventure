package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class ChatBuilder :
    BoundChatBuilder(),
    ChatScope {
    private var content: Component? by once()

    override fun content(init: ComponentScope.() -> Unit) = content(component(init))

    override fun <T : ComponentLike> content(component: T) {
        content = component.asComponent()
    }

    internal fun buildContent(): Component = checkNotNull(content) { "'content' is not set." }
}
