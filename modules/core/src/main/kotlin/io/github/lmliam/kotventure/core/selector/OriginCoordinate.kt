package io.github.lmliam.kotventure.core.selector

/**
 * A single selector origin coordinate, produced by the scoped `x`, `y`, and `z` properties:
 * `origin(12.5.x, 64.y)`.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorPositionVolumeSample
 */
public class OriginCoordinate internal constructor(
    internal val axis: String,
    internal val value: Double,
)

internal fun originCoordinate(
    axis: String,
    value: Number,
): OriginCoordinate {
    val coordinate = value.toDouble()
    require(coordinate.isFinite()) { "Selector origin $axis must be finite, got: $coordinate" }
    return OriginCoordinate(axis, coordinate)
}
