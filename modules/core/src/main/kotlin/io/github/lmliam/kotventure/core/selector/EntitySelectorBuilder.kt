package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal class EntitySelectorBuilder(
    private val head: String,
) : EntitySelectorScope {
    private val arguments = mutableListOf<String>()

    override val nearest: SelectorSort get() = SelectorSort.NEAREST
    override val furthest: SelectorSort get() = SelectorSort.FURTHEST
    override val random: SelectorSort get() = SelectorSort.RANDOM
    override val arbitrary: SelectorSort get() = SelectorSort.ARBITRARY

    override fun type(entityType: Key) {
        arguments += "type=${entityType.asString()}"
    }

    override fun type(entityType: String) {
        val prefixed = if (":" in entityType) entityType else "minecraft:$entityType"
        arguments += "type=$prefixed"
    }

    override fun limit(n: Int) {
        arguments += "limit=$n"
    }

    override fun distance(range: SelectorRange) {
        arguments += "distance=${range.rendered}"
    }

    override fun distance(range: ClosedFloatingPointRange<Double>) {
        arguments += "distance=${between(range.start, range.endInclusive).rendered}"
    }

    override fun sort(sort: SelectorSort) {
        arguments += "sort=${sort.value}"
    }

    override fun tag(tag: String) {
        arguments += "tag=$tag"
    }

    override fun name(name: String) {
        val value =
            if (needsQuoting(name)) {
            "\"${escapeQuotes(name)}\""
        } else {
            name
        }
        arguments += "name=$value"
    }

    private fun needsQuoting(str: String): Boolean = str.any { it in ",[]{}\" " }

    private fun escapeQuotes(str: String): String =
        str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")

    override fun level(range: SelectorRange) {
        arguments += "level=${range.rendered}"
    }

    override fun gamemode(mode: String) {
        arguments += "gamemode=$mode"
    }

    fun build(): EntitySelector {
        val suffix = if (arguments.isEmpty()) "" else arguments.joinToString(",", prefix = "[", postfix = "]")
        return EntitySelector("$head$suffix")
    }
}
