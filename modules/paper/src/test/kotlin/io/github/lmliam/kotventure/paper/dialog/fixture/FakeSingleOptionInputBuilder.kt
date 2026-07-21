package io.github.lmliam.kotventure.paper.dialog.fixture

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import net.kyori.adventure.text.Component

internal class FakeSingleOptionInputBuilder(
    private val key: String,
    private val label: Component,
    private val entries: List<SingleOptionDialogInput.OptionEntry>,
) : SingleOptionDialogInput.Builder {
    private var width: Int = 200
    private var labelVisible: Boolean = true

    override fun width(width: Int): SingleOptionDialogInput.Builder = apply { this.width = width }

    override fun labelVisible(labelVisible: Boolean): SingleOptionDialogInput.Builder =
        apply { this.labelVisible = labelVisible }

    override fun build(): SingleOptionDialogInput =
        mockk {
            every { this@mockk.key() } returns key
            every { this@mockk.label() } returns label
            every { this@mockk.entries() } returns entries
            every { this@mockk.width() } returns width
            every { this@mockk.labelVisible() } returns labelVisible
        }
}
