package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.chat.SignedMessage

internal fun chatSample() {
    val audience = emptyAudience()

    audience.chat {
        name { text("Steve") { color(aqua) } }
        target { text("Alex") }
        type(msgCommandOutgoing)
        content { text("hi there") }
    }
}

internal fun signedChatSample() {
    val audience = emptyAudience()
    val signed = signedMessageFromPlatform()

    audience.chat(signed) {
        name { text("Steve") }
    }
}

internal fun deleteSample() {
    val audience = emptyAudience()
    val signed = signedMessageFromPlatform()

    if (signed.canDelete()) {
        audience.delete(signed)
    }
}

internal fun deleteBySignatureSample() {
    val audience = emptyAudience()

    audience.delete(signature(byteArrayOf(1, 2, 3)))
}

internal fun systemMessageSample() {
    val announcement =
        systemMessage("Server restarting soon") {
            text("Server restarting ") { color(gold) }
            text("soon")
        }

    emptyAudience().chat(announcement) {
        name { text("Server") }
    }
}

private fun signedMessageFromPlatform(): SignedMessage = systemMessage("hi there")
