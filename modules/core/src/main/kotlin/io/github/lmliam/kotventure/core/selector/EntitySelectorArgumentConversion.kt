package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.NbtCompound
import io.github.lmliam.kotventure.core.nbt.renderCompound
import net.kyori.adventure.key.Key

/**
 * Converts the DSL builder's validated state into the shared typed argument model, in canonical
 * rendering order. [entitySelector] produces the same model from validated source, so both
 * construction paths render through one renderer.
 */
internal fun EntitySelectorBuilder.selectorArguments(): List<EntitySelectorArgument> =
    buildList {
        addAll(typeFilters.arguments(::typeArgument))
        addAll(nameFilters.arguments { value, negated -> EntitySelectorArgument.Name(value, negated) })
        (OriginAxis.entries + VolumeAxis.entries).forEach { axis ->
            coordinates[axis]?.let { add(EntitySelectorArgument.Coordinate(axis.coordinate, it)) }
        }
        distance?.let { add(EntitySelectorArgument.Range(SelectorRangeArgument.DISTANCE, it)) }
        pitch?.let { add(EntitySelectorArgument.Range(SelectorRangeArgument.X_ROTATION, it)) }
        yaw?.let { add(EntitySelectorArgument.Range(SelectorRangeArgument.Y_ROTATION, it)) }
        level?.let { add(EntitySelectorArgument.Level(it)) }
        scores?.let { scores -> add(EntitySelectorArgument.Scores(scores.map(::scoreArgument))) }
        advancements?.let { advancements ->
            add(EntitySelectorArgument.Advancements(advancements.map(::advancementArgument)))
        }
        addAll(gamemodeFilters.arguments { value, negated -> EntitySelectorArgument.Gamemode(value, negated) })
        addAll(teamFilters.arguments(::teamArgument))
        limit?.let { add(EntitySelectorArgument.Limit(it)) }
        sort?.let { add(EntitySelectorArgument.Sort(it)) }
        addAll(tagFilters.arguments(::tagArgument))
        addAll(nbtFilters.arguments(::nbtArgument))
        addAll(predicateFilters.arguments(::predicateArgument))
    }

private fun <T> SelectorFilterGroup<T>.arguments(
    toArgument: (value: T, isNegated: Boolean) -> EntitySelectorArgument,
): List<EntitySelectorArgument> =
    entries.map { entry -> toArgument(entry.value, entry.polarity == SelectorFilterPolarity.NEGATIVE) }

private val SelectorAxis.coordinate: SelectorCoordinate
    get() = SelectorCoordinate.entries.first { it.argumentName == argument }

private fun typeArgument(
    value: String,
    isNegated: Boolean,
): EntitySelectorArgument.Type {
    val isTag = value.startsWith("#")
    return EntitySelectorArgument.Type(Key.key(value.removePrefix("#")), isTag, isNegated)
}

private fun predicateArgument(
    value: String,
    isNegated: Boolean,
): EntitySelectorArgument.Predicate = EntitySelectorArgument.Predicate(Key.key(value), isNegated)

private fun teamArgument(
    value: String,
    isNegated: Boolean,
): EntitySelectorArgument.Team = stringConditionArgument(value, isNegated, EntitySelectorArgument::Team)

private fun tagArgument(
    value: String,
    isNegated: Boolean,
): EntitySelectorArgument.Tag = stringConditionArgument(value, isNegated, EntitySelectorArgument::Tag)

private fun nbtArgument(
    value: NbtCompound,
    isNegated: Boolean,
): EntitySelectorArgument.Nbt = EntitySelectorArgument.Nbt(SnbtCompoundSource.trusted(renderCompound(value)), isNegated)

private fun <T : EntitySelectorArgument> stringConditionArgument(
    value: String,
    isNegated: Boolean,
    create: (SelectorStringCondition, Boolean) -> T,
): T =
    if (value.isEmpty()) {
        create(
            SelectorStringCondition.Presence(
                if (isNegated) SelectorPresence.ANY else SelectorPresence.NONE,
            ),
            false,
        )
    } else {
        create(SelectorStringCondition.Named(value), isNegated)
    }

private fun scoreArgument(score: Map.Entry<String, SelectorIntRange>): ParsedSelectorScore =
    ParsedSelectorScore(score.key, score.value)

private fun advancementArgument(advancement: Map.Entry<String, AdvancementCondition>): ParsedSelectorAdvancement =
    ParsedSelectorAdvancement(Key.key(advancement.key), advancement.value.progress())

private fun AdvancementCondition.progress(): ParsedAdvancementProgress =
    when (this) {
        is AdvancementCondition.Completed -> ParsedAdvancementProgress.Completion(completed)
        is AdvancementCondition.Criteria ->
            ParsedAdvancementProgress.Criteria(
                criteria.map { (name, completed) -> ParsedAdvancementCriterion(name, completed) },
            )
    }
