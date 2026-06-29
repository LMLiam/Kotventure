package io.github.lmliam.kotventure.serializer

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text

internal fun toMiniMessageSample() {
    val component = text("Welcome") { color(gold) }
    val markup = component.toMiniMessage()
}
