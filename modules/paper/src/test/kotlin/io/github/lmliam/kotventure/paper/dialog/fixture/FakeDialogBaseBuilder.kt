package io.github.lmliam.kotventure.paper.dialog.fixture

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogBase.DialogAfterAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import net.kyori.adventure.text.Component

/** Recording [DialogBase.Builder] whose [build] returns a mockk stub of the captured values. */
internal class FakeDialogBaseBuilder(
    private val title: Component,
) : DialogBase.Builder {
    private var externalTitle: Component? = null
    private var canCloseWithEscape: Boolean = true
    private var pause: Boolean = true
    private var afterAction: DialogAfterAction = DialogAfterAction.CLOSE
    private var body: List<DialogBody> = emptyList()
    private var inputs: List<DialogInput> = emptyList()

    override fun externalTitle(externalTitle: Component?): DialogBase.Builder =
        apply { this.externalTitle = externalTitle }

    override fun canCloseWithEscape(canCloseWithEscape: Boolean): DialogBase.Builder =
        apply { this.canCloseWithEscape = canCloseWithEscape }

    override fun pause(pause: Boolean): DialogBase.Builder = apply { this.pause = pause }

    override fun afterAction(afterAction: DialogAfterAction): DialogBase.Builder =
        apply { this.afterAction = afterAction }

    override fun body(body: MutableList<out DialogBody>): DialogBase.Builder = apply { this.body = body.toList() }

    override fun inputs(inputs: MutableList<out DialogInput>): DialogBase.Builder =
        apply { this.inputs = inputs.toList() }

    override fun build(): DialogBase =
        mockk {
            every { this@mockk.title() } returns title
            every { this@mockk.externalTitle() } returns externalTitle
            every { this@mockk.canCloseWithEscape() } returns canCloseWithEscape
            every { this@mockk.pause() } returns pause
            every { this@mockk.afterAction() } returns afterAction
            every { this@mockk.body() } returns body
            every { this@mockk.inputs() } returns inputs
        }
}
