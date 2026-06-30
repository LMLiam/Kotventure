package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.selector.self

internal fun nbtPathSample() {
    nbtPath("Items")[0]["tag"]["display"]["Name"]
    nbtPath("Inventory")[all]["id"]
    nbtPath("Items")[matching { "id" eq "minecraft:diamond" }]["Count"]
}

internal fun nbtPathVerbatimSample() {
    nbtPath("Items[{id:\"minecraft:diamond\"}].Count")
}

internal fun nbtPathKeySample() {
    nbtPath("tag")["display"]["Name"]
}

internal fun nbtPathIndexSample() {
    nbtPath("Items")[0]["id"]
}

internal fun nbtPathSelectionSample() {
    nbtPath("Inventory")[all]["id"]
    nbtPath("Items")[matching { "id" eq "minecraft:diamond" }]["Count"]
}

internal fun nbtPathFactorySample() {
    // Structured
    nbtPath("Items")[0]["id"]

    // Pre-formed string, still chainable
    nbtPath("Items[0]")["tag"]
}

internal fun allSample() {
    nbtPath("Passengers")[all]["CustomName"]
}

internal fun matchingSample() {
    nbtPath("Items")[matching { "id" eq "minecraft:diamond" }]["Count"]
}

internal fun nbtCompoundScopeSample() {
    matching {
        "id" eq "minecraft:diamond"
        "Count" eq 1.toByte()
        "tag" eq { "Unbreakable" eq 1.toByte() }
    }
}

internal fun nbtSample() {
    nbt {
        "kotventure" eq 1.toByte()
        "display" eq {
            "Name" eq "Sword"
        }
    }
}

internal fun nbtListSample() {
    nbt {
        "pages" eq list("Once", "upon", "a", "time")
        "Lore" eq
                list(
                    { "text" eq "Line 1" },
                    { "text" eq "Line 2" },
                )
        "rows" eq list(intArrayOf(1, 2), intArrayOf(3, 4))
        "grid" eq list(list(1, 2), list(3, 4))
    }
}

internal fun blockNbtSample() {
    val sign = blockNbt(blockPos(0, 64, 0), nbtPath("front_text")["messages"][0])
}

internal fun entityNbtSample() {
    val health = entityNbt(self(), nbtPath("Health"))
}

internal fun storageNbtSample() {
    val score = storageNbt(key("myplugin", "scores"), nbtPath("top.player"))
}
