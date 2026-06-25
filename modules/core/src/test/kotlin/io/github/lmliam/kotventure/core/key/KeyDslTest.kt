package io.github.lmliam.kotventure.core.key

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

class KeyDslTest :
    StringSpec(
        {
            "builds a key from namespace and value" {
                val adventureKey = key("kotventure", "messages")

                adventureKey shouldBe Key.key("kotventure", "messages")
                adventureKey.namespace() shouldBe "kotventure"
                adventureKey.value() shouldBe "messages"
                adventureKey.asString() shouldBe "kotventure:messages"
            }

            "throws when building key with an invalid namespace" {
                shouldThrow<InvalidKeyException> {
                    key("not valid", "messages")
                }
            }

            "throws when building key with an invalid value" {
                shouldThrow<InvalidKeyException> {
                    key("minecraft", "not valid")
                }
            }
        },
    )
