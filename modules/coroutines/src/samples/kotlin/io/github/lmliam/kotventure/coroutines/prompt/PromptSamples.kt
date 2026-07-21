package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.audience.Audience
import kotlin.time.Duration.Companion.minutes

internal enum class Kit {
    ARCHER,
    MAGE,
}

private class Kits {
    fun unlocked(
        @Suppress("UNUSED_PARAMETER") player: Audience,
    ): List<Kit> = listOf(Kit.ARCHER, Kit.MAGE)

    fun give(
        player: Audience,
        kit: Kit,
    ) {
        player.message { text("You have the $kit kit.") }
    }
}

internal suspend fun askSample() {
    val player = emptyAudience()
    val kits = Kits()

    val kit =
        player.ask {
            text("Choose a kit: ")
            option(Kit.ARCHER) { text("[Archer]") { color(green) } }
            text(" ")
            option(Kit.MAGE) { text("[Mage]") { color(aqua) } }
        }

    kits.give(player, kit)
}

private val kitPrompt =
    Prompt<Kit> {
        text("Choose a kit: ")
        Kits().unlocked(viewer).forEach { kit ->
            option(kit) { text("[$kit]") { color(gold) } }
        }
    }

internal suspend fun promptValueSample() {
    val player = emptyAudience()

    val kit = player.ask(kitPrompt)

    Kits().give(player, kit)
}

internal object KitPrompt : Prompt<Kit>({
    text("Choose a kit: ")
    option(Kit.ARCHER) { text("[Archer]") { color(green) } }
    option(Kit.MAGE) { text("[Mage]") { color(aqua) } }
})

internal suspend fun promptObjectSample() {
    val player = emptyAudience()

    val kit = player.ask(KitPrompt)

    Kits().give(player, kit)
}

internal suspend fun askPromptSample() {
    val player = emptyAudience()

    val kit = player.ask(KitPrompt, lifetime = 5.minutes)

    Kits().give(player, kit)
}
