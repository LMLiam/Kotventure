package io.github.lmliam.kotventure.core.audience

import net.kyori.adventure.chat.SignedMessage

/**
 * Wraps raw signature [bytes] for a [delete] call when only the stored signature remains.
 *
 * Adventure retains the array reference. A later change to [bytes] also changes the signature value. Make a defensive
 * copy before this call when the caller can change the array.
 *
 * @sample io.github.lmliam.kotventure.core.audience.deleteBySignatureSample
 */
public fun signature(bytes: ByteArray): SignedMessage.Signature = SignedMessage.signature(bytes)
