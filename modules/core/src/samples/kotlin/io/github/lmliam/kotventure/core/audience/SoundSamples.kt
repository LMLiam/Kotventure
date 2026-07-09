package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.sound.sound

internal fun audienceSoundSample() {
    val audience = emptyAudience()

    audience.sound(key("minecraft:entity.pig.ambient")) {
        volume(2f)
        pitch(0.5f)
        emitter(self)
    }
}

internal fun audiencePlaySoundSample() {
    val audience = emptyAudience()
    val alert =
        sound(key("minecraft:block.bell.use")) {
            source(music)
        }

    audience.play(alert)
}

internal fun audienceStopSoundSample() {
    val audience = emptyAudience()

    audience.stopSound { source(music) }
    audience.stopSound { all() }
}
