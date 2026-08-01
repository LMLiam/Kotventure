package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.test.selector.shouldBeCanonicalSelector
import io.github.lmliam.kotventure.test.selector.shouldFailToParseAt
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SelectorSnbtParsingTest :
    StringSpec(
        {
            "round trips nested SNBT filters" {
                """@e[nbt={Tags:["boss"],Data:[I;1,2]},nbt=!{Health:20.0f}]"""
                    .shouldBeCanonicalSelector()
            }

            "accepts typed SNBT array boundaries" {
                """
                @e[nbt={Bytes:[B;-128b,+127b],Ints:[I;-2147483648,+2147483647],Longs:[L;-9223372036854775808L,+9223372036854775807L]}]
                """.trimIndent().shouldBeCanonicalSelector()
            }

            "parses signed typed arrays with kind-specific suffixes" {
                val parsed =
                    parseSelector("@e[nbt={Bytes:[B;1b,-2B],Ints:[I;1,-2],Longs:[L;1l,-2L]}]")
                val nbt = parsed.arguments.filterIsInstance<EntitySelectorArgument.Nbt>().single()

                nbt.snbt.value shouldBe "{Bytes:[B;1b,-2B],Ints:[I;1,-2],Longs:[L;1l,-2L]}"
            }

            "rejects typed-array elements with the wrong suffix" {
                "@e[nbt={values:[B;" shouldFailToParseAt "1]}]"
                "@e[nbt={values:[B;" shouldFailToParseAt "1l]}]"
                "@e[nbt={values:[I;" shouldFailToParseAt "1b]}]"
                "@e[nbt={values:[I;" shouldFailToParseAt "1l]}]"
                "@e[nbt={values:[L;" shouldFailToParseAt "1]}]"
                "@e[nbt={values:[L;" shouldFailToParseAt "1b]}]"
            }

            "rejects repeated typed-array suffixes" {
                "@e[nbt={values:[B;" shouldFailToParseAt "1bb]}]"
                "@e[nbt={values:[L;" shouldFailToParseAt "1ll]}]"
            }

            "preserves Java Edition 26.2 SNBT container forms" {
                listOf(
                    """@e[nbt={foo:1b,}]""",
                    """@e[nbt={Tags:[1b,"mixed",]}]""",
                    """@e[nbt={values:[1b,2b,]}]""",
                    """@e[nbt={Data:[B;+1b,]}]""",
                    """@e[nbt={values:[I;1,2,]}]""",
                ).forEach { source ->
                    source.shouldBeCanonicalSelector()
                }
            }

            "stops unquoted SNBT scalars at every container terminator" {
                """@e[nbt={a:1,b:[2],c:{d:3}}]""".shouldBeCanonicalSelector()
            }

            "rejects malformed SNBT structure" {
                "@e[nbt={foo" shouldFailToParseAt "}]"
                "@e[nbt={list:[1 " shouldFailToParseAt "2]}]"
                "@e[nbt={id:minecraft" shouldFailToParseAt ":stone}]"
            }

            "rejects typed SNBT array overflow" {
                "@e[nbt={Data:[B;" shouldFailToParseAt "128b]}]"
                "@e[nbt={Data:[B;" shouldFailToParseAt "-129b]}]"
                "@e[nbt={Data:[I;" shouldFailToParseAt "2147483648]}]"
                "@e[nbt={Data:[I;" shouldFailToParseAt "-2147483649]}]"
                "@e[nbt={Data:[L;" shouldFailToParseAt "9223372036854775808L]}]"
                "@e[nbt={Data:[L;" shouldFailToParseAt "-9223372036854775809L]}]"
            }

            "rejects invalid characters in unquoted SNBT scalars immediately" {
                "@e[nbt={id:minecraft" shouldFailToParseAt ":stone}]"
            }

            "rejects empty unquoted SNBT values" {
                "@e[nbt={value:" shouldFailToParseAt "}]"
            }
        },
    )
