package io.github.lmliam.kotventure.core.selector

/**
 * One finite selector bounding-volume delta.
 *
 * Create a delta with the scoped `dx`, `dy`, or `dz` property. Then, supply it to `volume`, for example
 * `volume(16.dx, 8.dy)`. A delta is immutable and belongs to one axis.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorPositionVolumeSample
 */
public class VolumeDelta internal constructor(
    internal val coordinate: SelectorCoordinate,
    internal val value: Double,
)

internal fun volumeDelta(
    coordinate: SelectorCoordinate,
    value: Number,
): VolumeDelta = VolumeDelta(coordinate, validatedCoordinateValue("volume", coordinate, value))
