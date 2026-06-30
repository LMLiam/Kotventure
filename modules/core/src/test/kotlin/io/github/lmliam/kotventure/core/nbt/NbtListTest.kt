package io.github.lmliam.kotventure.core.nbt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class NbtListTest :
    StringSpec(
        {
            "builds a list of strings" {
                nbt { "pages" eq listOf("a", "b") }.string() shouldBe "{pages:[\"a\",\"b\"]}"
            }

            "builds a list of ints (distinct from an int array)" {
                nbt { "nums" eq listOf(1, 2, 3) }.string() shouldBe "{nums:[1,2,3]}"
            }

            "builds a list of bytes (distinct from a byte array)" {
                nbt { "flags" eq listOf<Byte>(1, 2) }.string() shouldBe "{flags:[1b,2b]}"
            }

            "builds a list of shorts" {
                nbt { "slots" eq listOf<Short>(3, 4) }.string() shouldBe "{slots:[3s,4s]}"
            }

            "builds a list of longs" {
                nbt { "times" eq listOf(10L, 20L) }.string() shouldBe "{times:[10L,20L]}"
            }

            "builds a list of floats" {
                nbt { "speeds" eq listOf(1.5f, 2.5f) }.string() shouldBe "{speeds:[1.5f,2.5f]}"
            }

            "builds a list of doubles" {
                nbt { "rates" eq listOf(0.5, 1.5) }.string() shouldBe "{rates:[0.5d,1.5d]}"
            }

            "builds a list of compounds" {
                nbt {
                    "Lore" eq
                        listOf {
                            element { "text" eq "Line 1" }
                            element { "text" eq "Line 2" }
                        }
                }.string() shouldBe "{Lore:[{text:\"Line 1\"},{text:\"Line 2\"}]}"
            }

            "builds an empty list" {
                nbt { "empty" eq listOf<String>() }.string() shouldBe "{empty:[]}"
            }

            "nests a list inside a list element" {
                nbt {
                    "outer" eq
                        listOf {
                            element { "inner" eq listOf("x", "y") }
                        }
                }.string() shouldBe "{outer:[{inner:[\"x\",\"y\"]}]}"
            }

            // An inferred mixed list (listOf("a", 1)) is rejected by the compiler (reified-intersection
            // warning); forcing a common supertype reaches the runtime guard instead.
            "rejects a forced mixed-type scalar list at runtime" {
                shouldThrow<IllegalArgumentException> {
                    nbt { "mixed" eq listOf<Any>("a", 1) }
                }
            }

            "rejects an unsupported scalar element type at runtime" {
                shouldThrow<IllegalArgumentException> {
                    nbt { "bools" eq listOf(true, false) }
                }
            }
        },
    )
