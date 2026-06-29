package io.github.lmliam.kotventure.core.nbt

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class NbtFactoryTest :
    StringSpec(
        {
            "builds a single byte entry" {
                nbt { "kotventure" eq 1.toByte() }.string() shouldBe "{kotventure:1b}"
            }

            "builds a single string entry" {
                nbt { "id" eq "minecraft:diamond" }.string() shouldBe "{id:\"minecraft:diamond\"}"
            }

            "builds a single int entry" {
                nbt { "count" eq 64 }.string() shouldBe "{count:64}"
            }

            "builds a single short entry" {
                nbt { "slot" eq 3.toShort() }.string() shouldBe "{slot:3s}"
            }

            "builds a single long entry" {
                nbt { "time" eq 1000L }.string() shouldBe "{time:1000L}"
            }

            "builds a single float entry" {
                nbt { "speed" eq 1.5f }.string() shouldBe "{speed:1.5f}"
            }

            "builds a single double entry" {
                nbt { "health" eq 20.0 }.string() shouldBe "{health:20.0d}"
            }

            "builds a nested compound" {
                nbt { "display" eq { "Name" eq "Sword" } }.string() shouldBe
                    "{display:{Name:\"Sword\"}}"
            }

            "builds a deeply nested compound" {
                nbt {
                    "tag" eq {
                        "display" eq {
                            "Name" eq "Diamond Sword"
                        }
                    }
                }.string() shouldBe "{tag:{display:{Name:\"Diamond Sword\"}}}"
            }

            "builds multi-entry compounds" {
                nbt {
                    "id" eq "minecraft:diamond"
                    "Count" eq 64.toByte()
                }.string() shouldBe "{id:\"minecraft:diamond\",Count:64b}"
            }

            "builds a byte array" {
                nbt { "Data" eq byteArrayOf(1, 2, 3) }.string() shouldBe "{Data:[B;1b,2b,3b]}"
            }

            "builds an int array" {
                nbt { "UUID" eq intArrayOf(1, 2, 3, 4) }.string() shouldBe "{UUID:[I;1,2,3,4]}"
            }

            "builds a long array" {
                nbt { "Times" eq longArrayOf(10L, 20L) }.string() shouldBe "{Times:[L;10L,20L]}"
            }

            "last-write-wins for duplicate keys" {
                nbt {
                    "key" eq 1
                    "key" eq 2
                }.string() shouldBe "{key:2}"
            }

            "raw SNBT passthrough" {
                nbt("{arbitrary:data}").string() shouldBe "{arbitrary:data}"
            }

            "escapes special characters in string values" {
                nbt { "msg" eq "say \"hello\"" }.string() shouldBe "{msg:\"say \\\"hello\\\"\"}"
            }

            "quotes non-alphanumeric keys" {
                nbt { "foo.bar" eq 1 }.string() shouldBe "{\"foo.bar\":1}"
            }
        },
    )
