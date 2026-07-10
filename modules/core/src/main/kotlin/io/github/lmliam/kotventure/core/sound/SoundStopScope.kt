package io.github.lmliam.kotventure.core.sound

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop

/**
 * Configures a [SoundStop] describing which in-flight client sounds to halt.
 *
 * Exactly one of the four Adventure factory shapes must result: [all], [named] alone, [source]
 * alone, or [named] plus [source]. An empty block is rejected so stopping everything is never a
 * silent default.
 *
 * Prefer the scope-bound source vals ([music], [ui], …) with [source] so no enum import is needed.
 */
@KotventureDslMarker
public interface SoundStopScope : SoundSourceScope {
    /**
     * Stops every sound ([SoundStop.all]).
     *
     * Mutually exclusive with [named] and [source].
     *
     * @throws IllegalStateException when [all] is already set, or [named]/[source] is set.
     */
    public fun all()

    /**
     * Stops sounds matching [name] ([SoundStop.named], or [SoundStop.namedOnSource] when [source]
     * is also set).
     *
     * Mutually exclusive with [all].
     *
     * @throws IllegalStateException when [named] is already set, or [all] is set.
     */
    public fun named(name: Key)

    /**
     * Stops sounds on [source] ([SoundStop.source], or [SoundStop.namedOnSource] when [named] is
     * also set).
     *
     * Prefer the scope-bound vals ([master], [music], …). Mutually exclusive with [all].
     *
     * @throws IllegalStateException when [source] is already set, or [all] is set.
     */
    public fun source(source: Sound.Source)
}
