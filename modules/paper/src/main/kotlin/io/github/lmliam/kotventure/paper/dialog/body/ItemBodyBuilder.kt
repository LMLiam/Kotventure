@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.body

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.inRange
import io.github.lmliam.kotventure.core.dsl.once
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.inventory.ItemStack

internal class ItemBodyBuilder(
    private val stack: ItemStack,
) : ItemBodyScope {
    private var description: Component? by once()
    private var decorations: Boolean? by once { "'decorations' is already set." }
    private var tooltip: Boolean? by once { "'tooltip' is already set." }
    private var width: Int? by once().inRange(1..256)
    private var height: Int? by once().inRange(1..256)

    override fun description(init: ComponentScope.() -> Unit) = description(component(init))

    override fun <T : ComponentLike> description(component: T) {
        description = component.asComponent()
    }

    override fun decorations(value: Boolean) {
        decorations = value
    }

    override fun tooltip(value: Boolean) {
        tooltip = value
    }

    override fun width(value: Int) {
        width = value
    }

    override fun height(value: Int) {
        height = value
    }

    internal fun build(): ItemDialogBody =
        DialogBody
            .item(stack)
            .apply {
                description?.let { description(DialogBody.plainMessage(it)) }
                decorations?.let(::showDecorations)
                tooltip?.let(::showTooltip)
                width?.let(::width)
                height?.let(::height)
            }.build()
}
