package io.github.lmliam.kotventure.core.selector

/**
 * Floating-point range arguments supported by Java Edition selectors.
 *
 * @property argumentName canonical selector argument name
 */
public enum class SelectorRangeArgument(
    public val argumentName: String,
    internal val hasNonNegativeOrderedBounds: Boolean,
) {
    /** Distance from the selector origin; bounds must be non-negative and ordered. */
    DISTANCE("distance", hasNonNegativeOrderedBounds = true),

    /** Vertical rotation; descending wrap-around bounds are valid. */
    X_ROTATION("x_rotation", hasNonNegativeOrderedBounds = false),

    /** Horizontal rotation; descending wrap-around bounds are valid. */
    Y_ROTATION("y_rotation", hasNonNegativeOrderedBounds = false),
}
