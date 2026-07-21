package io.github.lmliam.kotventure.coroutines.event

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

internal class RecordingAudience : Audience {
    val messages: MutableList<Component> = mutableListOf()

    override fun sendMessage(message: Component) {
        messages += message
    }
}
