package io.github.lmliam.kotventure.core.selector

/**
 * Polarity of a singleton selector argument: one positive value, or accumulated exclusions.
 */
internal sealed interface SelectorFilter<out T> {
    data class Positive<T>(
        val value: T,
    ) : SelectorFilter<T>

    data class Negative<T>(
        val values: List<T>,
    ) : SelectorFilter<T>
}

internal fun <T> SelectorFilter<T>?.including(
    argument: String,
    value: T,
): SelectorFilter<T> =
    when (this) {
        null -> SelectorFilter.Positive(value)
        is SelectorFilter.Positive ->
            error("Selector argument '$argument' is already set; vanilla syntax allows one positive value.")

        is SelectorFilter.Negative ->
            error(
                "Selector argument '$argument' already has exclusions; vanilla syntax cannot combine them with a positive value.",
            )
    }

internal fun <T> SelectorFilter<T>?.excluding(
    argument: String,
    value: T,
): SelectorFilter<T> =
    when (this) {
        null -> SelectorFilter.Negative(listOf(value))
        is SelectorFilter.Negative -> copy(values = values + value)
        is SelectorFilter.Positive ->
            error(
                "Selector argument '$argument' already has a positive value; vanilla syntax cannot combine it with exclusions.",
            )
    }
