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
                val path = nbtPath("Items")[matching { key("id") eq "minecraft:diamond" }]["Count"]

                path.asString() shouldBe "Items[{id:\"minecraft:diamond\"}].Count"
            }

            "predicate with typed byte literal" {
                val path = nbtPath("Entities")[matching { key("NoAI") eq nbtByte(1) }]

                path.asString() shouldBe "Entities[{NoAI:1b}]"
            }

            "predicate with multiple entries" {
                val path =
                    nbtPath("Items")[
                        matching {
                    key("id") eq "minecraft:diamond"
                    key("Count") eq nbtByte(64)
                },
                    ]["Slot"]

                path.asString() shouldBe "Items[{id:\"minecraft:diamond\",Count:64b}].Slot"
            }

            "escape hatch wraps a raw string" {
                val path = nbtPath("Items[{id:\"minecraft:diamond\"}].Count")

                path.asString() shouldBe "Items[{id:\"minecraft:diamond\"}].Count"
            }

            "heuristic detects dot as raw path" {
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

            "chaining operators on a raw path throws" {
                shouldThrow<IllegalArgumentException> {
                    nbtPath("Items[0].id")["tag"]
                }
            }

            "predicate value with quote characters is escaped" {
                val path = nbtPath("Items")[matching { key("Name") eq "Bob's \"Special\" Item" }]

                path.asString() shouldBe "Items[{Name:\"Bob's \\\"Special\\\" Item\"}]"
            }
        },
    )
