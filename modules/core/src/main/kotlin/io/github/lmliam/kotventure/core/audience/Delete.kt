package io.github.lmliam.kotventure.core.audience

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.chat.SignedMessage

/**
 * Requests deletion of the previously sent [signed] message from this [Audience]'s chat.
 *
 * Adventure silently ignores messages without a signature; this helper fails fast instead, so a
 * deletion that can never happen is surfaced at the call site.
 *
 * @throws IllegalStateException when [signed] carries no signature ([SignedMessage.canDelete] is
 *   `false`), as is the case for system messages.
 * @sample io.github.lmliam.kotventure.core.audience.deleteSample
 */
public fun Audience.delete(signed: SignedMessage) {
    check(signed.canDelete()) { "'signed' has no signature, so it can never be deleted." }
    deleteMessage(signed)
}

/**
 * Requests deletion of the previously sent message with [signature] from this [Audience]'s chat.
 *
 * @sample io.github.lmliam.kotventure.core.audience.deleteBySignatureSample
 */
public fun Audience.delete(signature: SignedMessage.Signature): Unit = deleteMessage(signature)
