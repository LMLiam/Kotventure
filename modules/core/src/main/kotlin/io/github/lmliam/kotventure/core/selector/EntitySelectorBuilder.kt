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
        arguments += "type=minecraft:$entityType"
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
        arguments += "name=$name"
    }

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
