package io.github.lmliam.kotventure.core.selector

internal data class SelectorNbtFilter(
    val renderedCompound: String,
    val isNegated: Boolean,
) {
    val rendered: String
        get() = if (isNegated) "!$renderedCompound" else renderedCompound
}
