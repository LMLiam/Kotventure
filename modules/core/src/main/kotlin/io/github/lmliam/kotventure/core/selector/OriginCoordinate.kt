package io.github.lmliam.kotventure.core.selector

/**
 * One finite selector-origin coordinate.
 *
 * Create a coordinate with the scoped `x`, `y`, or `z` property. Then, supply it to `origin`, for example
 * `origin(12.5.x, 64.y)`. A coordinate is immutable and belongs to one axis.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorPositionVolumeSample
 */
public class OriginCoordinate internal constructor(
    internal val coordinate: SelectorCoordinate,
    internal val value: Double,
)

internal fun originCoordinate(
    coordinate: SelectorCoordinate,
    value: Number,
): OriginCoordinate = OriginCoordinate(coordinate, validatedCoordinateValue("origin", coordinate, value))
