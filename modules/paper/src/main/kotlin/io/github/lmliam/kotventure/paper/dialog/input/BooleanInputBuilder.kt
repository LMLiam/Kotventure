@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import io.papermc.paper.registry.data.dialog.input.BooleanDialogInput
import io.papermc.paper.registry.data.dialog.input.DialogInput
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class BooleanInputBuilder(
    private val key: String,
) : BooleanInputScope {
    private var label: Component? by once()
    private var default: Boolean? by once()
    private var values: BooleanValuesBuilder? by once { "'values' is already set." }

    override fun label(init: ComponentScope.() -> Unit) = label(component(init))

    override fun <T : ComponentLike> label(component: T) {
        label = component.asComponent()
    }

    override fun default(value: Boolean) {
        default = value
    }

    override fun values(init: BooleanValuesScope.() -> Unit) {
        values = BooleanValuesBuilder().apply(init)
    }

    internal fun build(): BooleanDialogInput {
        val inputLabel = checkNotNull(label) { "a boolean input requires a 'label' slot." }

        return DialogInput
            .bool(key, inputLabel)
            .apply {
                default?.let(::initial)
                values?.onTrue?.let(::onTrue)
                values?.onFalse?.let(::onFalse)
            }.build()
    }
}
