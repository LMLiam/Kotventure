package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.renderValue

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
 * Returns `!` when this argument is negated, or an empty string otherwise.
 */
private val EntitySelectorArgument.Negatable.negationPrefix: String
    get() = if (isNegated) SELECTOR_NEGATION_PREFIX.toString() else ""

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
private fun EntitySelectorArgument.Name.renderQuotable(): String =
    if (value.all(Char::isAllowedInUnquotedSelectorToken)) value else value.quoteSelectorString()

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
 * Renders complete-advancement state or a criteria map.
 */
private fun SelectorAdvancementProgress.render(): String =
    when (this) {
        is SelectorAdvancementProgress.Completion -> completed.toString()
        is SelectorAdvancementProgress.Criteria ->
            criteria.joinToString(",", "{", "}") { (name, completed) -> "$name=$completed" }
    }
