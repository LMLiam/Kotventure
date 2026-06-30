package io.github.lmliam.kotventure.core.nbt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class NbtListTest :
    StringSpec(
        {
            "builds a list of strings" {
                nbt { "pages" eq list("a", "b") }.string() shouldBe "{pages:[\"a\",\"b\"]}"
            }

            "builds a list of ints (distinct from an int array)" {
                nbt { "nums" eq list(1, 2, 3) }.string() shouldBe "{nums:[1,2,3]}"
            }

            "builds a list of bytes (distinct from a byte array)" {
                nbt { "flags" eq list(1.toByte(), 2.toByte()) }.string() shouldBe "{flags:[1b,2b]}"
            }

            "builds a list of shorts" {
                nbt { "slots" eq list(3.toShort(), 4.toShort()) }.string() shouldBe "{slots:[3s,4s]}"
            }

            "builds a list of longs" {
                nbt { "times" eq list(10L, 20L) }.string() shouldBe "{times:[10L,20L]}"
            }

            "builds a list of floats" {
                nbt { "speeds" eq list(1.5f, 2.5f) }.string() shouldBe "{speeds:[1.5f,2.5f]}"
            }

            "builds a list of doubles" {
                nbt { "rates" eq list(0.5, 1.5) }.string() shouldBe "{rates:[0.5d,1.5d]}"
            }

            "renders booleans as bytes" {
                nbt { "checks" eq list(true, false) }.string() shouldBe "{checks:[1b,0b]}"
            }

            "builds a list of compounds" {
                nbt {
                    "Lore" eq
                            list(
                                { "text" eq "Line 1" },
                                { "text" eq "Line 2" },
                            )
                }.string() shouldBe "{Lore:[{text:\"Line 1\"},{text:\"Line 2\"}]}"
            }

            "builds a list of arrays" {
                nbt { "rows" eq list(intArrayOf(1, 2), intArrayOf(3, 4)) }.string() shouldBe
                        "{rows:[[I;1,2],[I;3,4]]}"
            }

            "builds a list of lists" {
                nbt { "grid" eq list(list(1, 2), list(3, 4)) }.string() shouldBe
                        "{grid:[[1,2],[3,4]]}"
            }

            "nests a list inside a compound element" {
                nbt {
                    "outer" eq list({ "inner" eq list("x", "y") })
                }.string() shouldBe "{outer:[{inner:[\"x\",\"y\"]}]}"
            }

            // An inferred mixed list (list("a", 1)) is rejected at compile time: the reified T is
            // inferred to an intersection of the element types, which is a warning we treat as an error.
            // Unsupported element types are caught at runtime.
            "rejects an unsupported element type at runtime" {
                shouldThrow<IllegalArgumentException> {
                    nbt { "chars" eq list('a', 'b') }
                }
            }
        },
    )
