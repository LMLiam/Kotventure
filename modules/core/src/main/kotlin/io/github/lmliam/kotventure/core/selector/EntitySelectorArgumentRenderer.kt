package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.renderValue
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

/**
 * Renders this argument as canonical `name=value` source text.
 *
 * The rendered value includes a leading `!` when the argument is negated.
 */
internal fun EntitySelectorArgument.render(): String = "$argumentName=${renderValue()}"

/**
 * Renders the value after the equals sign.
 */
private fun EntitySelectorArgument.renderValue(): String =
    when (this) {
        is Coordinate -> formatSelectorNumber(value)
        is Range -> range.toString()
        is Limit -> value.toString()
        is Sort -> value.value
        is Level -> range.toString()
        is GameMode -> renderNegated(value.value)
        is Name -> renderNegated(value.renderSelectorName())
        is Type -> renderNegated(target.render())
        is Tag -> renderNegated(condition.render())
        is Team -> renderNegated(condition.render())
        is Nbt -> renderNegated(snbt.value)
        is Scores -> scores.render()
        is Predicate -> renderNegated(key.asString())
        is Advancements -> advancements.render()
    }

/**
 * Renders [value] with this argument's leading negation marker when applicable.
 */
private fun EntitySelectorArgument.Negatable.renderNegated(value: String): String =
    if (isNegated) "$SELECTOR_NEGATION_PREFIX$value" else value

/**
 * Renders an entity type or entity-type tag.
 *
 * A tag has a leading `#`.
 */
private fun SelectorEntityType.render(): String =
    when (this) {
        is SelectorEntityType.Direct -> key.asString()
        is SelectorEntityType.Tag -> "#${key.asString()}"
    }

/**
 * Renders a name and quotes it when unquoted selector syntax cannot contain it.
 */
private fun String.renderSelectorName(): String =
    if (isNotEmpty() && all(Char::isAllowedInUnquotedSelectorToken)) this else quoteSelectorString()

/**
 * Quotes a selector string and escapes double quotes and backslashes.
 */
private fun String.quoteSelectorString(): String =
    buildString(length + 2) {
        append('"')
        this@quoteSelectorString.forEach { character ->
            if (character.needsEscape()) append('\\')
            append(character)
        }
        append('"')
    }

/**
 * Returns whether a quoted selector string must escape this character.
 */
private fun Char.needsEscape(): Boolean = this == '\\' || this == '"'

/**
 * Renders a named or presence condition.
 */
private fun SelectorStringCondition.render(): String =
    when (this) {
        is SelectorStringCondition.Named -> value
        is SelectorStringCondition.Presence -> value.value
    }

/**
 * Renders scoreboard objective ranges.
 */
private fun List<SelectorScoreRequirement>.render(): String =
    joinToString(",", "{", "}") { (objective, range) -> "$objective=$range" }

/**
 * Renders advancement requirements.
 */
private fun List<SelectorAdvancementRequirement>.render(): String =
    joinToString(",", "{", "}") { (advancement, progress) ->
        "${advancement.asString()}=${progress.render()}"
    }

/**
 * Renders complete-advancement state or a criteria map.
 */
private fun SelectorAdvancementProgress.render(): String =
    when (this) {
        is SelectorAdvancementProgress.Completion -> completed.toString()
        is SelectorAdvancementProgress.Criteria -> criteria.render()
    }

/**
 * Renders advancement criteria states.
 */
private fun List<SelectorAdvancementCriterion>.render(): String =
    joinToString(",", "{", "}") { (name, completed) -> "$name=$completed" }
