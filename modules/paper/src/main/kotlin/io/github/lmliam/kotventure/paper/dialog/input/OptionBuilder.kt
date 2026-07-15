package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class OptionBuilder(
    private val id: String,
) : OptionScope {
    private var display: Component? by once()
    private var default: Boolean? by once { "'default' is already set." }

    override fun display(init: ComponentScope.() -> Unit) = display(component(init))

    override fun <T : ComponentLike> display(component: T) {
        display = component.asComponent()
    }

    override fun default() {
        default = true
    }

    internal fun build(): SingleOptionDialogInput.OptionEntry =
        SingleOptionDialogInput.OptionEntry.create(id, display, default ?: false)
}
