package io.github.lmliam.kotventure.core.selector

internal sealed interface SelectorFilter<out T> {
    data class Included<T>(
        val value: T,
    ) : SelectorFilter<T>

    data class Excluded<T>(
        val values: List<T>,
    ) : SelectorFilter<T>
}

internal fun <T> T.asIncludedFilter(): SelectorFilter<T> = SelectorFilter.Included(this)

internal fun <T> SelectorFilter<T>?.excluding(value: T): SelectorFilter<T> =
    when (this) {
        is SelectorFilter.Excluded -> copy(values = values + value)
        else -> SelectorFilter.Excluded(listOf(value))
    }

internal fun <T> SelectorFilter<T>.renderValues(render: (T) -> String): List<String> =
    when (this) {
        is SelectorFilter.Included -> listOf(render(value))
        is SelectorFilter.Excluded -> values.map { value -> "!${render(value)}" }
    }
