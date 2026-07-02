package io.github.lmliam.kotventure.core.selector

/**
 * A single selector origin coordinate, produced by the scoped `x`, `y`, and `z` properties:
 * `origin(12.5.x, 64.y)`.
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
