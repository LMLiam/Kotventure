package io.github.lmliam.kotventure.minimessage.conversion

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class SnbtToDslTest :
    StringSpec(
        {
            "parses a single byte entry" {
                snbtToDslBody("{kotventure:1b}") shouldBe
                        "\"kotventure\" eq 1.toByte()"
            }

            "parenthesises a negative byte so it keeps the Byte type" {
                snbtToDslBody("{val:-5b}") shouldBe
                        "\"val\" eq (-5).toByte()"
            }

            "renders the minimum byte" {
                snbtToDslBody("{val:-128b}") shouldBe
                        "\"val\" eq (-128).toByte()"
            }

            "parses a single short entry" {
                snbtToDslBody("{slot:3s}") shouldBe
                        "\"slot\" eq 3.toShort()"
            }

            "parenthesises a negative short so it keeps the Short type" {
                snbtToDslBody("{slot:-3s}") shouldBe
                        "\"slot\" eq (-3).toShort()"
            }

            "parses a single int entry" {
                snbtToDslBody("{count:64}") shouldBe
                        "\"count\" eq 64"
            }

            "renders the minimum int as a constant" {
                snbtToDslBody("{count:-2147483648}") shouldBe
                        "\"count\" eq Int.MIN_VALUE"
            }

            "parses a single long entry" {
                snbtToDslBody("{time:1000L}") shouldBe
                        "\"time\" eq 1000L"
            }

            "renders the minimum long as a constant" {
                snbtToDslBody("{time:-9223372036854775808L}") shouldBe
                        "\"time\" eq Long.MIN_VALUE"
            }

            "parses a single float entry" {
                snbtToDslBody("{speed:1.5f}") shouldBe
                        "\"speed\" eq 1.5f"
            }

            "parses a single double entry with suffix" {
                snbtToDslBody("{health:20.0d}") shouldBe
                        "\"health\" eq 20.0"
            }

            "parses a bare decimal as double" {
                snbtToDslBody("{rate:0.5}") shouldBe
                        "\"rate\" eq 0.5"
            }

            "parses a string value" {
                snbtToDslBody("{id:\"minecraft:diamond\"}") shouldBe
                        "\"id\" eq \"minecraft:diamond\""
            }

            "parses a nested compound" {
                snbtToDslBody("{display:{Name:\"Sword\"}}") shouldBe
                        "\"display\" eq { \"Name\" eq \"Sword\" }"
            }

            "emits compound keys in alphabetical order" {
                snbtToDslBody("{id:\"minecraft:diamond\",Count:64b}") shouldBe
                        "\"Count\" eq 64.toByte(); \"id\" eq \"minecraft:diamond\""
            }

            "parses a byte array" {
                snbtToDslBody("{Data:[B;1b,2b,3b]}") shouldBe
                        "\"Data\" eq byteArrayOf(1, 2, 3)"
            }

            "parses an int array" {
                snbtToDslBody("{UUID:[I;1,2,3,4]}") shouldBe
                        "\"UUID\" eq intArrayOf(1, 2, 3, 4)"
            }

            "renders the minimum int inside an int array as a constant" {
                snbtToDslBody("{UUID:[I;-2147483648]}") shouldBe
                        "\"UUID\" eq intArrayOf(Int.MIN_VALUE)"
            }

            "parses a long array" {
                snbtToDslBody("{Times:[L;10L,20L]}") shouldBe
                        "\"Times\" eq longArrayOf(10L, 20L)"
            }

            "renders the minimum long inside a long array as a constant" {
                snbtToDslBody("{Times:[L;-9223372036854775808L]}") shouldBe
                        "\"Times\" eq longArrayOf(Long.MIN_VALUE)"
            }

            "parses a list of strings" {
                snbtToDslBody("{pages:[\"a\",\"b\"]}") shouldBe
                        "\"pages\" eq list(\"a\", \"b\")"
            }

            "parses a list of ints" {
                snbtToDslBody("{nums:[1,2,3]}") shouldBe
                        "\"nums\" eq list(1, 2, 3)"
            }

            "parses a list of bytes" {
                snbtToDslBody("{flags:[1b,2b]}") shouldBe
                        "\"flags\" eq list(1.toByte(), 2.toByte())"
            }

            "parses a list of shorts" {
                snbtToDslBody("{slots:[3s,4s]}") shouldBe
                        "\"slots\" eq list(3.toShort(), 4.toShort())"
            }

            "parses a list of longs" {
                snbtToDslBody("{times:[10L,20L]}") shouldBe
                        "\"times\" eq list(10L, 20L)"
            }

            "parses a list of floats" {
                snbtToDslBody("{speeds:[1.5f,2.5f]}") shouldBe
                        "\"speeds\" eq list(1.5f, 2.5f)"
            }

            "parses a list of doubles" {
                snbtToDslBody("{rates:[0.5d,1.5d]}") shouldBe
                        "\"rates\" eq list(0.5, 1.5)"
            }

            "parses a list of compounds" {
                snbtToDslBody("{Lore:[{text:\"L1\"},{text:\"L2\"}]}") shouldBe
                        "\"Lore\" eq list({ \"text\" eq \"L1\" }, { \"text\" eq \"L2\" })"
            }

            "parses a list of arrays" {
                snbtToDslBody("{rows:[[I;1,2],[I;3,4]]}") shouldBe
                        "\"rows\" eq list(intArrayOf(1, 2), intArrayOf(3, 4))"
            }

            "parses a list of lists" {
                snbtToDslBody("{grid:[[1,2],[3,4]]}") shouldBe
                        "\"grid\" eq list(list(1, 2), list(3, 4))"
            }

            "parses a list nested in a compound" {
                snbtToDslBody("{tag:{lore:[\"a\",\"b\"]}}") shouldBe
                        "\"tag\" eq { \"lore\" eq list(\"a\", \"b\") }"
            }

            "returns null for an empty list" {
                snbtToDslBody("{items:[]}").shouldBeNull()
            }

            "parses quoted keys" {
                snbtToDslBody("{\"foo.bar\":1b}") shouldBe
                        "\"foo.bar\" eq 1.toByte()"
            }

            "parses escaped strings" {
                snbtToDslBody("{msg:\"say \\\"hello\\\"\"}") shouldBe
                        "\"msg\" eq \"say \\\"hello\\\"\""
            }

            "returns null for trailing garbage" {
                snbtToDslBody("{ok:1b}garbage").shouldBeNull()
            }

            "returns null for malformed input" {
                snbtToDslBody("not-snbt").shouldBeNull()
            }

            "renders an empty compound as an empty body" {
                snbtToDslBody("{}") shouldBe ""
            }

            "parses deeply nested compounds" {
                snbtToDslBody("{a:{b:{c:1}}}") shouldBe
                        "\"a\" eq { \"b\" eq { \"c\" eq 1 } }"
            }

            "sorts keys of a nested compound too" {
                snbtToDslBody("{outer:{b:1,a:2}}") shouldBe
                        "\"outer\" eq { \"a\" eq 2; \"b\" eq 1 }"
            }
        },
    )
