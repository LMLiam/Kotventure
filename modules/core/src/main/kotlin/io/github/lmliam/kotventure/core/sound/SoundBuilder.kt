package io.github.lmliam.kotventure.core.sound

import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

internal class SoundBuilder(
    private val name: Key,
) : SoundScope {
    private var source: Sound.Source? by once()
    private var volume: Float? by once()
    private var pitch: Float? by once()
    private var seed: Long? by once()

    override fun source(source: Sound.Source) {
        this.source = source
    }

    override fun volume(volume: Float) {
        this.volume = volume
    }

    override fun pitch(pitch: Float) {
        this.pitch = pitch
    }

    override fun seed(seed: Long) {
        this.seed = seed
    }

    internal fun build(): Sound =
        Sound.sound {
            it.type(name)
            source?.let(it::source)
            volume?.let(it::volume)
            pitch?.let(it::pitch)
            seed?.let(it::seed)
        }
}
