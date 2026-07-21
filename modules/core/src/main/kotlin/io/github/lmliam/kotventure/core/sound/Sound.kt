package io.github.lmliam.kotventure.core.sound

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

/**
 * Creates an Adventure [Sound] from [name] and [init] without playing it.
 *
 * Construction has no audience side effects. Use this when a sound value is shared, stored, or passed to
 * a later playback API.
 *
 * @throws IllegalStateException when any slot is set twice.
 * @sample io.github.lmliam.kotventure.core.sound.soundSample
 */
public fun sound(
    name: Key,
    init: SoundScope.() -> Unit = {},
): Sound = SoundBuilder(name).apply(init).build()
