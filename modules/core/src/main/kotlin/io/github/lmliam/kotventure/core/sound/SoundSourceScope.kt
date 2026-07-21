package io.github.lmliam.kotventure.core.sound

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.sound.Sound

/**
 * Provides scope-bound [Sound.Source] values for sound blocks.
 *
 * These values exist only inside this block. [KotventureDslMarker] prevents nested scopes from seeing them. Use these
 * values instead of importing the Adventure enum to keep call sites self-contained.
 */
@KotventureDslMarker
public interface SoundSourceScope {
    /** [Sound.Source.MASTER]. */
    public val master: Sound.Source
        get() = Sound.Source.MASTER

    /** [Sound.Source.MUSIC]. */
    public val music: Sound.Source
        get() = Sound.Source.MUSIC

    /** [Sound.Source.RECORD]. */
    public val record: Sound.Source
        get() = Sound.Source.RECORD

    /** [Sound.Source.WEATHER]. */
    public val weather: Sound.Source
        get() = Sound.Source.WEATHER

    /** [Sound.Source.BLOCK]. */
    public val block: Sound.Source
        get() = Sound.Source.BLOCK

    /** [Sound.Source.HOSTILE]. */
    public val hostile: Sound.Source
        get() = Sound.Source.HOSTILE

    /** [Sound.Source.NEUTRAL]. */
    public val neutral: Sound.Source
        get() = Sound.Source.NEUTRAL

    /** [Sound.Source.PLAYER]. */
    public val player: Sound.Source
        get() = Sound.Source.PLAYER

    /** [Sound.Source.AMBIENT]. */
    public val ambient: Sound.Source
        get() = Sound.Source.AMBIENT

    /** [Sound.Source.VOICE]. */
    public val voice: Sound.Source
        get() = Sound.Source.VOICE

    /** [Sound.Source.UI]. */
    public val ui: Sound.Source
        get() = Sound.Source.UI
}
