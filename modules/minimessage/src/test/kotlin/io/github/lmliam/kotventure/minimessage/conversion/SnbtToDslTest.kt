package io.github.lmliam.kotventure.minimessage.conversion

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class SnbtToDslTest :
    StringSpec(
        {
            "parses a single byte entry" {
                snbtToDslExpression("{kotventure:1b}") shouldBe
                    "nbt { \"kotventure\" eq 1.toByte() }"
            }

            "parses a negative byte" {
                snbtToDslExpression("{val:-5b}") shouldBe
                    "nbt { \"val\" eq -5.toByte() }"
            }

            "parses a single short entry" {
                snbtToDslExpression("{slot:3s}") shouldBe
                    "nbt { \"slot\" eq 3.toShort() }"
            }

            "parses a single int entry" {
                snbtToDslExpression("{count:64}") shouldBe
                    "nbt { \"count\" eq 64 }"
            }

            "parses a single long entry" {
                snbtToDslExpression("{time:1000L}") shouldBe
                    "nbt { \"time\" eq 1000L }"
            }

            "parses a single float entry" {
                snbtToDslExpression("{speed:1.5f}") shouldBe
                    "nbt { \"speed\" eq 1.5f }"
            }

            "parses a single double entry with suffix" {
                snbtToDslExpression("{health:20.0d}") shouldBe
                    "nbt { \"health\" eq 20.0 }"
            }

            "parses a bare decimal as double" {
                snbtToDslExpression("{rate:0.5}") shouldBe
                    "nbt { \"rate\" eq 0.5 }"
            }

            "parses a string value" {
                snbtToDslExpression("{id:\"minecraft:diamond\"}") shouldBe
                    "nbt { \"id\" eq \"minecraft:diamond\" }"
            }

            "parses a nested compound" {
                snbtToDslExpression("{display:{Name:\"Sword\"}}") shouldBe
                    "nbt { \"display\" eq { \"Name\" eq \"Sword\" } }"
            }

            "parses multi-entry compounds" {
                snbtToDslExpression("{id:\"minecraft:diamond\",Count:64b}") shouldBe
                    "nbt { \"id\" eq \"minecraft:diamond\"; \"Count\" eq 64.toByte() }"
            }

            "parses a byte array" {
                snbtToDslExpression("{Data:[B;1b,2b,3b]}") shouldBe
                    "nbt { \"Data\" eq byteArrayOf(1, 2, 3) }"
            }

            "parses an int array" {
                snbtToDslExpression("{UUID:[I;1,2,3,4]}") shouldBe
                    "nbt { \"UUID\" eq intArrayOf(1, 2, 3, 4) }"
            }

            "parses a long array" {
                snbtToDslExpression("{Times:[L;10L,20L]}") shouldBe
                    "nbt { \"Times\" eq longArrayOf(10L, 20L) }"
            }

            "returns null for generic lists" {
                snbtToDslExpression("{items:[1,2,3]}").shouldBeNull()
            }

            "parses quoted keys" {
                snbtToDslExpression("{\"foo.bar\":1b}") shouldBe
                    "nbt { \"foo.bar\" eq 1.toByte() }"
            }

            "parses escaped strings" {
                snbtToDslExpression("{msg:\"say \\\"hello\\\"\"}") shouldBe
                    "nbt { \"msg\" eq \"say \\\"hello\\\"\" }"
            }

            "returns null for trailing garbage" {
                snbtToDslExpression("{ok:1b}garbage").shouldBeNull()
            }

            "returns null for malformed input" {
                snbtToDslExpression("not-snbt").shouldBeNull()
            }

            "parses an empty compound" {
                snbtToDslExpression("{}") shouldBe "nbt {  }"
            }

            "parses deeply nested compounds" {
                snbtToDslExpression("{a:{b:{c:1}}}") shouldBe
                    "nbt { \"a\" eq { \"b\" eq { \"c\" eq 1 } } }"
            }
        },
    )
