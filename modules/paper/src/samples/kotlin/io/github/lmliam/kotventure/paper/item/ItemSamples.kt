package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.gray
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

internal fun itemSample(): ItemStack =
    item(Material.DIAMOND_SWORD) {
        name("Excalibur") { color(gold) }
        lore {
            +"A legendary blade"
            "+5 Strength" { color(gray) }
            blank()
        }
    }

internal fun editItemMetaSample(stack: ItemStack) {
    stack.editMeta { meta ->
        meta.lore { +"Bound to soul" }
    }
}
