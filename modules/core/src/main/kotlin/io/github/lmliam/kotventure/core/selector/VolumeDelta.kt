package io.github.lmliam.kotventure.core.selector

/**
 * A single selector bounding-volume delta, produced by the scoped `dx`, `dy`, and `dz` properties:
 * `volume(16.dx, 8.dy)`.
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
