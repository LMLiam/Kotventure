package io.github.lmliam.kotventure.paper.dialog.fixture

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.type.DialogListType
import io.papermc.paper.registry.set.RegistrySet

/** Recording [DialogListType.Builder] whose [build] returns a mockk stub of the captured values. */
internal class FakeDialogListBuilder(
    private val dialogs: RegistrySet<Dialog>,
) : DialogListType.Builder {
    private var exitAction: ActionButton? = null
    private var columns: Int = 2
    private var buttonWidth: Int = 150

    override fun exitAction(exitAction: ActionButton?): DialogListType.Builder = apply { this.exitAction = exitAction }

    override fun columns(columns: Int): DialogListType.Builder = apply { this.columns = columns }

    override fun buttonWidth(buttonWidth: Int): DialogListType.Builder = apply { this.buttonWidth = buttonWidth }

    override fun build(): DialogListType =
        mockk {
            every { this@mockk.dialogs() } returns dialogs
            every { this@mockk.exitAction() } returns exitAction
            every { this@mockk.columns() } returns columns
            every { this@mockk.buttonWidth() } returns buttonWidth
        }
}
