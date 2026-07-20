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
    /** Distance from the selector origin. Bounds must be non-negative and in ascending order. */
    DISTANCE("distance", hasNonNegativeOrderedBounds = true),

    /** Vertical rotation. Descending wrap-around bounds are valid. */
    X_ROTATION("x_rotation", hasNonNegativeOrderedBounds = false),

    /** Horizontal rotation. Descending wrap-around bounds are valid. */
    Y_ROTATION("y_rotation", hasNonNegativeOrderedBounds = false),
}
