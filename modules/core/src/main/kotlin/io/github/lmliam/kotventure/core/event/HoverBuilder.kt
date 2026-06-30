package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.HoverEvent
import java.util.UUID
import io.github.lmliam.kotventure.core.text.text as textComponent

internal class HoverBuilder : HoverContentScope {
    private var event: HoverEvent<*>? = null

    override fun text(component: ComponentLike) {
        set(HoverEvent.showText(component))
    }

    override fun text(
        value: String,
        init: TextScope.() -> Unit,
    ) {
        text(textComponent(value, init))
    }

    override fun text(init: ComponentScope.() -> Unit) {
        text(component(init))
    }

    override fun item(
        key: Key,
        count: Int,
        components: ItemDataComponentScope.() -> Unit,
    ) {
        val dataComponents = ItemDataComponentBuilder().apply(components).build()
        set(HoverEvent.showItem(key, requireShowItemCount(count), dataComponents))
    }

    override fun entity(
        type: Key,
        id: UUID,
        name: ComponentLike?,
    ) {
        set(HoverEvent.showEntity(type, id, name?.asComponent()))
    }

    override fun entity(
        type: Key,
        id: UUID,
        init: ComponentScope.() -> Unit,
    ) {
        entity(type, id, component(init))
    }

    internal fun build(): HoverEvent<*> =
        event ?: error("hover { ... } must choose exactly one payload with text(...), item(...), or entity(...).")

    private fun set(event: HoverEvent<*>) {
        check(this.event == null) {
            "hover { ... } must choose only one payload: text(...), item(...), or entity(...)."
        }
        this.event = event
    }
}

private fun requireShowItemCount(count: Int): Int {
    require(count >= 0) {
        "Show item count must be greater than or equal to 0, but was <$count>."
    }
    return count
}
