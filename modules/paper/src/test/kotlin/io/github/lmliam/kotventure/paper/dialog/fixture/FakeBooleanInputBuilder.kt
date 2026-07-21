package io.github.lmliam.kotventure.paper.dialog.fixture

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.registry.data.dialog.input.BooleanDialogInput
import net.kyori.adventure.text.Component

internal class FakeBooleanInputBuilder(
    private val key: String,
    private val label: Component,
) : BooleanDialogInput.Builder {
    private var initial: Boolean = false
    private var onTrue: String = "true"
    private var onFalse: String = "false"

    override fun initial(initial: Boolean): BooleanDialogInput.Builder = apply { this.initial = initial }

    override fun onTrue(onTrue: String): BooleanDialogInput.Builder = apply { this.onTrue = onTrue }

    override fun onFalse(onFalse: String): BooleanDialogInput.Builder = apply { this.onFalse = onFalse }

    override fun build(): BooleanDialogInput =
        mockk {
            every { this@mockk.key() } returns key
            every { this@mockk.label() } returns label
            every { this@mockk.initial() } returns initial
            every { this@mockk.onTrue() } returns onTrue
            every { this@mockk.onFalse() } returns onFalse
        }
}
