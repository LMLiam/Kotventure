package io.github.lmliam.kotventure.core.selector

internal fun EntitySelectorArgument.render(): String =
    when (this) {
        is EntitySelectorArgument.Coordinate -> "${coordinate.argumentName}=${formatSelectorNumber(value)}"
        is EntitySelectorArgument.Range -> "${argument.argumentName}=$range"
        is EntitySelectorArgument.Limit -> "limit=$value"
        is EntitySelectorArgument.Sort -> "sort=${value.value}"
        is EntitySelectorArgument.Level -> "level=$range"
        is EntitySelectorArgument.Gamemode -> "gamemode=$negationPrefix${value.value}"
        is EntitySelectorArgument.Name -> "name=$negationPrefix${renderQuotable()}"
        is EntitySelectorArgument.Type -> "type=$negationPrefix${if (isTag) "#" else ""}${key.asString()}"
        is EntitySelectorArgument.Tag -> "tag=$negationPrefix$value"
        is EntitySelectorArgument.Team -> "team=$negationPrefix$value"
        is EntitySelectorArgument.Nbt -> "nbt=$negationPrefix$snbt"
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

private fun EntitySelectorArgument.Name.renderQuotable(): String {
    val selectedQuote = quote ?: if (value.all(Char::isAllowedInUnquotedSelectorToken)) null else '"'
    return selectedQuote?.let { value.quoteSelectorString(it) } ?: value
}

private fun String.quoteSelectorString(quote: Char): String =
    buildString(length + 2) {
        append(quote)
        this@quoteSelectorString.forEach { character ->
            if (character == '\\' || character == quote) append('\\')
            append(character)
        }
        append(quote)
    }

private fun ParsedAdvancementProgress.render(): String =
    when (this) {
        is ParsedAdvancementProgress.Completion -> completed.toString()
        is ParsedAdvancementProgress.Criteria ->
            criteria.joinToString(",", "{", "}") { "${it.name}=${it.completed}" }
    }
