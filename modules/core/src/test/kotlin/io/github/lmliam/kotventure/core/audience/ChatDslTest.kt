package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

private class RecordingChatAudience : Audience {
    val componentSends = mutableListOf<Pair<Component, ChatType.Bound>>()
    val signedSends = mutableListOf<Pair<SignedMessage, ChatType.Bound>>()

    override fun sendMessage(
        message: Component,
        boundChatType: ChatType.Bound,
    ) {
        componentSends += message to boundChatType
    }

    override fun sendMessage(
        signedMessage: SignedMessage,
        boundChatType: ChatType.Bound,
    ) {
        signedSends += signedMessage to boundChatType
    }
}

class ChatDslTest :
    StringSpec(
        {
            "sends the content as ordinary player chat by default" {
                val audience = RecordingChatAudience()

                audience.chat {
                    name { text("Steve") { color(aqua) } }
                    content { text("hi there") }
                }

                audience.componentSends shouldHaveSize 1
                val (content, bound) = audience.componentSends.single()
                content shouldContainText "hi there"
                bound.type().key() shouldBe Key.key("chat")
                bound.name().childAt(0) shouldContainText "Steve"
                bound.name().childAt(0) shouldHaveColor aqua
                bound.target().shouldBeNull()
            }

            "binds an explicit chat type and target" {
                val audience = RecordingChatAudience()

                audience.chat {
                    type(msgCommandOutgoing)
                    name { text("Steve") }
                    target { text("Alex") }
                    content { text("psst") }
                }

                val (_, bound) = audience.componentSends.single()
                bound.type().key() shouldBe Key.key("msg_command_outgoing")
                bound.target().shouldNotBeNull() shouldContainText "Alex"
            }

            "accepts existing components for name, target, and content" {
                val audience = RecordingChatAudience()
                val name = Component.text("Steve")
                val target = Component.text("Alex")
                val content = Component.text("hi there")

                audience.chat {
                    name(name)
                    target(target)
                    content(content)
                }

                val (sent, bound) = audience.componentSends.single()
                sent shouldBe content
                bound.name() shouldBe name
                bound.target() shouldBe target
            }

            "sends a signed message with the built bound chat type" {
                val audience = RecordingChatAudience()
                val signed = systemMessage("hi there")

                audience.chat(signed) {
                    name { text("Steve") }
                }

                audience.signedSends shouldHaveSize 1
                val (sent, bound) = audience.signedSends.single()
                sent shouldBeSameInstanceAs signed
                bound.type().key() shouldBe Key.key("chat")
                bound.name() shouldContainText "Steve"
            }

            "sends the same chat to every member of a forwarding audience" {
                val first = RecordingChatAudience()
                val second = RecordingChatAudience()

                audienceOf(first, second).chat {
                    name { text("Steve") }
                    content { text("broadcast") }
                }

                first.componentSends shouldHaveSize 1
                second.componentSends shouldHaveSize 1
                first.componentSends.single() shouldBe second.componentSends.single()
            }

            "rejects a block without a name" {
                shouldThrow<IllegalStateException> {
                    RecordingChatAudience().chat {
                        content { text("hi") }
                    }
                }
            }

            "rejects a block without content" {
                shouldThrow<IllegalStateException> {
                    RecordingChatAudience().chat {
                        name { text("Steve") }
                    }
                }
            }

            "rejects a signed block without a name" {
                shouldThrow<IllegalStateException> {
                    RecordingChatAudience().chat(systemMessage("hi")) {}
                }
            }

            "rejects a duplicate name" {
                shouldThrow<IllegalStateException> {
                    RecordingChatAudience().chat {
                        name { text("Steve") }
                        name { text("Alex") }
                    }
                }
            }

            "rejects a duplicate target" {
                shouldThrow<IllegalStateException> {
                    RecordingChatAudience().chat {
                        target { text("Alex") }
                        target { text("Steve") }
                    }
                }
            }

            "rejects a duplicate type" {
                shouldThrow<IllegalStateException> {
                    RecordingChatAudience().chat {
                        type(chat)
                        type(sayCommand)
                    }
                }
            }

            "rejects duplicate content" {
                shouldThrow<IllegalStateException> {
                    RecordingChatAudience().chat {
                        content { text("hi") }
                        content { text("there") }
                    }
                }
            }
        },
    )
