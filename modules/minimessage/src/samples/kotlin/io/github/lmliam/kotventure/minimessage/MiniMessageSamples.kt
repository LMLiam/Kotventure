package io.github.lmliam.kotventure.minimessage

internal fun miniSample() {
    val greeting = mini("<gold>Welcome <bold>back</bold>!")
}

internal fun miniWithPlaceholdersSample() {
    val playerName = "Alex"
    val line =
        mini("<greeting> <player>!") {
            parsed("greeting", "<gold>Welcome")
            unparsed("player", playerName)
        }
}
