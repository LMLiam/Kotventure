package io.github.lmliam.kotventure.core.selector

internal fun EntitySelectorArgument.render(): String =
    when (this) {
        is EntitySelectorArgument.Coordinate -> "${coordinate.argumentName}=${formatSelectorNumber(value)}"
        is EntitySelectorArgument.Range -> "${argument.argumentName}=$range"
        is EntitySelectorArgument.Limit -> "limit=$value"
        is EntitySelectorArgument.Sort -> "sort=${value.value}"
        is EntitySelectorArgument.Level -> "level=$range"
        is EntitySelectorArgument.GameMode -> "gamemode=$negationPrefix${value.value}"
        is EntitySelectorArgument.Name -> "name=$negationPrefix${renderQuotable()}"
        is EntitySelectorArgument.Type -> "type=$negationPrefix${target.render()}"
        is EntitySelectorArgument.Tag -> "tag=$negationPrefix${condition.render()}"
        is EntitySelectorArgument.Team -> "team=$negationPrefix${condition.render()}"
        is EntitySelectorArgument.Nbt -> "nbt=$negationPrefix${snbt.value}"
        is EntitySelectorArgument.Scores ->
            scores.joinToString(",", "scores={", "}") { "${it.objective}=${it.range}" }

        is EntitySelectorArgument.Predicate -> "predicate=$negationPrefix${key.asString()}"
        is EntitySelectorArgument.Advancements ->
            advancements.joinToString(",", "advancements={", "}") {
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
