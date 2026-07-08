package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.chat.SignedMessage

internal fun chatSample() {
    val audience = Audience.empty()

    audience.chat {
        name { text("Steve") { color(aqua) } }
        target { text("Alex") }
        type(ChatType.MSG_COMMAND_OUTGOING)
        content { text("hi there") }
    }
}

internal fun signedChatSample() {
    val audience = Audience.empty()
    val signed = signedMessageFromPlatform()

    audience.chat(signed) {
        name { text("Steve") }
    }
}

internal fun deleteSample() {
    val audience = Audience.empty()
    val signed = signedMessageFromPlatform()

    if (signed.canDelete()) {
        audience.delete(signed)
    }
}

internal fun deleteBySignatureSample() {
    val audience = Audience.empty()
    val signature = SignedMessage.signature(byteArrayOf(1, 2, 3))

    audience.delete(signature)
}

internal fun systemMessageSample() {
    val announcement =
        systemMessage("Server restarting soon") {
            text("Server restarting ") { color(gold) }
            text("soon")
        }

    Audience.empty().chat(announcement) {
        name { text("Server") }
    }
}

/** Stand-in for a platform source such as Paper's chat event or signed-message command argument. */
private fun signedMessageFromPlatform(): SignedMessage = systemMessage("hi there")
