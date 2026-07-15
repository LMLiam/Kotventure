@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.input

import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput

internal class OptionsBuilder : OptionsScope {
    private val entries = mutableListOf<SingleOptionDialogInput.OptionEntry>()

    override operator fun String.invoke(init: OptionScope.() -> Unit) {
        entries += OptionBuilder(this).apply(init).build()
    }

    override operator fun String.unaryPlus() {
        entries += OptionBuilder(this).build()
    }

    internal fun build(): List<SingleOptionDialogInput.OptionEntry> = entries.toList()
}
