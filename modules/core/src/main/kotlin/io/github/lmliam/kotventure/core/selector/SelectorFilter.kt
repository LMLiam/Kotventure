package io.github.lmliam.kotventure.core.selector

internal sealed interface SelectorFilter<out T> {
    data class Included<T>(
        val value: T,
    ) : SelectorFilter<T>

    data class Excluded<T>(
        val values: List<T>,
    ) : SelectorFilter<T>
}

internal fun <T> SelectorFilter<T>?.including(
    argument: String,
    value: T,
): SelectorFilter<T> =
    when (this) {
        null -> SelectorFilter.Included(value)
        is SelectorFilter.Included ->
            error("Selector argument '$argument' is already set; vanilla syntax allows one positive value.")
        is SelectorFilter.Excluded ->
            error(
                "Selector argument '$argument' already has exclusions; vanilla syntax cannot combine them with a positive value.",
            )
    }

internal fun <T> SelectorFilter<T>?.excluding(
    argument: String,
    value: T,
): SelectorFilter<T> =
    when (this) {
        null -> SelectorFilter.Excluded(listOf(value))
        is SelectorFilter.Excluded -> copy(values = values + value)
        is SelectorFilter.Included ->
            error(
                "Selector argument '$argument' already has a positive value; vanilla syntax cannot combine it with exclusions.",
            )
    }

internal fun <T> SelectorFilter<T>.renderValues(render: (T) -> String): List<String> =
    when (this) {
        is SelectorFilter.Included -> listOf(render(value))
        is SelectorFilter.Excluded -> values.map { value -> "!${render(value)}" }
    }
