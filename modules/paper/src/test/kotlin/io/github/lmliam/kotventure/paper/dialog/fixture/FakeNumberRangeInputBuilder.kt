package io.github.lmliam.kotventure.paper.dialog.fixture

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.registry.data.dialog.input.NumberRangeDialogInput
import net.kyori.adventure.text.Component

internal class FakeNumberRangeInputBuilder(
    private val key: String,
    private val label: Component,
    private val start: Float,
    private val end: Float,
) : NumberRangeDialogInput.Builder {
    private var width: Int = 200
    private var labelFormat: String = "options.generic_value"
    private var initial: Float? = null
    private var step: Float? = null

    override fun width(width: Int): NumberRangeDialogInput.Builder = apply { this.width = width }

    override fun labelFormat(labelFormat: String): NumberRangeDialogInput.Builder =
        apply { this.labelFormat = labelFormat }

    override fun initial(initial: Float?): NumberRangeDialogInput.Builder = apply { this.initial = initial }

    override fun step(step: Float?): NumberRangeDialogInput.Builder = apply { this.step = step }

    override fun build(): NumberRangeDialogInput =
        mockk {
            every { this@mockk.key() } returns key
            every { this@mockk.label() } returns label
            every { this@mockk.start() } returns start
            every { this@mockk.end() } returns end
            every { this@mockk.width() } returns width
            every { this@mockk.labelFormat() } returns labelFormat
            every { this@mockk.initial() } returns initial
            every { this@mockk.step() } returns step
        }
}
