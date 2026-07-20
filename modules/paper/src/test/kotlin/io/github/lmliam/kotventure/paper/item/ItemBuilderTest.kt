@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.item

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State
import org.bukkit.inventory.ItemStack

class ItemBuilderTest :
    StringSpec(
        {
            "forwards name and lore to the item stack extensions" {
                val stack = mockk<ItemStack>(relaxed = true)
                val expectedName = Component.text("X").decoration(TextDecoration.ITALIC, State.FALSE)
                val expectedLore = listOf(Component.text("line").decoration(TextDecoration.ITALIC, State.FALSE))
                val itemLore = mockk<ItemLore>()
                mockkStatic(ItemLore::class)
                every { ItemLore.lore(expectedLore) } returns itemLore

                try {
                    ItemBuilder(stack).apply {
                        name("X")
                        lore { +"line" }
                    }

                    verify { stack.setData(DataComponentTypes.CUSTOM_NAME, expectedName) }
                    verify { ItemLore.lore(expectedLore) }
                    verify { stack.setData(DataComponentTypes.LORE, itemLore) }
                } finally {
                    unmockkStatic(ItemLore::class)
                }
            }
        },
    )
