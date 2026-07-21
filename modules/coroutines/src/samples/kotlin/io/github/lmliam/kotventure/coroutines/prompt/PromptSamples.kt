package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.audience.Audience
import kotlin.time.Duration.Companion.minutes

private fun unlockedKits(
    @Suppress("UNUSED_PARAMETER") player: Audience,
): List<Kit> = listOf(Kit.ARCHER, Kit.MAGE)

private fun give(
    player: Audience,
    kit: Kit,
) {
    player.message { text("You have the $kit kit.") }
}

internal suspend fun askSample() {
    val player = emptyAudience()

    val kit =
        player.ask {
            text("Choose a kit: ")
            option(Kit.ARCHER) { text("[Archer]") { color(green) } }
            text(" ")
            option(Kit.MAGE) { text("[Mage]") { color(aqua) } }
        }

    give(player, kit)
}

private val unlockedKitPrompt =
    Prompt {
        text("Choose a kit: ")
        unlockedKits(viewer).forEach { kit ->
            option(kit) { text("[$kit]") { color(gold) } }
        }
    }

internal suspend fun promptValueSample() {
    val player = emptyAudience()

    val kit = player.ask(unlockedKitPrompt)

    give(player, kit)
}

internal suspend fun promptObjectSample() {
    val player = emptyAudience()

    val kit = player.ask(KitPrompt)

    give(player, kit)
}

internal suspend fun askPromptSample() {
    val player = emptyAudience()

    val kit = player.ask(KitPrompt, lifetime = 5.minutes)

    give(player, kit)
}
