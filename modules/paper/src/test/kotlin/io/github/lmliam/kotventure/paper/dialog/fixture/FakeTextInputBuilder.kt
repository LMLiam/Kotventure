package io.github.lmliam.kotventure.paper.dialog.fixture

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import net.kyori.adventure.text.Component

/** Recording [TextDialogInput.Builder] whose [build] returns a mockk stub of the captured values. */
internal class FakeTextInputBuilder(
    private val key: String,
    private val label: Component,
) : TextDialogInput.Builder {
    private var width: Int = 200
    private var labelVisible: Boolean = true
    private var initial: String = ""
    private var maxLength: Int = 32
    private var multiline: TextDialogInput.MultilineOptions? = null

    override fun width(width: Int): TextDialogInput.Builder = apply { this.width = width }

    override fun labelVisible(labelVisible: Boolean): TextDialogInput.Builder =
        apply { this.labelVisible = labelVisible }

    override fun initial(initial: String): TextDialogInput.Builder = apply { this.initial = initial }

    override fun maxLength(maxLength: Int): TextDialogInput.Builder = apply { this.maxLength = maxLength }

    override fun multiline(multiline: TextDialogInput.MultilineOptions?): TextDialogInput.Builder =
        apply { this.multiline = multiline }

    override fun build(): TextDialogInput =
        mockk {
            every { this@mockk.key() } returns key
            every { this@mockk.label() } returns label
            every { this@mockk.width() } returns width
            every { this@mockk.labelVisible() } returns labelVisible
            every { this@mockk.initial() } returns initial
            every { this@mockk.maxLength() } returns maxLength
            every { this@mockk.multiline() } returns multiline
        }
}
