package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.renderValue

/**
 * Render this argument as `name=value` selector source text.
 *
 * Coordinates and ranges own their own argument names; keyword arguments resolve names via
 * [argumentName]. Negation is included in the rendered value when applicable.
 */
internal fun EntitySelectorArgument.render(): String = "$argumentName=${renderValue()}"

/**
 * Render the value portion of this argument (everything after `name=`).
 *
 * Handles negation prefixes, quoting, compound structures, and range formatting as appropriate.
 */
private fun EntitySelectorArgument.renderValue(): String =
    when (this) {
        is EntitySelectorArgument.Coordinate -> formatSelectorNumber(value)
        is EntitySelectorArgument.Range -> range.toString()
        is EntitySelectorArgument.Limit -> value.toString()
        is EntitySelectorArgument.Sort -> value.value
        is EntitySelectorArgument.Level -> range.toString()
        is EntitySelectorArgument.GameMode -> "$negationPrefix${value.value}"
        is EntitySelectorArgument.Name -> "$negationPrefix${renderQuotable()}"
        is EntitySelectorArgument.Type -> "$negationPrefix${target.render()}"
        is EntitySelectorArgument.Tag -> "$negationPrefix${condition.render()}"
        is EntitySelectorArgument.Team -> "$negationPrefix${condition.render()}"
        is EntitySelectorArgument.Nbt -> "$negationPrefix${snbt.value}"
        is EntitySelectorArgument.Scores ->
            scores.joinToString(",", "{", "}") { (objective, range) -> "$objective=$range" }

        is EntitySelectorArgument.Predicate -> "$negationPrefix${key.asString()}"
        is EntitySelectorArgument.Advancements ->
            advancements.joinToString(",", "{", "}") { (advancement, progress) ->
                "${advancement.asString()}=${progress.render()}"
            }
    }

/**
 * Render the negation prefix (`!`) if this argument is negated, or an empty string otherwise.
 */
private val EntitySelectorArgument.Negatable.negationPrefix: String
    get() = if (isNegated) SELECTOR_NEGATION_PREFIX.toString() else ""

/**
 * Render an entity type or entity-type tag.
 *
 * Direct types are rendered as-is; tags are prefixed with `#`.
 */
private fun SelectorEntityType.render(): String =
    when (this) {
        is SelectorEntityType.Direct -> key.asString()
        is SelectorEntityType.Tag -> "#${key.asString()}"
    }

/**
 * Render a name, quoting it only if it contains characters disallowed in unquoted selector tokens.
 */
private fun EntitySelectorArgument.Name.renderQuotable(): String =
    if (value.all(Char::isAllowedInUnquotedSelectorToken)) value else value.quoteSelectorString()

/**
 * Render a string as a quoted selector string with proper escape sequences.
 *
 * Quotes and backslashes are escaped; the string is wrapped in double quotes.
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
 * Check if this character needs to be escaped in a quoted selector string.
 *
 * Backslashes and double quotes require escaping.
 */
private fun Char.needsEscape(): Boolean = this == '\\' || this == '"'

/**
 * Render a string condition (named value or presence keyword).
 */
private fun SelectorStringCondition.render(): String =
    when (this) {
        is SelectorStringCondition.Named -> value
        is SelectorStringCondition.Presence -> value.value
    }

/**
 * Render advancement progress: either a boolean for full completion or a criteria map.
 */
private fun SelectorAdvancementProgress.render(): String =
    when (this) {
        is SelectorAdvancementProgress.Completion -> completed.toString()
        is SelectorAdvancementProgress.Criteria ->
            criteria.joinToString(",", "{", "}") { (name, completed) -> "$name=$completed" }
    }
