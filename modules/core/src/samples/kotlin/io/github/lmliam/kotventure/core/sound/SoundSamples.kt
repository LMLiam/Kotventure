package io.github.lmliam.kotventure.core.sound

import io.github.lmliam.kotventure.core.key.key

internal fun soundSample() {
    sound(key("minecraft:entity.experience_orb.pickup")) {
        source(music)
        volume(2f)
        pitch(0.5f)
        seed(42L)
    }
}
