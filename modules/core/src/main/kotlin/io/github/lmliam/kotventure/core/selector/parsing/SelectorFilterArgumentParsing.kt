package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.GameMode
import io.github.lmliam.kotventure.core.selector.SELECTOR_NEGATION_PREFIX
import io.github.lmliam.kotventure.core.selector.SelectorEntityType
import io.github.lmliam.kotventure.core.selector.SelectorStringCondition
import io.github.lmliam.kotventure.core.selector.SnbtCompoundSource

private inline fun <T> SelectorReader.withNegation(block: SelectorReader.(isNegated: Boolean) -> T): T {
    val negated = consumeNegation()
    return block(negated)
}

internal fun SelectorReader.readGamemodeArgument(): EntitySelectorArgument.GameMode =
    withNegation { negated ->
        val tokenOffset = offset
        val token = readValueToken()
        val gamemode =
            GameMode.entries.find { it.value == token }
                ?: failAt(tokenOffset, "Unsupported game mode '$token'")
        EntitySelectorArgument.GameMode(gamemode, negated)
    }

internal fun SelectorReader.readNameArgument(): EntitySelectorArgument.Name =
    withNegation { negated ->
        val next = peek()
        if (next == '"' || next == '\'') return@withNegation EntitySelectorArgument.Name(readQuotedString(), negated)

        val tokenOffset = offset
        val token = readValueToken()
        validateUnquotedToken(token, tokenOffset, description = "name")
        EntitySelectorArgument.Name(token, negated)
    }

internal fun SelectorReader.readTypeArgument(): EntitySelectorArgument.Type =
    withNegation { negated ->
        val target =
            if (consume('#')) {
                SelectorEntityType.Tag(readSelectorKey())
            } else {
                SelectorEntityType.Direct(readSelectorKey())
            }
        EntitySelectorArgument.Type(target, negated)
    }

internal fun SelectorReader.readTagArgument(): EntitySelectorArgument.Tag =
    EntitySelectorArgument.Tag(readStringCondition())

internal fun SelectorReader.readTeamArgument(): EntitySelectorArgument.Team =
    EntitySelectorArgument.Team(readStringCondition())

private fun SelectorReader.readStringCondition(): SelectorStringCondition =
    withNegation { negated ->
        val tokenOffset = offset
        val token = readValueToken().also { if (it.isNotEmpty()) validateUnquotedToken(it, tokenOffset) }
        SelectorStringCondition(token, negated)
    }

internal fun SelectorReader.readNbtArgument(): EntitySelectorArgument.Nbt =
    withNegation { negated ->
        val start = offset
        validateSnbtCompound()
        EntitySelectorArgument.Nbt(SnbtCompoundSource(substringFrom(start)), negated)
    }

internal fun SelectorReader.readPredicateArgument(): EntitySelectorArgument.Predicate =
    withNegation { negated -> EntitySelectorArgument.Predicate(readSelectorKey(), negated) }

private fun SelectorReader.consumeNegation(): Boolean = consume(SELECTOR_NEGATION_PREFIX)
