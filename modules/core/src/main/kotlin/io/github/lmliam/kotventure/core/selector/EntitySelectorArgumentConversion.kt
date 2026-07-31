package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.nbt.NbtCompound
import io.github.lmliam.kotventure.core.nbt.renderCompound
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Advancements
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Coordinate
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.GameMode
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Level
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Limit
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Name
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Nbt
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Predicate
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Range
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Scores
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Sort
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Tag
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Team
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Type
import java.util.Map.entry

private const val ENTITY_TYPE_TAG_PREFIX: String = "#"

/**
 * Converts validated builder state to typed arguments in canonical order.
 */
internal fun EntitySelectorBuilder.selectorArguments(): List<EntitySelectorArgument> =
    buildList {
        addArguments(typeFilters, ::typeArgument)
        addArguments(nameFilters, ::Name)

        SelectorCoordinate.entries.forEach { coordinate ->
            coordinates[coordinate]?.let { add(Coordinate(coordinate, it)) }
        }

        distance?.let { add(Range(SelectorRangeArgument.DISTANCE, it)) }
        pitch?.let { add(Range(SelectorRangeArgument.X_ROTATION, it)) }
        yaw?.let { add(Range(SelectorRangeArgument.Y_ROTATION, it)) }
        level?.let { add(Level(it)) }

        scores?.let { scores ->
            add(Scores(scores.map { (objective, range) -> SelectorScoreRequirement(objective, range) }))
        }

        advancements?.let { advancements ->
            add(
                Advancements(
                    advancements.map { (advancement, condition) ->
                        SelectorAdvancementRequirement(advancement, condition.progress())
                    },
                ),
            )
        }

        addArguments(gamemodeFilters, ::GameMode)
        addArguments(teamFilters, ::teamArgument)

        limit?.let { add(Limit(it)) }
        sort?.let { add(Sort(it)) }

        addArguments(tagFilters, ::tagArgument)
        addArguments(nbtFilters, ::nbtArgument)
        addArguments(predicateFilters, ::predicateArgument)
    }

/**
 * Converts [filters] with [toArgument] and keeps entry polarity and order.
 */
private inline fun <T> MutableList<EntitySelectorArgument>.addArguments(
    filters: SelectorFilterGroup<T>,
    toArgument: (value: T, isNegated: Boolean) -> EntitySelectorArgument,
) {
    filters.entries.forEach { entry ->
        add(toArgument(entry.value, entry.isNegated))
    }
}

/**
 * Returns whether this filter entry has negative polarity.
 */
private val SelectorFilterEntry<*>.isNegated: Boolean
    get() = polarity == SelectorFilterPolarity.NEGATIVE

/**
 * Converts a type or type-tag string to a [Type] argument.
 *
 * A string that starts with `#` identifies an entity-type tag. Other strings identify direct entity types.
 */
private fun typeArgument(
    value: String,
    isNegated: Boolean,
): Type =
    Type(
        target =
            if (value.isEntityTypeTag()) {
            SelectorEntityType.Tag(key(value.removePrefix(ENTITY_TYPE_TAG_PREFIX)))
        } else {
            SelectorEntityType.Direct(key(value))
        },
            isNegated = isNegated,
    )

/**
 * Converts a predicate ID to a [Predicate] argument.
 */
private fun predicateArgument(
    value: String,
    isNegated: Boolean,
): Predicate = Predicate(key(value), isNegated)

/**
 * Converts a team name and polarity to a [Team] argument.
 */
private fun teamArgument(
    value: String,
    isNegated: Boolean,
): Team = Team(SelectorStringCondition(value, isNegated))

/**
 * Converts a tag name and polarity to a [Tag] argument.
 */
private fun tagArgument(
    value: String,
    isNegated: Boolean,
): Tag = Tag(SelectorStringCondition(value, isNegated))

/**
 * Converts an NBT compound and polarity to an [Nbt] argument.
 *
 * The argument stores the rendered SNBT source.
 */
private fun nbtArgument(
    value: NbtCompound,
    isNegated: Boolean,
): Nbt = Nbt(SnbtCompoundSource(renderCompound(value)), isNegated)

/**
 * Converts an [AdvancementCondition] to public advancement progress.
 */
private fun AdvancementCondition.progress(): SelectorAdvancementProgress =
    when (this) {
        is AdvancementCondition.Completed -> SelectorAdvancementProgress.Completion(completed)
        is AdvancementCondition.Criteria ->
            SelectorAdvancementProgress.Criteria(
                criteria.map { (name, completed) -> SelectorAdvancementCriterion(name, completed) },
            )
    }

/**
 * Returns whether this string starts with the entity-type-tag prefix.
 */
private fun String.isEntityTypeTag(): Boolean = startsWith(ENTITY_TYPE_TAG_PREFIX)
