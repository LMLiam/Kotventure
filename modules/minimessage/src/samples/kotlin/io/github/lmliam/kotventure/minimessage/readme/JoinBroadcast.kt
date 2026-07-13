package io.github.lmliam.kotventure.minimessage.readme

import io.github.lmliam.kotventure.minimessage.template.MiniTemplate
import net.kyori.adventure.text.Component

internal object JoinBroadcast :
    MiniTemplate("<gray>[<green>+</green>]</gray> <player> <gray>joined — <online> online</gray>") {
    val player by placeholder<Component>()
    val online by placeholder<Int>()
}
