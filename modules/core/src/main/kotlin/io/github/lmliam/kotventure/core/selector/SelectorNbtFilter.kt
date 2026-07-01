package io.github.lmliam.kotventure.core.selector

internal data class SelectorNbtFilter(
    val compound: String,
    val isNegated: Boolean,
) {
    val rendered: String
        get() = if (isNegated) "!$compound" else compound
}
