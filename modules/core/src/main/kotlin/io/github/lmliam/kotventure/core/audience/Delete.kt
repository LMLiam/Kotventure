package io.github.lmliam.kotventure.core.audience

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.chat.SignedMessage

/**
 * Requests deletion of the previously sent [signed] message from this [Audience]'s chat.
 *
 * Adventure ignores messages without a signature. This function fails immediately so that the call site identifies an
 * impossible deletion.
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
 * Requests deletion of the message identified by [signature] from this [Audience]'s chat.
 *
 * This overload does not verify that the audience previously received a message with this signature.
 *
 * @sample io.github.lmliam.kotventure.core.audience.deleteBySignatureSample
 */
public fun Audience.delete(signature: SignedMessage.Signature): Unit = deleteMessage(signature)
