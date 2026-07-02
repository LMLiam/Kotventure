package io.github.lmliam.kotventure.core.selector

internal fun EntitySelectorArgument.render(): String = "$argumentName=${renderValue()}"

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
            scores.joinToString(",", "{", "}") { "${it.objective}=${it.range}" }

        is EntitySelectorArgument.Predicate -> "$negationPrefix${key.asString()}"
        is EntitySelectorArgument.Advancements ->
            advancements.joinToString(",", "{", "}") {
                "${it.advancement.asString()}=${it.progress.render()}"
            }
    }

private val EntitySelectorArgument.Negatable.negationPrefix: String
    get() = if (isNegated) "!" else ""

private fun SelectorEntityType.render(): String =
    when (this) {
        is SelectorEntityType.Direct -> key.asString()
        is SelectorEntityType.Tag -> "#${key.asString()}"
    }

private fun EntitySelectorArgument.Name.renderQuotable(): String =
    if (value.all(Char::isAllowedInUnquotedSelectorToken)) value else value.quoteSelectorString()

private fun String.quoteSelectorString(): String =
    buildString(length + 2) {
        append('"')
        this@quoteSelectorString.forEach { character ->
            if (character == '\\' || character == '"') append('\\')
            append(character)
        }
        append('"')
    }

private fun SelectorStringCondition.render(): String =
    when (this) {
        is SelectorStringCondition.Named -> value
        is SelectorStringCondition.Presence -> value.value
    }

private fun SelectorAdvancementProgress.render(): String =
    when (this) {
        is SelectorAdvancementProgress.Completion -> completed.toString()
        is SelectorAdvancementProgress.Criteria ->
            criteria.joinToString(",", "{", "}") { "${it.name}=${it.completed}" }
    }
