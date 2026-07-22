package io.github.lmliam.kotventure.core.audience

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.types.shouldBeSameInstanceAs
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import java.time.Instant
import java.util.UUID

private class RecordingDeleteAudience : Audience {
    val deletedMessages = mutableListOf<SignedMessage>()
    val deletedSignatures = mutableListOf<SignedMessage.Signature>()

    override fun deleteMessage(signedMessage: SignedMessage) {
        deletedMessages += signedMessage
    }

    override fun deleteMessage(signature: SignedMessage.Signature) {
        deletedSignatures += signature
    }
}

/** A substitute for a player-signed message. Genuine player-signed messages exist only on a platform. */
private class PlayerSignedMessage(
    private val signature: SignedMessage.Signature,
) : SignedMessage {
    override fun timestamp(): Instant = Instant.EPOCH

    override fun salt(): Long = 0

    override fun signature(): SignedMessage.Signature = signature

    override fun unsignedContent(): Component? = null

    override fun message(): String = "player chat"

    override fun identity(): Identity = Identity.identity(UUID(0, 0))
}

class DeleteDslTest :
    StringSpec(
        {
            "deletes a signed message" {
                val audience = RecordingDeleteAudience()
                val signed = PlayerSignedMessage(signature(byteArrayOf(1, 2, 3)))

                audience.delete(signed)

                audience.deletedMessages shouldHaveSize 1
                audience.deletedMessages.single() shouldBeSameInstanceAs signed
            }

            "rejects deleting a message without a signature" {
                val audience = RecordingDeleteAudience()

                shouldThrow<IllegalStateException> {
                    audience.delete(systemMessage("cannot be deleted"))
                }

                audience.deletedMessages.shouldBeEmpty()
                audience.deletedSignatures.shouldBeEmpty()
            }

            "deletes by signature" {
                val audience = RecordingDeleteAudience()
                val signature = signature(byteArrayOf(4, 5, 6))

                audience.delete(signature)

                audience.deletedSignatures shouldHaveSize 1
                audience.deletedSignatures.single() shouldBeSameInstanceAs signature
            }
        },
    )
