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

/**
 * Converts the DSL builder's validated state into the shared typed argument model, in canonical
 * rendering order.
 *
 * [entitySelector] produces the same model from validated source, so both construction paths
 * render through one renderer. This ensures DSL and string-parsed selectors produce identical
 * output.
 */
internal fun EntitySelectorBuilder.selectorArguments(): List<EntitySelectorArgument> =
    buildList {
        addAll(typeFilters.arguments(::typeArgument))
        addAll(nameFilters.arguments(::Name))
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
        addAll(gamemodeFilters.arguments(::GameMode))
        addAll(teamFilters.arguments(::teamArgument))
        limit?.let { add(Limit(it)) }
        sort?.let { add(Sort(it)) }
        addAll(tagFilters.arguments(::tagArgument))
        addAll(nbtFilters.arguments(::nbtArgument))
        addAll(predicateFilters.arguments(::predicateArgument))
    }

//region Filter group conversion

/**
 * Generic helper to convert a filter group's entries into typed arguments.
 *
 * Maps each entry to an argument using the provided conversion function, preserving polarity.
 *
 * @param toArgument conversion function that builds an argument from value and negation flag
 * @return list of converted arguments in declaration order
 */
private fun <T> SelectorFilterGroup<T>.arguments(
    toArgument: (value: T, isNegated: Boolean) -> EntitySelectorArgument,
): List<EntitySelectorArgument> =
    entries.map {
        toArgument(it.value, it.polarity == SelectorFilterPolarity.NEGATIVE)
    }

//endregion
//region Individual argument converters

/**
 * Convert a type or type-tag string into a [Type] argument.
 *
 * Strings starting with `#` denote entity-type tags; others denote direct entity types.
 */
private fun typeArgument(
    value: String,
    isNegated: Boolean,
): Type {
    val target =
        if (value.isEntityTypeTag()) {
        SelectorEntityType.Tag(key(value.removePrefix("#")))
    } else {
        SelectorEntityType.Direct(key(value))
    }
    return Type(target, isNegated)
}

/**
 * Convert a predicate ID string into a [Predicate] argument.
 */
private fun predicateArgument(
    value: String,
    isNegated: Boolean,
): Predicate = Predicate(key(value), isNegated)

/**
 * Convert a team name into a [Team] argument with negation.
 */
private fun teamArgument(
    value: String,
    isNegated: Boolean,
): Team = Team(SelectorStringCondition(value, isNegated))

/**
 * Convert a tag name into a [Tag] argument with negation.
 */
private fun tagArgument(
    value: String,
    isNegated: Boolean,
): Tag = Tag(SelectorStringCondition(value, isNegated))

/**
 * Convert an NBT compound into an [Nbt] argument with negation.
 *
 * The compound is rendered to SNBT source for storage.
 */
private fun nbtArgument(
    value: NbtCompound,
    isNegated: Boolean,
): Nbt = Nbt(SnbtCompoundSource(renderCompound(value)), isNegated)

//endregion
//region Advancement progress conversion

/**
 * Convert an [AdvancementCondition] into the corresponding [SelectorAdvancementProgress].
 *
 * Models either full completion or criteria-specific progress.
 */
private fun AdvancementCondition.progress(): SelectorAdvancementProgress =
    when (this) {
        is AdvancementCondition.Completed -> SelectorAdvancementProgress.Completion(completed)
        is AdvancementCondition.Criteria ->
            SelectorAdvancementProgress.Criteria(
                criteria.map { (name, completed) -> SelectorAdvancementCriterion(name, completed) },
            )
    }

//endregion
//region Helper extensions

/**
 * Check if this string represents an entity-type tag (starts with `#`).
 */
private fun String.isEntityTypeTag(): Boolean = startsWith("#")

//endregion
