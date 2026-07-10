package io.github.lmliam.kotventure.core.sound

import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

internal class PlaySoundBuilder(
    private val sound: SoundBuilder,
) : PlaySoundScope,
    SoundScope by sound {
    internal constructor(name: Key) : this(SoundBuilder(name))

    private var playback: (Audience.(Sound) -> Unit)? by
    once { "a playback context ('emitter' or 'at') is already set." }

    override fun emitter(emitter: Sound.Emitter) {
        playback = { sound -> playSound(sound, emitter) }
    }

    override fun at(
        x: Double,
        y: Double,
        z: Double,
    ) {
        playback = { sound -> playSound(sound, x, y, z) }
    }

    internal fun playOn(audience: Audience): Sound {
        val built = sound.build()
        (playback ?: Audience::playSound)(audience, built)
        return built
    }
}
