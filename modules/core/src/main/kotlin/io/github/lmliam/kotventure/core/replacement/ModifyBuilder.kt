package io.github.lmliam.kotventure.core.replacement

import io.github.lmliam.kotventure.core.text.TextBuilder
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.Component

internal class ModifyBuilder(
    private val text: TextBuilder,
    override val match: TextMatch,
) : ModifyScope,
    TextScope by text {
    fun build(): Component = text.build()
}
