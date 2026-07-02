package io.github.lmliam.kotventure.core.selector

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
import net.kyori.adventure.key.Key

/**
 * Converts the DSL builder's validated state into the shared typed argument model, in canonical
 * rendering order. [entitySelector] produces the same model from validated source, so both
 * construction paths render through one renderer.
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
        scores?.let { scores -> add(Scores(scores.map(::scoreArgument))) }
        advancements?.let { advancements ->
            add(Advancements(advancements.map(::advancementArgument)))
        }
        addAll(gamemodeFilters.arguments(::GameMode))
        addAll(teamFilters.arguments(::teamArgument))
        limit?.let { add(Limit(it)) }
        sort?.let { add(Sort(it)) }
        addAll(tagFilters.arguments(::tagArgument))
        addAll(nbtFilters.arguments(::nbtArgument))
        addAll(predicateFilters.arguments(::predicateArgument))
    }

private fun <T> SelectorFilterGroup<T>.arguments(
    toArgument: (value: T, isNegated: Boolean) -> EntitySelectorArgument,
): List<EntitySelectorArgument> =
    entries.map { entry -> toArgument(entry.value, entry.polarity == SelectorFilterPolarity.NEGATIVE) }

private fun typeArgument(
    value: String,
    isNegated: Boolean,
): Type {
    val target =
        if (value.startsWith("#")) {
            SelectorEntityType.Tag(Key.key(value.removePrefix("#")))
        } else {
            SelectorEntityType.Direct(Key.key(value))
        }
    return Type(target, isNegated)
}

private fun predicateArgument(
    value: String,
    isNegated: Boolean,
): Predicate = Predicate(Key.key(value), isNegated)

private fun teamArgument(
    value: String,
    isNegated: Boolean,
): Team = Team(SelectorStringCondition.of(value, isNegated))

private fun tagArgument(
    value: String,
    isNegated: Boolean,
): Tag = Tag(SelectorStringCondition.of(value, isNegated))

private fun nbtArgument(
    value: NbtCompound,
    isNegated: Boolean,
): Nbt = Nbt(SnbtCompoundSource.trusted(renderCompound(value)), isNegated)

private fun scoreArgument(score: Map.Entry<String, SelectorIntRange>): SelectorScoreRequirement =
    SelectorScoreRequirement(score.key, score.value)

private fun advancementArgument(advancement: Map.Entry<Key, AdvancementCondition>): SelectorAdvancementRequirement =
    SelectorAdvancementRequirement(advancement.key, advancement.value.progress())

private fun AdvancementCondition.progress(): SelectorAdvancementProgress =
    when (this) {
        is AdvancementCondition.Completed -> SelectorAdvancementProgress.Completion(completed)
        is AdvancementCondition.Criteria ->
            SelectorAdvancementProgress.Criteria(
                criteria.map { (name, completed) -> SelectorAdvancementCriterion(name, completed) },
            )
    }
