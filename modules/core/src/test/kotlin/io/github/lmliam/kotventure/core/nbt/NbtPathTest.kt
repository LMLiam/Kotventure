package io.github.lmliam.kotventure.core.nbt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class NbtPathTest :
    StringSpec(
        {
            "builds a single key path" {
                val path = nbtPath("Health")

                path.asString() shouldBe "Health"
            }

            "builds a dotted compound path via indexing" {
                val path = nbtPath("tag")["display"]["Name"]

                path.asString() shouldBe "tag.display.Name"
            }

            "quotes special characters in indexed keys" {
                val path = nbtPath("root")["foo.bar"]["with space"]["[index]"]["say \"hi\""]

                path.asString() shouldBe "root.\"foo.bar\".\"with space\".\"[index]\".\"say \\\"hi\\\"\""
            }

            "uses the root key verbatim as a pre-formed escape hatch" {
                val path = nbtPath("display name")

                path.asString() shouldBe "display name"
            }

            "quotes empty keys instead of emitting a bare segment" {
                val path = nbtPath("root")[""]

                path.asString() shouldBe "root.\"\""
            }

            "builds a path with a list index" {
                val path = nbtPath("Items")[0]["id"]

                path.asString() shouldBe "Items[0].id"
            }

            "builds a path with all-elements selection" {
                val path = nbtPath("Inventory")[all]["id"]

                path.asString() shouldBe "Inventory[].id"
            }

            "builds a complex nested path" {
                val path = nbtPath("front_text")["messages"][0]

                path.asString() shouldBe "front_text.messages[0]"
            }

            "builds a path with a compound filter" {
                val path = nbtPath("Items")[matching { "id" eq "minecraft:diamond" }]["Count"]

                path.asString() shouldBe "Items[{id:\"minecraft:diamond\"}].Count"
            }

            "predicate with a nested compound" {
                val path = nbtPath("Items")[matching { "tag" eq { "Unbreakable" eq 1.toByte() } }]

                path.asString() shouldBe "Items[{tag:{Unbreakable:1b}}]"
            }

            "predicate with typed arrays" {
                val path =
                    nbtPath("Entities")[
                        matching {
                            "UUID" eq intArrayOf(1, 2, 3, 4)
                            "Data" eq byteArrayOf(1, 2)
                            "Times" eq longArrayOf(10L, 20L)
                        },
                    ]

                path.asString() shouldBe "Entities[{UUID:[I;1,2,3,4],Data:[B;1b,2b],Times:[L;10L,20L]}]"
            }

            "predicate copies array values so later caller mutation is ignored" {
                val data = intArrayOf(1, 2, 3)
                val path = nbtPath("Entities")[matching { "UUID" eq data }]
                data[0] = 99

                path.asString() shouldBe "Entities[{UUID:[I;1,2,3]}]"
            }

            "predicate with multiple entries" {
                val path =
                    nbtPath("Items")[
                        matching {
                            "id" eq "minecraft:diamond"
                            "Count" eq 64.toByte()
                        },
                    ]["Slot"]

                path.asString() shouldBe "Items[{id:\"minecraft:diamond\",Count:64b}].Slot"
            }

            "escape hatch wraps a raw string" {
                val path = nbtPath("Items[{id:\"minecraft:diamond\"}].Count")

                path.asString() shouldBe "Items[{id:\"minecraft:diamond\"}].Count"
            }

            "pre-formed dotted string is used verbatim" {
                val path = nbtPath("welcome.title")

                path.asString() shouldBe "welcome.title"
            }

            "toString returns the path string" {
                val path = nbtPath("Health")

                path.toString() shouldBe "Health"
            }

            "integer index factory creates path starting at index" {
                val path = nbtPath(0)["id"]

                path.asString() shouldBe "[0].id"
            }

            "chaining operators on a raw path extends it" {
                val path = nbtPath("Items[0].id")["tag"]

                path.asString() shouldBe "Items[0].id.tag"
            }

            "predicate value with quote and backslash characters is escaped" {
                val path = nbtPath("Items")[matching { "Name" eq "path\\to\\\"file\"" }]

                path.asString() shouldBe "Items[{Name:\"path\\\\to\\\\\\\"file\\\"\"}]"
            }

            "predicate value with control characters is escaped" {
                val path = nbtPath("Data")[matching { "text" eq "line1\nline2\ttab" }]

                path.asString() shouldBe "Data[{text:\"line1\\nline2\\ttab\"}]"
            }

            "rejects duplicate predicate keys" {
                shouldThrow<IllegalStateException> {
                    nbtPath("Items")[
                        matching {
                            "id" eq "minecraft:stone"
                            "id" eq "minecraft:diamond"
                        },
                    ]
                }
            }

            "predicate eq with Byte primitive" {
                val path = nbtPath("Entities")[matching { "NoAI" eq 1.toByte() }]

                path.asString() shouldBe "Entities[{NoAI:1b}]"
            }

            "predicate eq with Short primitive" {
                val path = nbtPath("Items")[matching { "Damage" eq 100.toShort() }]

                path.asString() shouldBe "Items[{Damage:100s}]"
            }

            "predicate eq with Long primitive" {
                val path = nbtPath("Data")[matching { "Time" eq 1000L }]

                path.asString() shouldBe "Data[{Time:1000L}]"
            }

            "predicate eq with Float primitive" {
                val path = nbtPath("Entities")[matching { "Speed" eq 1.5f }]

                path.asString() shouldBe "Entities[{Speed:1.5f}]"
            }

            "predicate eq with Double primitive" {
                val path = nbtPath("Entities")[matching { "Health" eq 20.0 }]

                path.asString() shouldBe "Entities[{Health:20.0d}]"
            }
        },
    )
