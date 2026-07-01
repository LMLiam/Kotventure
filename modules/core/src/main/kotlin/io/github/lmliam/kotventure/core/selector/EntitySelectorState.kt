package io.github.lmliam.kotventure.core.selector

internal class EntitySelectorState {
    var type: String? = null
    var limit: Int? = null
    var distance: SelectorRange? = null
    var sort: SelectorSort? = null
    var name: String? = null
    var level: LevelRange? = null
    var gamemode: GameMode? = null
    val tags: MutableList<String> = mutableListOf()
}
