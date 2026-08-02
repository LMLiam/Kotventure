package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.GameMode
import io.github.lmliam.kotventure.core.selector.SELECTOR_NEGATION_PREFIX
import io.github.lmliam.kotventure.core.selector.SelectorEntityType
import io.github.lmliam.kotventure.core.selector.SelectorStringCondition
import io.github.lmliam.kotventure.core.selector.SnbtCompoundSource

private inline fun <T> SelectorReader.withNegation(block: SelectorReader.(isNegated: Boolean) -> T): T {
    val isNegated = consumeNegation()
    return block(isNegated)
}

internal fun SelectorReader.readGamemodeArgument(): EntitySelectorArgument.GameMode =
    withNegation { isNegated ->
        val tokenOffset = offset
        val token = readValueToken()
        val gameMode =
            GameMode.fromValue(token)
                ?: failAt(tokenOffset, "Unsupported game mode '$token'")

        EntitySelectorArgument.GameMode(gameMode, isNegated)
    }

internal fun SelectorReader.readNameArgument(): EntitySelectorArgument.Name =
    withNegation { isNegated ->
        val name =
            when (peek()) {
                '"', '\'' -> readQuotedString()
                else -> readValidatedValueToken(description = "name")
            }

        EntitySelectorArgument.Name(name, isNegated)
    }

internal fun SelectorReader.readTypeArgument(): EntitySelectorArgument.Type =
    withNegation { isNegated ->
        val target =
            if (consume('#')) {
                SelectorEntityType.Tag(readSelectorKey())
            } else {
                SelectorEntityType.Direct(readSelectorKey())
            }

        EntitySelectorArgument.Type(target, isNegated)
    }

internal fun SelectorReader.readTagArgument(): EntitySelectorArgument.Tag =
    EntitySelectorArgument.Tag(readStringCondition())

internal fun SelectorReader.readTeamArgument(): EntitySelectorArgument.Team =
    EntitySelectorArgument.Team(readStringCondition())

private fun SelectorReader.readStringCondition(): SelectorStringCondition =
    withNegation { isNegated ->
        SelectorStringCondition(readValidatedValueToken(allowEmpty = true), isNegated)
    }

internal fun SelectorReader.readNbtArgument(): EntitySelectorArgument.Nbt =
    withNegation { isNegated ->
        val start = offset
        validateSnbtCompound()
        EntitySelectorArgument.Nbt(SnbtCompoundSource(substringFrom(start)), isNegated)
    }

internal fun SelectorReader.readPredicateArgument(): EntitySelectorArgument.Predicate =
    withNegation { isNegated ->
        EntitySelectorArgument.Predicate(readSelectorKey(), isNegated)
    }

private fun SelectorReader.readValidatedValueToken(
    description: String = "token",
    allowEmpty: Boolean = false,
): String {
    val tokenOffset = offset
    val token = readValueToken()

    if (token.isNotEmpty() || !allowEmpty) {
        validateUnquotedToken(token, tokenOffset, description)
    }

    return token
}

private fun SelectorReader.consumeNegation(): Boolean = consume(SELECTOR_NEGATION_PREFIX)
