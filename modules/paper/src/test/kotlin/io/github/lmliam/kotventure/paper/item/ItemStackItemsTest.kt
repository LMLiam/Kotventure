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

class ItemStackItemsTest :
    StringSpec(
        {
            "applies a non-italic custom name data component" {
                val stack = mockk<ItemStack>(relaxed = true)
                val expected = Component.text("X").decoration(TextDecoration.ITALIC, State.FALSE)

                stack.name("X")

                verify { stack.setData(DataComponentTypes.CUSTOM_NAME, expected) }
            }

            "applies non-italic lore data component in call order" {
                val stack = mockk<ItemStack>(relaxed = true)
                val expected =
                    listOf(
                        Component.text("a").decoration(TextDecoration.ITALIC, State.FALSE),
                        Component.text("b").decoration(TextDecoration.ITALIC, State.FALSE),
                    )
                val itemLore = mockk<ItemLore>()
                mockkStatic(ItemLore::class)
                every { ItemLore.lore(expected) } returns itemLore

                try {
                    stack.lore {
                        +"a"
                        +"b"
                    }

                    verify { ItemLore.lore(expected) }
                    verify { stack.setData(DataComponentTypes.LORE, itemLore) }
                } finally {
                    unmockkStatic(ItemLore::class)
                }
            }
        },
    )
