package io.github.lmliam.kotventure.serializer.readme

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.serializer.asLegacyAmpersandComponent
import io.github.lmliam.kotventure.serializer.toJson
import io.github.lmliam.kotventure.serializer.toMiniMessage
import io.github.lmliam.kotventure.serializer.toPlainText

internal fun readmeSerializerTourSample() {
    val message = text("Welcome") { color(gold) }

    val json = message.toJson()
    val markup = message.toMiniMessage()
    val plain = message.toPlainText()

    val imported = "&6Welcome".asLegacyAmpersandComponent()
}
