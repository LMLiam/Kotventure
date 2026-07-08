package io.github.lmliam.kotventure.core.audience

import net.kyori.adventure.chat.SignedMessage

/**
 * Wraps raw signature [bytes] as a [SignedMessage.Signature], for [delete]-by-signature calls when
 * only the stored bytes of a signed message survive.
 *
 * @sample io.github.lmliam.kotventure.core.audience.deleteBySignatureSample
 */
public fun signature(bytes: ByteArray): SignedMessage.Signature = SignedMessage.signature(bytes)
