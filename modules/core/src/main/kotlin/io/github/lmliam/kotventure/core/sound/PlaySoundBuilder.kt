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

    private var emitter: Sound.Emitter? by once()
    private var position: SoundPosition? by once { "'at' is already set." }

    override fun emitter(emitter: Sound.Emitter) {
        check(position == null) { "'emitter' cannot be set when 'at' is already set." }
        this.emitter = emitter
    }

    override fun at(
        x: Double,
        y: Double,
        z: Double,
    ) {
        check(emitter == null) { "'at' cannot be set when 'emitter' is already set." }
        position = SoundPosition(x, y, z)
    }

    internal fun playOn(audience: Audience): Sound {
        val built = sound.build()
        val emitter = emitter
        val position = position
        when {
            emitter != null -> audience.playSound(built, emitter)
            position != null -> audience.playSound(built, position.x, position.y, position.z)
            else -> audience.playSound(built)
        }
        return built
    }
}
