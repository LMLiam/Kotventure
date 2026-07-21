package io.github.lmliam.kotventure.paper.dialog.fixture

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.type.MultiActionType

internal class FakeMultiActionBuilder(
    private val actions: List<ActionButton>,
) : MultiActionType.Builder {
    private var exitAction: ActionButton? = null
    private var columns: Int = 2

    override fun exitAction(exitAction: ActionButton?): MultiActionType.Builder = apply { this.exitAction = exitAction }

    override fun columns(columns: Int): MultiActionType.Builder = apply { this.columns = columns }

    override fun build(): MultiActionType =
        mockk {
            every { this@mockk.actions() } returns actions
            every { this@mockk.exitAction() } returns exitAction
            every { this@mockk.columns() } returns columns
        }
}
