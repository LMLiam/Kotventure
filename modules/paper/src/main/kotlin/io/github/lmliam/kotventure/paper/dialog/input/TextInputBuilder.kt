@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.inRange
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.dsl.positive
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class TextInputBuilder(
    private val key: String,
) : TextInputScope {
    private var label: Component? by once()
    private var labelVisible: Boolean? by once()
    private var width: Int? by once().inRange(1..1024)
    private var default: String? by once()
    private var maxLength: Int? by once().positive()
    private var multilineOptions: TextDialogInput.MultilineOptions? by once()

    override fun label(init: LabelScope.() -> Unit) {
        var visible: Boolean? = null
        label =
            component {
                val labelBuilder = LabelBuilder(this)
                labelBuilder.init()
                visible = labelBuilder.visible
            }
        labelVisible = visible
    }

    override fun <T : ComponentLike> label(component: T) {
        label = component.asComponent()
    }

    override fun width(value: Int) {
        width = value
    }

    override fun default(value: String) {
        default = value
    }

    override fun maxLength(value: Int) {
        maxLength = value
    }

    override fun multiline(init: TextMultilineScope.() -> Unit) {
        multilineOptions =
            TextMultilineBuilder()
                .apply(init)
                .build()
    }

    internal fun build(): TextDialogInput {
        val inputLabel = checkNotNull(label) { "a text input requires a 'label' slot." }

        return DialogInput
            .text(key, inputLabel)
            .apply {
                width?.let(::width)
                labelVisible?.let(::labelVisible)
                default?.let(::initial)
                maxLength?.let(::maxLength)
                multilineOptions?.let(::multiline)
            }.build()
    }
}
