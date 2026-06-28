package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal class EntitySelectorBuilder(
    private val head: String,
) : EntitySelectorScope {
    private var type: String? = null
    private var limit: Int? = null
    private var distance: String? = null
    private var sort: String? = null
    private var name: String? = null
    private var level: String? = null
    private var gamemode: String? = null
    private val tags = mutableListOf<String>()

    override val nearest: SelectorSort get() = SelectorSort.NEAREST
    override val furthest: SelectorSort get() = SelectorSort.FURTHEST
    override val random: SelectorSort get() = SelectorSort.RANDOM
    override val arbitrary: SelectorSort get() = SelectorSort.ARBITRARY

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
        distance = range.rendered
    }

    override fun distance(range: ClosedFloatingPointRange<Double>) {
        distance = between(range.start, range.endInclusive).rendered
    }

    override fun sort(sort: SelectorSort) {
        this.sort = sort.value
    }

    override fun tag(tag: String) {
        tags += tag
    }

    override fun name(name: String) {
        this.name =
            if (needsQuoting(name)) {
                "\"${escapeQuotes(name)}\""
            } else {
                name
            }
    }

    private fun needsQuoting(str: String): Boolean = str.any { it in ",[]{}\" " }

    private fun escapeQuotes(str: String): String =
        str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")

    override fun level(range: SelectorRange) {
        level = range.rendered
    }

    override fun gamemode(mode: String) {
        gamemode = mode
    }

    fun build(): EntitySelector {
        val arguments =
            buildList {
                type?.let { add("type=$it") }
                name?.let { add("name=$it") }
                distance?.let { add("distance=$it") }
                level?.let { add("level=$it") }
                gamemode?.let { add("gamemode=$it") }
                limit?.let { add("limit=$it") }
                sort?.let { add("sort=$it") }
                tags.forEach { add("tag=$it") }
            }
        val suffix = if (arguments.isEmpty()) "" else arguments.joinToString(",", prefix = "[", postfix = "]")
        return EntitySelector("$head$suffix")
    }
}
