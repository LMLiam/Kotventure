package io.github.lmliam.kotventure.core.sound

/**
 * World coordinates for position-based sound playback ([PlaySoundScope.at]).
 */
internal data class SoundPosition(
    val x: Double,
    val y: Double,
    val z: Double,
)
