package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.test.selector.shouldBeCanonicalSelector
import io.github.lmliam.kotventure.test.selector.shouldFailToParseAt
import io.kotest.core.spec.style.StringSpec

class SelectorSnbtParsingTest :
    StringSpec(
        {
            "round trips nested SNBT filters" {
                "@e[nbt={Tags:[\"boss\"],Data:[I;1,2]},nbt=!{Health:20.0f}]".shouldBeCanonicalSelector()
            }

            "accepts typed SNBT array boundaries" {
                val source =
                    "@e[nbt={" +
                        "Bytes:[B;-128b,+127b]," +
                        "Ints:[I;-2147483648,+2147483647]," +
                        "Longs:[L;-9223372036854775808L,+9223372036854775807L]" +
                        "}]"

                source.shouldBeCanonicalSelector()
            }

            "preserves Java Edition 26.2 SNBT container forms" {
                listOf(
                    "@e[nbt={foo:1b,}]",
                    "@e[nbt={Tags:[1b,\"mixed\",]}]",
                    "@e[nbt={values:[1b,2b,]}]",
                    "@e[nbt={Data:[B;+1b,]}]",
                    "@e[nbt={values:[I;1,2,]}]",
                ).forEach { source ->
                    source.shouldBeCanonicalSelector()
                }
            }

            "stops unquoted SNBT scalars at every container terminator" {
                "@e[nbt={a:1,b:[2],c:{d:3}}]".shouldBeCanonicalSelector()
            }

            "rejects malformed SNBT structure" {
                "@e[nbt={foo" shouldFailToParseAt "}]"
                "@e[nbt={list:[1 " shouldFailToParseAt "2]}]"
                "@e[nbt={id:minecraft" shouldFailToParseAt ":stone}]"
            }

            "rejects typed SNBT array overflow" {
                "@e[nbt={Data:[B;" shouldFailToParseAt "128b]}]"
                "@e[nbt={Data:[I;" shouldFailToParseAt "2147483648]}]"
                "@e[nbt={Data:[L;" shouldFailToParseAt "9223372036854775808L]}]"
            }
        },
    )
