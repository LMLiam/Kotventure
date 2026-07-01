package io.github.lmliam.kotventure.minimessage.conversion

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorHead
import io.github.lmliam.kotventure.core.selector.EntitySelectorParseResult
import io.github.lmliam.kotventure.core.selector.ParsedEntitySelector
import io.github.lmliam.kotventure.core.selector.parseEntitySelector

internal fun selectorDslSource(pattern: String): SelectorDslSource? {
    val parsed =
        when (val result = parseEntitySelector(pattern)) {
            is EntitySelectorParseResult.Success -> result.selector
            is EntitySelectorParseResult.Failure -> return null
        }
    val nbtSources = parsed.selectorNbtSources() ?: return null
    if (!parsed.isLosslesslyRepresentable(pattern, nbtSources)) return null
    val body = parsed.toDslBody(nbtSources) ?: return null
    return SelectorDslSource(
        factoryName = parsed.head.factoryName,
        body = body,
    )
}

internal typealias SelectorNbtSources = Map<EntitySelectorArgument.Nbt, SnbtDslSource>

private fun ParsedEntitySelector.selectorNbtSources(): SelectorNbtSources? {
    val sources = mutableMapOf<EntitySelectorArgument.Nbt, SnbtDslSource>()
    arguments.filterIsInstance<EntitySelectorArgument.Nbt>().forEach { argument ->
        sources[argument] = snbtToDslSource(argument.snbt) ?: return null
    }
    return sources
}

internal data class SelectorDslSource(
    val factoryName: String,
    val body: List<SelectorDslNode>,
) {
    fun appendTo(builder: KotlinSourceBuilder) {
        if (body.isEmpty()) {
            builder.line("$factoryName()")
        } else {
            builder.block(factoryName) { body.forEach { it.appendTo(this) } }
        }
    }
}

internal sealed interface SelectorDslNode {
    fun appendTo(builder: KotlinSourceBuilder)

    data class Line(
        val source: String,
    ) : SelectorDslNode {
        override fun appendTo(builder: KotlinSourceBuilder) {
            builder.line(source)
        }
    }

    data class Block(
        val header: String,
        val body: List<SelectorDslNode>,
    ) : SelectorDslNode {
        override fun appendTo(builder: KotlinSourceBuilder) {
            builder.block(header) { body.forEach { it.appendTo(this) } }
        }
    }

    data class Group(
        val body: List<SelectorDslNode>,
    ) : SelectorDslNode {
        override fun appendTo(builder: KotlinSourceBuilder) {
            body.forEach { it.appendTo(builder) }
        }
    }
}

private val EntitySelectorHead.factoryName: String
    get() =
        when (this) {
            EntitySelectorHead.NEAREST_PLAYER -> "nearestPlayer"
            EntitySelectorHead.ALL_PLAYERS -> "allPlayers"
            EntitySelectorHead.RANDOM_PLAYER -> "randomPlayer"
            EntitySelectorHead.SELF -> "self"
            EntitySelectorHead.ENTITIES -> "entities"
            EntitySelectorHead.NEAREST_ENTITY -> "nearestEntity"
        }
