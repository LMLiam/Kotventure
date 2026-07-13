package io.github.lmliam.kotventure.core.readme

import io.github.lmliam.kotventure.core.audience.book
import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.audience.tabList
import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.gradientText
import io.github.lmliam.kotventure.core.color.gray
import io.github.lmliam.kotventure.core.color.hex
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.nbt.entityNbt
import io.github.lmliam.kotventure.core.nbt.nbtPath
import io.github.lmliam.kotventure.core.score.score
import io.github.lmliam.kotventure.core.selector.allPlayers
import io.github.lmliam.kotventure.core.selector.atLeast
import io.github.lmliam.kotventure.core.selector.parseSelector
import io.github.lmliam.kotventure.core.selector.selector
import io.github.lmliam.kotventure.core.selector.self
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.translatable.translatable

internal fun readmeComponentsTourSample() {
    val hint =
        component {
            text("Press ") { color(gray) }
            keybind("key.sneak") { color(aqua) }
            text(" to sneak past the ")
            translatable("entity.minecraft.warden") { fallback("Warden") }
        }

    val banner = gradientText("Sky Games", hex("#55FFFF"), hex("#FFAA00"))
}

internal fun readmeSelectorsTourSample() {
    val champions =
        allPlayers {
            tag("champion")
            scores { "wins" eq atLeast(3) }
        }

    val scoreboardLine =
        component {
            selector(champions)
            text(" — ")
            score("@s", "wins")
        }

    parseSelector("@e[type=minecraft:zombie,tag=!hidden]")

    val health = entityNbt(self(), nbtPath("Health"))
}

internal fun readmeAudienceTourSample() {
    val player = emptyAudience()

    player.book {
        title { text("Event Guide") }
        author { text("The Architects") }
        page { text("Chapter 1 — Capture the Core") }
    }

    player.tabList {
        header { text("Sky Games") { color(gold) } }
        footer { text("play.example.com") }
    }
}
