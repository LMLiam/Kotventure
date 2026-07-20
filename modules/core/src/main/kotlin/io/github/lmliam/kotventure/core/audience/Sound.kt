package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.sound.PlaySoundBuilder
import io.github.lmliam.kotventure.core.sound.PlaySoundScope
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

/**
 * Builds a [Sound] from [name] and optional [init], plays it on this [Audience] immediately, and
 * returns it for later [Audience.stopSound] (Adventure's native `stopSound(Sound)`).
 *
 * Inside [init], set sound slots ([volume][PlaySoundScope.volume], [pitch][PlaySoundScope.pitch],
 * …) and at most one playback context: [emitter][PlaySoundScope.emitter] (e.g. `emitter(self)`)
 * or [at][PlaySoundScope.at]. When neither is set, the sound plays at the recipient's location.
 *
 * Works for a player, the console, or a forwarding audience. An audience without a sound surface ignores it. For a
 * reusable sound that you play more than one time, use
 * [sound][io.github.lmliam.kotventure.core.sound.sound] then [play].
 *
 * @throws IllegalStateException when any sound or playback slot is set twice, or both playback
 *   slots are set.
 * @sample io.github.lmliam.kotventure.core.audience.audienceSoundSample
 */
public fun Audience.sound(
    name: Key,
    init: PlaySoundScope.() -> Unit = {},
): Sound = PlaySoundBuilder(name).apply(init).playOn(this)

/**
 * Plays [sound] on this [Audience] at the recipient's location.
 *
 * Type-overloaded [play] so sound, and future playable types, can share the verb — same idea as
 * [show] and [open]. For build-and-play in one expression, prefer [Audience.sound].
 *
 * @sample io.github.lmliam.kotventure.core.audience.audiencePlaySoundSample
 */
public fun Audience.play(sound: Sound): Unit = playSound(sound)

/**
 * Plays [sound] on this [Audience] from [emitter].
 *
 * Type-overloaded [play] so sound, and future playable types, can share the verb — same idea as
 * [show] and [open]. For build-and-play in one expression, prefer [Audience.sound].
 *
 * @sample io.github.lmliam.kotventure.core.audience.audiencePlaySoundSample
 */
public fun Audience.play(
    sound: Sound,
    emitter: Sound.Emitter,
): Unit = playSound(sound, emitter)

/**
 * Plays [sound] on this [Audience] at world position ([x], [y], [z]).
 *
 * Type-overloaded [play] so sound, and future playable types, can share the verb — same idea as
 * [show] and [open]. For build-and-play in one expression, prefer [Audience.sound].
 *
 * @sample io.github.lmliam.kotventure.core.audience.audiencePlaySoundSample
 */
public fun Audience.play(
    sound: Sound,
    x: Double,
    y: Double,
    z: Double,
): Unit = playSound(sound, x, y, z)
