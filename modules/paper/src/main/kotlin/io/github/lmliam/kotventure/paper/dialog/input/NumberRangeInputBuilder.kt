@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.inRange
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.dsl.positive
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.NumberRangeDialogInput
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class NumberRangeInputBuilder(
    private val key: String,
    private val range: ClosedFloatingPointRange<Float>,
) : NumberRangeInputScope {
    private var labelComponent: Component? by once { "'label' is already set." }
    private var width: Int? by once().inRange(1..1024)
    private var labelFormat: String? by once { "'format' is already set." }
    private var default: Float? by once()
    private var step: Float? by once().positive()

    override val label: String get() = $$"%1$s"
    override val value: String get() = $$"%2$s"

    override fun label(init: ComponentScope.() -> Unit) = label(component(init))

    override fun <T : ComponentLike> label(component: T) {
        labelComponent = component.asComponent()
    }

    override fun width(value: Int) {
        width = value
    }

    override fun format(vararg parts: String) {
        require(parts.isNotEmpty()) { "'format' requires at least one part." }
        labelFormat = parts.joinToString("")
    }

    override fun default(value: Float) {
        default = value
    }

    override fun step(value: Float) {
        step = value
    }

    internal fun build(): NumberRangeDialogInput {
        val inputLabel = checkNotNull(labelComponent) { "a number-range input requires a 'label' slot." }

        return DialogInput
            .numberRange(key, inputLabel, range.start, range.endInclusive)
            .apply {
                width?.let(::width)
                labelFormat?.let(::labelFormat)
                default?.let(::initial)
                step?.let(::step)
            }.build()
    }
}
