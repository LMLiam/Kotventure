package io.github.lmliam.kotventure.paper.item

import io.kotest.core.spec.style.StringSpec
import io.mockk.mockk
import io.mockk.verify
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State
import org.bukkit.inventory.meta.ItemMeta

class ItemMetaItemsTest :
    StringSpec(
        {
            "applies a non-italic custom name" {
                val meta = mockk<ItemMeta>(relaxed = true)
                val expected = Component.text("X").decoration(TextDecoration.ITALIC, State.FALSE)

                meta.name("X")

                verify { meta.customName(expected) }
            }

            "applies non-italic lore in call order" {
                val meta = mockk<ItemMeta>(relaxed = true)
                val expected =
                    listOf(
                        Component.text("a").decoration(TextDecoration.ITALIC, State.FALSE),
                        Component.text("b").decoration(TextDecoration.ITALIC, State.FALSE),
                    )

                meta.lore {
                    +"a"
                    +"b"
                }

                verify { meta.lore(expected) }
            }
        },
    )
