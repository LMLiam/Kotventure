package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

internal fun parseCoordinateArgument(
    name: String,
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Coordinate =
    EntitySelectorArgument.Coordinate(
        coordinate = SelectorCoordinate.entries.first { it.argumentName == name },
        value = parseSelectorDouble(value, valueOffset),
    )

internal fun parseFloatingRangeArgument(
    name: String,
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Range {
    val range = parseSelectorRange(value, valueOffset)
    if (name == "distance") {
        if ((range.minimum ?: 0.0) < 0.0 || (range.maximum ?: 0.0) < 0.0) {
            fail(valueOffset, "Distance bounds must be non-negative")
        }
        if (range.minimum != null && range.maximum != null && range.minimum > range.maximum) {
            fail(valueOffset, "Distance minimum must not exceed maximum")
        }
    }
    return EntitySelectorArgument.Range(
        argument = SelectorRangeArgument.entries.first { it.argumentName == name },
        range = range,
    )
}

internal fun parseSelectorIntRange(
    value: String,
    valueOffset: Int,
    nonNegative: Boolean,
): SelectorIntRange {
    val bounds = splitSelectorRange(value, valueOffset)
    val minimum = bounds.first.takeIf(String::isNotEmpty)?.let { parseSelectorInt(it, valueOffset) }
    val maximum =
        bounds.second?.takeIf(String::isNotEmpty)?.let {
            parseSelectorInt(it, valueOffset + value.indexOf("..") + 2)
        }
    if (nonNegative && ((minimum ?: 0) < 0 || (maximum ?: 0) < 0)) {
        fail(valueOffset, "Level bounds must be non-negative")
    }
    if (minimum != null && maximum != null && minimum > maximum) {
        fail(valueOffset, "Range minimum must not exceed maximum")
    }
    return SelectorIntRange(minimum, maximum)
}

internal fun parseLimitArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Limit {
    val limit = parseSelectorInt(value, valueOffset)
    if (limit <= 0) fail(valueOffset, "Selector limit must be positive")
    return EntitySelectorArgument.Limit(limit)
}

internal fun parseSortArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Sort {
    val sort =
        SelectorSort.entries.firstOrNull { it.value == value }
            ?: fail(valueOffset, "Unsupported selector sort '$value'")
    return EntitySelectorArgument.Sort(sort)
}

internal fun parseGamemodeArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Gamemode {
    val negated = value.startsWith('!')
    val token = value.removeSelectorNegation()
    val gamemode =
        GameMode.entries.firstOrNull { it.value == token }
            ?: fail(valueOffset + if (negated) 1 else 0, "Unsupported game mode '$token'")
    return EntitySelectorArgument.Gamemode(gamemode, negated)
}

internal fun parseNameArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Name {
    val negated = value.startsWith('!')
    val token = value.removeSelectorNegation()
    val tokenOffset = valueOffset + if (negated) 1 else 0
    if (token.startsWith('"') || token.startsWith('\'')) {
        val quote = token.first()
        if (token.length < 2 || token.last() != quote) {
            fail(tokenOffset, "Unterminated quoted string")
        }
        return EntitySelectorArgument.Name(
            value = decodeSelectorQuotedString(token, tokenOffset),
            quote = quote,
            isNegated = negated,
        )
    }
    if (!token.all(Char::isAllowedInUnquotedSelectorToken)) {
        fail(tokenOffset, "Invalid unquoted selector name")
    }
    return EntitySelectorArgument.Name(token, quote = null, isNegated = negated)
}

internal fun parseTypeArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Type {
    val negated = value.startsWith('!')
    var token = value.removeSelectorNegation()
    var tokenOffset = valueOffset + if (negated) 1 else 0
    val isTag = token.startsWith('#')
    if (isTag) {
        token = token.substring(1)
        tokenOffset++
    }
    return EntitySelectorArgument.Type(parseSelectorKey(token, tokenOffset), isTag, negated)
}

internal fun parseTagArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Tag {
    val negated = value.startsWith('!')
    val token = value.removeSelectorNegation()
    validateSelectorUnquotedToken(token, valueOffset + if (negated) 1 else 0, allowEmpty = true)
    return EntitySelectorArgument.Tag(token, negated)
}

internal fun parseTeamArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Team {
    val negated = value.startsWith('!')
    val token = value.removeSelectorNegation()
    validateSelectorUnquotedToken(token, valueOffset + if (negated) 1 else 0, allowEmpty = true)
    return EntitySelectorArgument.Team(token, negated)
}

internal fun parsePredicateArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Predicate {
    val negated = value.startsWith('!')
    val token = value.removeSelectorNegation()
    val tokenOffset = valueOffset + if (negated) 1 else 0
    return EntitySelectorArgument.Predicate(parseSelectorKey(token, tokenOffset), negated)
}

internal fun parseSelectorBoolean(
    value: String,
    valueOffset: Int,
): Boolean =
    when (value) {
        "true" -> true
        "false" -> false
        else -> fail(valueOffset, "Expected 'true' or 'false'")
    }

internal fun parseSelectorKey(
    value: String,
    valueOffset: Int,
): Key {
    if (value.isEmpty()) fail(valueOffset, "Expected a namespaced key")
    return try {
        Key.key(value)
    } catch (_: InvalidKeyException) {
        fail(valueOffset, "Invalid namespaced key '$value'")
    }
}

internal fun validateSelectorUnquotedToken(
    value: String,
    valueOffset: Int,
    allowEmpty: Boolean = false,
) {
    if (!allowEmpty && value.isEmpty()) {
        fail(valueOffset, "Expected a non-empty selector token")
    }
    if (!value.all(Char::isAllowedInUnquotedSelectorToken)) {
        fail(valueOffset, "Invalid unquoted selector token")
    }
}

private fun parseSelectorRange(
    value: String,
    valueOffset: Int,
): SelectorRange {
    val bounds = splitSelectorRange(value, valueOffset)
    val minimum = bounds.first.takeIf(String::isNotEmpty)?.let { parseSelectorDouble(it, valueOffset) }
    val maximum =
        bounds.second?.takeIf(String::isNotEmpty)?.let {
            parseSelectorDouble(it, valueOffset + value.indexOf("..") + 2)
        }
    return SelectorRange(minimum, maximum)
}

private fun splitSelectorRange(
    value: String,
    valueOffset: Int,
): Pair<String, String?> {
    if (value.isEmpty()) {
        fail(valueOffset, "Expected a range")
    }
    val separator = value.indexOf("..")
    if (separator < 0) return value to null
    val secondSeparator = value.indexOf("..", separator + 1)
    if (secondSeparator >= 0) {
        fail(valueOffset + secondSeparator, "Range contains more than one '..' separator")
    }
    val minimum = value.substring(0, separator)
    val maximum = value.substring(separator + 2)
    if (minimum.isEmpty() && maximum.isEmpty()) {
        fail(valueOffset, "Range must contain at least one bound")
    }
    return minimum to maximum
}

private fun parseSelectorDouble(
    value: String,
    valueOffset: Int,
): Double {
    if (!SELECTOR_DOUBLE.matches(value)) {
        fail(valueOffset, "Expected a finite decimal number")
    }
    return value.toDoubleOrNull()?.takeIf(Double::isFinite)
        ?: fail(valueOffset, "Expected a finite decimal number")
}

private fun parseSelectorInt(
    value: String,
    valueOffset: Int,
): Int {
    if (!SELECTOR_INTEGER.matches(value)) {
        fail(valueOffset, "Expected an integer")
    }
    return value.toIntOrNull() ?: fail(valueOffset, "Integer is outside the supported range")
}

private fun decodeSelectorQuotedString(
    value: String,
    valueOffset: Int,
): String {
    val quote = value.first()
    return buildString(value.length - 2) {
        var index = 1
        while (index < value.lastIndex) {
            val character = value[index]
            if (character != '\\') {
                append(character)
                index++
                continue
            }
            if (index + 1 >= value.lastIndex) {
                fail(valueOffset + index, "Unterminated escape sequence")
            }
            val escaped = value[index + 1]
            if (escaped != quote && escaped != '\\') {
                fail(valueOffset + index, "Invalid quoted-string escape")
            }
            append(escaped)
            index += 2
        }
    }
}

internal fun String.removeSelectorNegation(): String = if (startsWith('!')) substring(1) else this

private val SELECTOR_DOUBLE: Regex = Regex("-?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)")
private val SELECTOR_INTEGER: Regex = Regex("-?[0-9]+")
