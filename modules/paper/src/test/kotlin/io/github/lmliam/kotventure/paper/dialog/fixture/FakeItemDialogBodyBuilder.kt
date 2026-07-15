package io.github.lmliam.kotventure.paper.dialog.fixture

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody
import org.bukkit.inventory.ItemStack

/** Recording [ItemDialogBody.Builder] whose [build] returns a mockk stub of the captured values. */
internal class FakeItemDialogBodyBuilder(
    private val item: ItemStack,
) : ItemDialogBody.Builder {
    private var description: PlainMessageDialogBody? = null
    private var showDecorations: Boolean = true
    private var showTooltip: Boolean = true
    private var width: Int = 16
    private var height: Int = 16

    override fun description(description: PlainMessageDialogBody?): ItemDialogBody.Builder =
        apply { this.description = description }

    override fun showDecorations(showDecorations: Boolean): ItemDialogBody.Builder =
        apply { this.showDecorations = showDecorations }

    override fun showTooltip(showTooltip: Boolean): ItemDialogBody.Builder = apply { this.showTooltip = showTooltip }

    override fun width(width: Int): ItemDialogBody.Builder = apply { this.width = width }

    override fun height(height: Int): ItemDialogBody.Builder = apply { this.height = height }

    override fun build(): ItemDialogBody =
        mockk {
            every { this@mockk.item() } returns item
            every { this@mockk.description() } returns description
            every { this@mockk.showDecorations() } returns showDecorations
            every { this@mockk.showTooltip() } returns showTooltip
            every { this@mockk.width() } returns width
            every { this@mockk.height() } returns height
        }
}
