@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.inRange
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.dsl.positive
import io.papermc.paper.registry.data.dialog.input.TextDialogInput

internal class TextMultilineBuilder : TextMultilineScope {
    private var maxLines: Int? by once().positive()
    private var height: Int? by once().inRange(1..512)

    override fun maxLines(value: Int) {
        maxLines = value
    }

    override fun height(value: Int) {
        height = value
    }

    internal fun build(): TextDialogInput.MultilineOptions = TextDialogInput.MultilineOptions.create(maxLines, height)
}
