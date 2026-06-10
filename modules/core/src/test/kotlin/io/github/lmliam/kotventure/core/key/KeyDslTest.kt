package io.github.lmliam.kotventure.core.key

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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

            "builds a key with infix namespace syntax" {
                val adventureKey = "kotventure" namespace "messages"

                adventureKey shouldBe key("kotventure", "messages")
                adventureKey.namespace() shouldBe "kotventure"
                adventureKey.value() shouldBe "messages"
            }

            "parses a key from a namespaced string" {
                val adventureKey = "kotventure:messages".asKey()

                adventureKey shouldBe Key.key("kotventure", "messages")
                adventureKey.namespace() shouldBe "kotventure"
                adventureKey.value() shouldBe "messages"
            }

            "uses the minecraft namespace when parsing a bare value" {
                val adventureKey = "stone".asKey()

                adventureKey shouldBe Key.key("minecraft", "stone")
                adventureKey.namespace() shouldBe "minecraft"
                adventureKey.value() shouldBe "stone"
            }
        },
    )
