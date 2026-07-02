package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.NbtCompound
import io.github.lmliam.kotventure.core.nbt.renderCompound
import net.kyori.adventure.key.Key
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Advancements as AdvancementsArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Coordinate as CoordinateArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.GameMode as GameModeArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Level as LevelArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Limit as LimitArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Name as NameArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Nbt as NbtArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Predicate as PredicateArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Range as RangeArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Scores as ScoresArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Sort as SortArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Tag as TagArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Team as TeamArgument
import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument.Type as TypeArgument

/**
 * Converts the DSL builder's validated state into the shared typed argument model, in canonical
 * rendering order. [entitySelector] produces the same model from validated source, so both
 * construction paths render through one renderer.
 */
internal fun EntitySelectorBuilder.selectorArguments(): List<EntitySelectorArgument> =
    buildList {
        addAll(typeFilters.arguments(::typeArgument))
        addAll(nameFilters.arguments(::NameArgument))
        SelectorCoordinate.entries.forEach { coordinate ->
            coordinates[coordinate]?.let { add(CoordinateArgument(coordinate, it)) }
        }
        distance?.let { add(RangeArgument(SelectorRangeArgument.DISTANCE, it)) }
        pitch?.let { add(RangeArgument(SelectorRangeArgument.X_ROTATION, it)) }
        yaw?.let { add(RangeArgument(SelectorRangeArgument.Y_ROTATION, it)) }
        level?.let { add(LevelArgument(it)) }
        scores?.let { scores -> add(ScoresArgument(scores.map(::scoreArgument))) }
        advancements?.let { advancements ->
            add(AdvancementsArgument(advancements.map(::advancementArgument)))
        }
        addAll(gamemodeFilters.arguments(::GameModeArgument))
        addAll(teamFilters.arguments(::teamArgument))
        limit?.let { add(LimitArgument(it)) }
        sort?.let { add(SortArgument(it)) }
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
): TypeArgument {
    val target =
        if (value.startsWith("#")) {
            SelectorEntityType.Tag(Key.key(value.removePrefix("#")))
        } else {
            SelectorEntityType.Direct(Key.key(value))
        }
    return TypeArgument(target, isNegated)
}

private fun predicateArgument(
    value: String,
    isNegated: Boolean,
): PredicateArgument = PredicateArgument(Key.key(value), isNegated)

private fun teamArgument(
    value: String,
    isNegated: Boolean,
): TeamArgument = TeamArgument(SelectorStringCondition.of(value, isNegated))

private fun tagArgument(
    value: String,
    isNegated: Boolean,
): TagArgument = TagArgument(SelectorStringCondition.of(value, isNegated))

private fun nbtArgument(
    value: NbtCompound,
    isNegated: Boolean,
): NbtArgument = NbtArgument(SnbtCompoundSource.trusted(renderCompound(value)), isNegated)

private fun scoreArgument(score: Map.Entry<String, SelectorIntRange>): SelectorScoreRequirement =
    SelectorScoreRequirement(score.key, score.value)

private fun advancementArgument(advancement: Map.Entry<String, AdvancementCondition>): SelectorAdvancementRequirement =
    SelectorAdvancementRequirement(Key.key(advancement.key), advancement.value.progress())

private fun AdvancementCondition.progress(): SelectorAdvancementProgress =
    when (this) {
        is AdvancementCondition.Completed -> SelectorAdvancementProgress.Completion(completed)
        is AdvancementCondition.Criteria ->
            SelectorAdvancementProgress.Criteria(
                criteria.map { (name, completed) -> SelectorAdvancementCriterion(name, completed) },
            )
    }
