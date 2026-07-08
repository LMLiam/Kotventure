package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal open class BoundChatBuilder : BoundChatScope {
    private var type: ChatType? by once()
    private var name: Component? by once()
    private var target: Component? by once()

    override fun type(type: ChatType) {
        this.type = type
    }

    override fun name(init: ComponentScope.() -> Unit) = name(component(init))

    override fun <T : ComponentLike> name(component: T) {
        name = component.asComponent()
    }

    override fun target(init: ComponentScope.() -> Unit) = target(component(init))

    override fun <T : ComponentLike> target(component: T) {
        target = component.asComponent()
    }

    internal fun buildBound(): ChatType.Bound {
        val name = checkNotNull(name) { "'name' is not set." }
        return (type ?: ChatType.CHAT).bind(name, target)
    }
}

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
