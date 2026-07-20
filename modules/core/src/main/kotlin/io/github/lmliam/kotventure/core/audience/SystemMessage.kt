package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.chat.SignedMessage

/**
 * Creates a server-authored system [SignedMessage] from the plain [message] string, with unsigned
 * component content built from [init] when given.
 *
 * System messages do not have a player signature. Thus, [delete] cannot delete them. Get genuine player-signed messages
 * from a platform source, such as Paper's chat event.
 *
 * @sample io.github.lmliam.kotventure.core.audience.systemMessageSample
 */
public fun systemMessage(
    message: String,
    init: (ComponentScope.() -> Unit)? = null,
): SignedMessage = SignedMessage.system(message, init?.let { component(it) })
