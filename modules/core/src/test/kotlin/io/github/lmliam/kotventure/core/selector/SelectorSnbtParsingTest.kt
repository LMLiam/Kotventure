package io.github.lmliam.kotventure.core.selector

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SelectorSnbtParsingTest :
    StringSpec(
        {
            "round trips nested SNBT filters" {
                val source = "@e[nbt={Tags:[\"boss\"],Data:[I;1,2]},nbt=!{Health:20.0f}]"

                entitySelector(source).asString() shouldBe source
            }

            "accepts typed SNBT array boundaries" {
                val source =
                    "@e[nbt={" +
                            "Bytes:[B;-128b,+127b]," +
                            "Ints:[I;-2147483648,+2147483647]," +
                            "Longs:[L;-9223372036854775808L,+9223372036854775807L]" +
                            "}]"

                entitySelector(source).asString() shouldBe source
            }

            "preserves Java Edition 26.2 SNBT container forms" {
                listOf(
                    "@e[nbt={foo:1b,}]",
                    "@e[nbt={Tags:[1b,\"mixed\",]}]",
                    "@e[nbt={values:[1b,2b,]}]",
                    "@e[nbt={Data:[B;+1b,]}]",
                    "@e[nbt={values:[I;1,2,]}]",
                ).forEach { source ->
                    entitySelector(source).asString() shouldBe source
                }
            }

            "stops unquoted SNBT scalars at every container terminator" {
                entitySelector("@e[nbt={a:1,b:[2],c:{d:3}}]").asString() shouldBe
                        "@e[nbt={a:1,b:[2],c:{d:3}}]"
            }

            "rejects malformed SNBT structure" {
                assertParseFailure("@e[nbt={foo}]", 11, "Expected ':'")
                assertParseFailure("@e[nbt={list:[1 2]}]", 16, "Expected ','")
                assertParseFailure("@e[nbt={id:minecraft:stone}]", 20, "Invalid unquoted SNBT token")
            }

            "rejects typed SNBT array overflow" {
                listOf(
                    "@e[nbt={Data:[B;128b]}]" to "128b",
                    "@e[nbt={Data:[I;2147483648]}]" to "2147483648",
                    "@e[nbt={Data:[L;9223372036854775808L]}]" to "9223372036854775808L",
                ).forEach { (source, invalidValue) ->
                    assertParseFailure(source, source.indexOf(invalidValue), "Invalid")
                }
            }
        },
    )
