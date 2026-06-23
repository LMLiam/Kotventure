package io.github.lmliam.kotventure.core.key

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

class KeyDslTest :
    StringSpec(
        {
            "builds a key from a namespace and value" {
                val adventureKey = key("kotventure", "messages")

                adventureKey shouldBe Key.key("kotventure", "messages")
                adventureKey.namespace() shouldBe "kotventure"
                adventureKey.value() shouldBe "messages"
                adventureKey.asString() shouldBe "kotventure:messages"
            }

            "parses a key from a namespaced string" {
                val adventureKey = Key.key("kotventure:messages")

                adventureKey shouldBe Key.key("kotventure", "messages")
                adventureKey.namespace() shouldBe "kotventure"
                adventureKey.value() shouldBe "messages"
            }

            "uses the minecraft namespace when parsing a bare value" {
                val adventureKey = Key.key("stone")

                adventureKey shouldBe Key.key("minecraft", "stone")
                adventureKey.namespace() shouldBe "minecraft"
                adventureKey.value() shouldBe "stone"
            }

            "throws when building a key with an invalid namespace" {
                shouldThrow<InvalidKeyException> {
                    key("not valid", "messages")
                }
            }

            "throws when parsing an invalid key string" {
                shouldThrow<InvalidKeyException> {
                    Key.key("not valid:messages")
                }
            }
        },
    )
