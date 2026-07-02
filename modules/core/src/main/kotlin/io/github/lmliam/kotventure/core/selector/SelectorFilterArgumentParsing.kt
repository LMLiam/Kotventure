package io.github.lmliam.kotventure.core.selector

internal fun SelectorReader.readGamemodeArgument(): EntitySelectorArgument.Gamemode {
    val negated = consume('!')
    val tokenOffset = offset
    val token = readValueToken()
    val gamemode =
        GameMode.entries.firstOrNull { it.value == token }
            ?: failAt(tokenOffset, "Unsupported game mode '$token'")
    return EntitySelectorArgument.Gamemode(gamemode, negated)
}

internal fun SelectorReader.readNameArgument(): EntitySelectorArgument.Name {
    val negated = consume('!')
    val next = peek()
    if (next == '"' || next == '\'') {
        val quoted = readQuotedString()
        return EntitySelectorArgument.Name(quoted.value, negated)
    }
    val tokenOffset = offset
    val token = readValueToken()
    validateUnquotedToken(token, tokenOffset, description = "name")
    return EntitySelectorArgument.Name(token, isNegated = negated)
}

internal fun SelectorReader.readTypeArgument(): EntitySelectorArgument.Type {
    val negated = consume('!')
    val isTag = consume('#')
    return EntitySelectorArgument.Type(readSelectorKey(), isTag, negated)
}

internal fun SelectorReader.readTagArgument(): EntitySelectorArgument.Tag {
    val negated = consume('!')
    val tokenOffset = offset
    val token = readValueToken()
    validateUnquotedToken(token, tokenOffset)
    return EntitySelectorArgument.Tag(token.selectorStringCondition(negated), token.isNotEmpty() && negated)
}

internal fun SelectorReader.readTeamArgument(): EntitySelectorArgument.Team {
    val negated = consume('!')
    val tokenOffset = offset
    val token = readValueToken()
    validateUnquotedToken(token, tokenOffset)
    return EntitySelectorArgument.Team(token.selectorStringCondition(negated), token.isNotEmpty() && negated)
}

internal fun SelectorReader.readNbtArgument(): EntitySelectorArgument.Nbt {
    val negated = consume('!')
    val start = offset
    validateSnbtCompound()
    return EntitySelectorArgument.Nbt(SnbtCompoundSource.validated(substringFrom(start)), negated)
}

internal fun SelectorReader.readPredicateArgument(): EntitySelectorArgument.Predicate {
    val negated = consume('!')
    return EntitySelectorArgument.Predicate(readSelectorKey(), negated)
}

private fun String.selectorStringCondition(isNegated: Boolean): SelectorStringCondition =
    if (isEmpty()) {
        SelectorStringCondition.Presence(
            if (isNegated) SelectorPresence.ANY else SelectorPresence.NONE,
        )
    } else {
        SelectorStringCondition.Named(this)
    }
