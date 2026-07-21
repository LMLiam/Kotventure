package io.github.lmliam.kotventure.paper.dialog.fixture

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.action.DialogAction
import net.kyori.adventure.text.Component

internal class FakeActionButtonBuilder(
    private val label: Component,
) : ActionButton.Builder {
    private var tooltip: Component? = null
    private var width: Int = 150
    private var action: DialogAction? = null

    override fun tooltip(tooltip: Component?): ActionButton.Builder = apply { this.tooltip = tooltip }

    override fun width(width: Int): ActionButton.Builder = apply { this.width = width }

    override fun action(action: DialogAction?): ActionButton.Builder = apply { this.action = action }

    override fun build(): ActionButton =
        mockk {
            every { this@mockk.label() } returns label
            every { this@mockk.tooltip() } returns tooltip
            every { this@mockk.width() } returns width
            every { this@mockk.action() } returns action
        }
}
