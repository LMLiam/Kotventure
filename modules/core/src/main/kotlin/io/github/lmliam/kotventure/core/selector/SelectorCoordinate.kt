package io.github.lmliam.kotventure.core.selector

/**
 * Coordinate names supported by Java Edition selectors.
 *
 * @property argumentName canonical selector argument name
 */
public enum class SelectorCoordinate(
    public val argumentName: String,
) {
    /** Origin X coordinate. */
    X("x"),

    /** Origin Y coordinate. */
    Y("y"),

    /** Origin Z coordinate. */
    Z("z"),

    /** Bounding-volume X delta. */
    DX("dx"),

    /** Bounding-volume Y delta. */
    DY("dy"),

    /** Bounding-volume Z delta. */
    DZ("dz"),
}
