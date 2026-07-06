package io.github.lmliam.kotventure.core.nbt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class NbtListTest :
    StringSpec(
        {
            // Helper to reduce repetition
            fun render(block: NbtCompoundScope.() -> Unit): String = nbt(block).string()

            "builds an empty list" {
                val actual = render { "Items" eq list() }
                val expected = """{Items:[]}"""
                actual shouldBe expected
            }

            "builds a list of strings" {
                val actual = render { "pages" eq list("a", "b") }
                val expected = """{pages:["a","b"]}"""
                actual shouldBe expected
            }

            "builds a list of ints (distinct from an int array)" {
                val actual = render { "nums" eq list(1, 2, 3) }
                val expected = """{nums:[1,2,3]}"""
                actual shouldBe expected
            }

            "builds a list of bytes (distinct from a byte array)" {
                val actual = render { "flags" eq list(1.toByte(), 2.toByte()) }
                val expected = """{flags:[1b,2b]}"""
                actual shouldBe expected
            }

            "builds a list of shorts" {
                val actual = render { "slots" eq list(3.toShort(), 4.toShort()) }
                val expected = """{slots:[3s,4s]}"""
                actual shouldBe expected
            }

            "builds a list of longs" {
                val actual = render { "times" eq list(10L, 20L) }
                val expected = """{times:[10L,20L]}"""
                actual shouldBe expected
            }

            "builds a list of floats" {
                val actual = render { "speeds" eq list(1.5f, 2.5f) }
                val expected = """{speeds:[1.5f,2.5f]}"""
                actual shouldBe expected
            }

            "builds a list of doubles" {
                val actual = render { "rates" eq list(0.5, 1.5) }
                val expected = """{rates:[0.5d,1.5d]}"""
                actual shouldBe expected
            }

            "renders booleans as bytes" {
                val actual = render { "checks" eq list(true, false) }
                val expected = """{checks:[1b,0b]}"""
                actual shouldBe expected
            }

            "builds a list of compounds" {
                val actual =
                    render {
                        "Lore" eq
                                list(
                                    { "text" eq "Line 1" },
                                    { "text" eq "Line 2" },
                                )
                    }
                val expected = """{Lore:[{text:"Line 1"},{text:"Line 2"}]}"""
                actual shouldBe expected
            }

            "builds a list of arrays" {
                val actual = render { "rows" eq list(intArrayOf(1, 2), intArrayOf(3, 4)) }
                val expected = """{rows:[[I;1,2],[I;3,4]]}"""
                actual shouldBe expected
            }

            "builds a list of lists" {
                val actual = render { "grid" eq list(list(1, 2), list(3, 4)) }
                val expected = """{grid:[[1,2],[3,4]]}"""
                actual shouldBe expected
            }

            "nests a list inside a compound element" {
                val actual =
                    render {
                        "outer" eq list({ "inner" eq list("x", "y") })
                    }
                val expected = """{outer:[{inner:["x","y"]}]}"""
                actual shouldBe expected
            }

            "rejects an unsupported element type at runtime" {
                val e =
                    shouldThrow<IllegalArgumentException> {
                        render { "chars" eq list('a', 'b') }
                    }
                // Assert the message mentions the NBT element contract (protects future regressions)
                e.message.shouldContain("NBT list elements must be")
            }
        },
    )
