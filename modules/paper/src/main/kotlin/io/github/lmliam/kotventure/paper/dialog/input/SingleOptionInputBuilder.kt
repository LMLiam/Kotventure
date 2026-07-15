@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.inRange
import io.github.lmliam.kotventure.core.dsl.once
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class SingleOptionInputBuilder(
    private val key: String,
) : SingleOptionInputScope {
    private var label: Component? by once()
    private var labelVisible: Boolean? by once()
    private var width: Int? by once().inRange(1..1024)
    private val entries = mutableListOf<SingleOptionDialogInput.OptionEntry>()

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

    override fun options(init: OptionsScope.() -> Unit) {
        entries += OptionsBuilder().apply(init).build()
    }

    internal fun build(): SingleOptionDialogInput {
        val inputLabel = checkNotNull(label) { "a single-option input requires a 'label' slot." }
        check(entries.isNotEmpty()) { "a single-option input requires at least one option." }

        val duplicate =
            entries
                .groupingBy(SingleOptionDialogInput.OptionEntry::id)
                .eachCount()
                .entries
                .firstOrNull { it.value > 1 }
                ?.key
        check(duplicate == null) { "option '$duplicate' is declared more than once." }
        check(entries.count(SingleOptionDialogInput.OptionEntry::initial) <= 1) {
            "at most one option may be marked default."
        }

        return DialogInput
            .singleOption(key, inputLabel, entries)
            .apply {
                width?.let(::width)
                labelVisible?.let(::labelVisible)
            }.build()
    }
}
