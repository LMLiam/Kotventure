package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal class EntitySelectorBuilder(
    private val head: String,
) : EntitySelectorScope {
    private var type: String? = null
    private var limit: Int? = null
    private var distance: SelectorRange? = null
    private var sort: SelectorSort? = null
    private var name: String? = null
    private var level: SelectorRange? = null
    private var gamemode: GameMode? = null
    private val tags = mutableListOf<String>()

    override val nearest: SelectorSort get() = SelectorSort.NEAREST
    override val furthest: SelectorSort get() = SelectorSort.FURTHEST
    override val random: SelectorSort get() = SelectorSort.RANDOM
    override val arbitrary: SelectorSort get() = SelectorSort.ARBITRARY

    override val survival: GameMode get() = GameMode.SURVIVAL
    override val creative: GameMode get() = GameMode.CREATIVE
    override val adventure: GameMode get() = GameMode.ADVENTURE
    override val spectator: GameMode get() = GameMode.SPECTATOR

    override fun type(entityType: Key) {
        type = entityType.asString()
    }

    override fun type(entityType: String) {
        type = if (":" in entityType) entityType else "minecraft:$entityType"
    }

    override fun limit(n: Int) {
        require(n > 0) { "Selector limit must be positive, got: $n" }
        limit = n
    }

    override fun distance(range: SelectorRange) {
        distance = range
    }

    override fun distance(range: ClosedFloatingPointRange<Double>) {
        require(!range.isEmpty()) { "Range must not be empty, got: $range" }
        distance = closedRange(range.start, range.endInclusive)
    }

    override fun sort(sort: SelectorSort) {
        this.sort = sort
    }

    override fun tag(tag: String) {
        tags += tag
    }

    override fun name(name: String) {
        this.name = name
    }

    override fun level(range: SelectorRange) {
        level = range
    }

    override fun level(range: IntRange) {
        require(!range.isEmpty()) { "Range must not be empty, got: $range" }
        level = SelectorRange("${range.first}..${range.last}")
    }

    override fun gamemode(mode: GameMode) {
        gamemode = mode
    }

    private fun renderName(value: String): String = if (needsQuoting(value)) "\"${escapeQuotes(value)}\"" else value

    private fun needsQuoting(str: String): Boolean = str.any { it in ",[]{}\" " }

    private fun escapeQuotes(str: String): String =
        str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")

    fun build(): EntitySelector {
        val arguments =
            buildList {
                type?.let { add("type=$it") }
                name?.let { add("name=${renderName(it)}") }
                distance?.let { add("distance=${it.rendered}") }
                level?.let { add("level=${it.rendered}") }
                gamemode?.let { add("gamemode=${it.value}") }
                limit?.let { add("limit=$it") }
                sort?.let { add("sort=${it.value}") }
                tags.forEach { add("tag=$it") }
            }
        val suffix = if (arguments.isEmpty()) "" else arguments.joinToString(",", prefix = "[", postfix = "]")
        return EntitySelector("$head$suffix")
    }
}
