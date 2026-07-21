package io.github.lmliam.kotventure.core.selector

/**
 * The floating-point range arguments that Java Edition selectors support.
 *
 * @property argumentName The canonical selector argument name.
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
