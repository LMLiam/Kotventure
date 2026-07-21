package io.github.lmliam.kotventure.core.sound

import net.kyori.adventure.sound.Sound

/**
 * Configures a [Sound] and its one-shot playback context on an audience.
 *
 * Extends [SoundScope] with mutually exclusive emitter-relative ([emitter]) and world-position
 * ([at]) playback slots. When neither is set, the sound plays at the recipient's location via
 * Adventure's plain [Audience.playSound][net.kyori.adventure.audience.Audience.playSound] overload.
 */
public interface PlaySoundScope : SoundScope {
    /**
     * Plays the sound relative to [emitter] (for example [self] to follow the recipient).
     *
     * This setting is mutually exclusive with [at]. Adventure's [Audience.playSound][net.kyori.adventure.audience.Audience.playSound]
     * overloads cannot express both.
     *
     * @throws IllegalStateException when a playback context (either 'emitter' or 'at') is already
     *   set in this block.
     */
    public fun emitter(emitter: Sound.Emitter)

    /**
     * An emitter representing the recipient of the sound ([Sound.Emitter.self]).
     *
     * Scope-bound so [emitter]`(self)` needs no extra import.
     */
    public val self: Sound.Emitter
        get() = Sound.Emitter.self()

    /**
     * Plays the sound at world position ([x], [y], [z]).
     *
     * This setting is mutually exclusive with [emitter]. Adventure's
     * [Audience.playSound][net.kyori.adventure.audience.Audience.playSound] overloads cannot express both.
     *
     * @throws IllegalStateException when a playback context (either 'emitter' or 'at') is already
     *   set in this block.
     */
    public fun at(
        x: Double,
        y: Double,
        z: Double,
    )
}
