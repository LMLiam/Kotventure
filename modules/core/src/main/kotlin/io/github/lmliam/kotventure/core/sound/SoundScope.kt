package io.github.lmliam.kotventure.core.sound

import net.kyori.adventure.sound.Sound

/**
 * Configures the source, volume, pitch, and weighted-selection seed of an Adventure [Sound].
 *
 * Unset slots keep Adventure defaults ([Sound.Source.MASTER], volume `1`, pitch `1`, and no seed
 * so the receiver's world seed is used at playback). Each slot may be set at most once.
 *
 * @sample io.github.lmliam.kotventure.core.sound.soundSample
 */
public interface SoundScope : SoundSourceScope {
    /**
     * Sets the sound source (category).
     *
     * Prefer the scope-bound vals ([master], [music], …) so no enum import is needed.
     * Defaults to [Sound.Source.MASTER] when unset.
     *
     * @throws IllegalStateException when the source is already set in this block.
     */
    public fun source(source: Sound.Source)

    /**
     * Sets the playback volume.
     *
     * Defaults to `1` when unset. Volume primarily controls the audible distance. Adventure declares a non-negative
     * value but does not validate it. The receiving platform interprets the value during playback.
     *
     * @throws IllegalStateException when the volume is already set in this block.
     */
    public fun volume(volume: Float)

    /**
     * Sets the playback pitch.
     *
     * Defaults to `1` when unset. Adventure does not validate the value. The receiving platform interprets it during
     * playback.
     *
     * @throws IllegalStateException when the pitch is already set in this block.
     */
    public fun pitch(pitch: Float)

    /**
     * Sets the seed used for weighted sound-effect selection.
     *
     * When unset, the receiver's world seed is used at playback.
     *
     * @throws IllegalStateException when the seed is already set in this block.
     */
    public fun seed(seed: Long)
}
