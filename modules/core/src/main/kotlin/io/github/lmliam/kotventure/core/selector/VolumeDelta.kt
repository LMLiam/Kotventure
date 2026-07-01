package io.github.lmliam.kotventure.core.selector

/**
 * A single selector bounding-volume delta, produced by the scoped `dx`, `dy`, and `dz` properties:
 * `volume(16.dx, 8.dy)`.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorPositionVolumeSample
 */
public class VolumeDelta internal constructor(
    internal val axis: VolumeAxis,
    internal val value: Double,
)

internal fun volumeDelta(
    axis: VolumeAxis,
    value: Number,
): VolumeDelta {
    val delta = value.toDouble()
    require(delta.isFinite()) { "Selector volume ${axis.argument} must be finite, got: $delta" }
    return VolumeDelta(axis, delta)
}
