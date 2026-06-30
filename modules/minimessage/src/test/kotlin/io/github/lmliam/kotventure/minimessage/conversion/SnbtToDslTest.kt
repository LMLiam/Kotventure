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

            "parenthesises a negative byte so it keeps the Byte type" {
                snbtToDslExpression("{val:-5b}") shouldBe
                        "nbt { \"val\" eq (-5).toByte() }"
            }

            "renders the minimum byte" {
                snbtToDslExpression("{val:-128b}") shouldBe
                        "nbt { \"val\" eq (-128).toByte() }"
            }

            "parses a single short entry" {
                snbtToDslExpression("{slot:3s}") shouldBe
                        "nbt { \"slot\" eq 3.toShort() }"
            }

            "parenthesises a negative short so it keeps the Short type" {
                snbtToDslExpression("{slot:-3s}") shouldBe
                        "nbt { \"slot\" eq (-3).toShort() }"
            }

            "parses a single int entry" {
                snbtToDslExpression("{count:64}") shouldBe
                        "nbt { \"count\" eq 64 }"
            }

            "renders the minimum int as a constant" {
                snbtToDslExpression("{count:-2147483648}") shouldBe
                        "nbt { \"count\" eq Int.MIN_VALUE }"
            }

            "parses a single long entry" {
                snbtToDslExpression("{time:1000L}") shouldBe
                        "nbt { \"time\" eq 1000L }"
            }

            "renders the minimum long as a constant" {
                snbtToDslExpression("{time:-9223372036854775808L}") shouldBe
                        "nbt { \"time\" eq Long.MIN_VALUE }"
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

            "emits compound keys in alphabetical order" {
                snbtToDslExpression("{id:\"minecraft:diamond\",Count:64b}") shouldBe
                        "nbt { \"Count\" eq 64.toByte(); \"id\" eq \"minecraft:diamond\" }"
            }

            "parses a byte array" {
                snbtToDslExpression("{Data:[B;1b,2b,3b]}") shouldBe
                        "nbt { \"Data\" eq byteArrayOf(1, 2, 3) }"
            }

            "parses an int array" {
                snbtToDslExpression("{UUID:[I;1,2,3,4]}") shouldBe
                        "nbt { \"UUID\" eq intArrayOf(1, 2, 3, 4) }"
            }

            "renders the minimum int inside an int array as a constant" {
                snbtToDslExpression("{UUID:[I;-2147483648]}") shouldBe
                        "nbt { \"UUID\" eq intArrayOf(Int.MIN_VALUE) }"
            }

            "parses a long array" {
                snbtToDslExpression("{Times:[L;10L,20L]}") shouldBe
                        "nbt { \"Times\" eq longArrayOf(10L, 20L) }"
            }

            "renders the minimum long inside a long array as a constant" {
                snbtToDslExpression("{Times:[L;-9223372036854775808L]}") shouldBe
                        "nbt { \"Times\" eq longArrayOf(Long.MIN_VALUE) }"
            }

            "parses a list of strings" {
                snbtToDslExpression("{pages:[\"a\",\"b\"]}") shouldBe
                        "nbt { \"pages\" eq list(\"a\", \"b\") }"
            }

            "parses a list of ints" {
                snbtToDslExpression("{nums:[1,2,3]}") shouldBe
                        "nbt { \"nums\" eq list(1, 2, 3) }"
            }

            "parses a list of bytes" {
                snbtToDslExpression("{flags:[1b,2b]}") shouldBe
                        "nbt { \"flags\" eq list(1.toByte(), 2.toByte()) }"
            }

            "parses a list of shorts" {
                snbtToDslExpression("{slots:[3s,4s]}") shouldBe
                        "nbt { \"slots\" eq list(3.toShort(), 4.toShort()) }"
            }

            "parses a list of longs" {
                snbtToDslExpression("{times:[10L,20L]}") shouldBe
                        "nbt { \"times\" eq list(10L, 20L) }"
            }

            "parses a list of floats" {
                snbtToDslExpression("{speeds:[1.5f,2.5f]}") shouldBe
                        "nbt { \"speeds\" eq list(1.5f, 2.5f) }"
            }

            "parses a list of doubles" {
                snbtToDslExpression("{rates:[0.5d,1.5d]}") shouldBe
                        "nbt { \"rates\" eq list(0.5, 1.5) }"
            }

            "parses a list of compounds" {
                snbtToDslExpression("{Lore:[{text:\"L1\"},{text:\"L2\"}]}") shouldBe
                        "nbt { \"Lore\" eq list({ \"text\" eq \"L1\" }, { \"text\" eq \"L2\" }) }"
            }

            "parses a list of arrays" {
                snbtToDslExpression("{rows:[[I;1,2],[I;3,4]]}") shouldBe
                        "nbt { \"rows\" eq list(intArrayOf(1, 2), intArrayOf(3, 4)) }"
            }

            "parses a list of lists" {
                snbtToDslExpression("{grid:[[1,2],[3,4]]}") shouldBe
                        "nbt { \"grid\" eq list(list(1, 2), list(3, 4)) }"
            }

            "parses a list nested in a compound" {
                snbtToDslExpression("{tag:{lore:[\"a\",\"b\"]}}") shouldBe
                        "nbt { \"tag\" eq { \"lore\" eq list(\"a\", \"b\") } }"
            }

            "returns null for an empty list" {
                snbtToDslExpression("{items:[]}").shouldBeNull()
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
                snbtToDslExpression("{}") shouldBe "nbt { }"
            }

            "parses deeply nested compounds" {
                snbtToDslExpression("{a:{b:{c:1}}}") shouldBe
                        "nbt { \"a\" eq { \"b\" eq { \"c\" eq 1 } } }"
            }

            "sorts keys of a nested compound too" {
                snbtToDslExpression("{outer:{b:1,a:2}}") shouldBe
                        "nbt { \"outer\" eq { \"a\" eq 2; \"b\" eq 1 } }"
            }
        },
    )
