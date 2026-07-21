package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.chat.SignedMessage

/**
 * Creates a server-authored system [SignedMessage] from plain [message].
 *
 * When [init] is present, it creates the unsigned component content that clients display. The function constructs a
 * value and does not send it to an audience.
 *
 * System messages do not have a player signature. Thus, [delete] cannot delete them. Get genuine player-signed messages
 * from a platform source, such as Paper's chat event.
 *
 * @sample io.github.lmliam.kotventure.core.audience.systemMessageSample
 * @throws IllegalStateException when [init] assigns a write-once component slot more than once.
 */
public fun systemMessage(
    message: String,
    init: (ComponentScope.() -> Unit)? = null,
): SignedMessage = SignedMessage.system(message, init?.let { component(it) })
