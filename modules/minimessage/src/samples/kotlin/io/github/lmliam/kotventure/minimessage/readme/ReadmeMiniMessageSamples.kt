package io.github.lmliam.kotventure.minimessage.readme

import io.github.lmliam.kotventure.minimessage.mini
import io.github.lmliam.kotventure.minimessage.miniToDsl
import io.github.lmliam.kotventure.minimessage.validate

internal fun readmeMiniTourSample() {
    val motd = mini("<gradient:#55FFFF:#FFAA00>Sky Games</gradient> <gray>— Season 5</gray>")

    val streak =
        mini("<gold><wins></gold> win streak, <player>!") {
            unparsed("player", "Alex")
            parsed("wins", "<bold>12</bold>")
        }

    val diagnostics = JoinBroadcast.validate()

    val generated = miniToDsl("<gold>Welcome <bold>back</bold>!")
}
