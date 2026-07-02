package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.GameMode
import io.github.lmliam.kotventure.core.selector.SelectorEntityType
import io.github.lmliam.kotventure.core.selector.SelectorStringCondition
import io.github.lmliam.kotventure.core.selector.SnbtCompoundSource

internal fun SelectorReader.readGamemodeArgument(): EntitySelectorArgument.GameMode {
    val negated = consume('!')
    val tokenOffset = offset
    val token = readValueToken()
    val gamemode =
        GameMode.entries.firstOrNull { it.value == token }
            ?: failAt(tokenOffset, "Unsupported game mode '$token'")
    return EntitySelectorArgument.GameMode(gamemode, negated)
}

internal fun SelectorReader.readNameArgument(): EntitySelectorArgument.Name {
    val negated = consume('!')
    val next = peek()
    if (next == '"' || next == '\'') {
        return EntitySelectorArgument.Name(readQuotedString(), negated)
    }
    val tokenOffset = offset
    val token = readValueToken()
    validateUnquotedToken(token, tokenOffset, description = "name")
    return EntitySelectorArgument.Name(token, isNegated = negated)
}

internal fun SelectorReader.readTypeArgument(): EntitySelectorArgument.Type {
    val negated = consume('!')
    val isTag = consume('#')
    val key = readSelectorKey()
    val target = if (isTag) SelectorEntityType.Tag(key) else SelectorEntityType.Direct(key)
    return EntitySelectorArgument.Type(target, negated)
}

internal fun SelectorReader.readTagArgument(): EntitySelectorArgument.Tag =
    EntitySelectorArgument.Tag(readStringCondition())

internal fun SelectorReader.readTeamArgument(): EntitySelectorArgument.Team =
    EntitySelectorArgument.Team(readStringCondition())

private fun SelectorReader.readStringCondition(): SelectorStringCondition {
    val negated = consume('!')
    val tokenOffset = offset
    val token = readValueToken()
    if (token.isNotEmpty()) validateUnquotedToken(token, tokenOffset)
    return SelectorStringCondition.of(token, negated)
}

internal fun SelectorReader.readNbtArgument(): EntitySelectorArgument.Nbt {
    val negated = consume('!')
    val start = offset
    validateSnbtCompound()
    return EntitySelectorArgument.Nbt(SnbtCompoundSource.trusted(substringFrom(start)), negated)
}

internal fun SelectorReader.readPredicateArgument(): EntitySelectorArgument.Predicate {
    val negated = consume('!')
    return EntitySelectorArgument.Predicate(readSelectorKey(), negated)
}
