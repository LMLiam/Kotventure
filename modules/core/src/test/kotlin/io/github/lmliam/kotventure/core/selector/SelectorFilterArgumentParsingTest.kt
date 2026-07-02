package io.github.lmliam.kotventure.core.selector

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SelectorFilterArgumentParsingTest :
    StringSpec(
        {
            "round trips type, name, gamemode, tag, team, and predicate filters" {
                val source =
                    "@e[" +
                            "type=!#my_pack:hostile," +
                            "name=\"Boss Mob\"," +
                            "gamemode=!creative," +
                            "tag=!hidden,team=blue," +
                            "predicate=!my_pack:hidden" +
                            "]"

                entitySelector(source).asString() shouldBe source
            }

            "renders decoded selector names canonically" {
                entitySelector("@e[name='Boss Mob']").asString() shouldBe "@e[name=\"Boss Mob\"]"
                entitySelector("@e[name=\"Boss \\\"Mob\\\"\"]").asString() shouldBe
                        "@e[name=\"Boss \\\"Mob\\\"\"]"
            }

            "preserves repeated empty-value filters" {
                entitySelector("@e[tag=,tag=!,team=,team=!]").asString() shouldBe
                        "@e[tag=,tag=!,team=,team=!]"
            }

            "rejects malformed names" {
                assertParseFailure("@e[name=]", 8, "Invalid unquoted selector name")
                assertParseFailure("@e[name=\"Boss]", 8, "Unterminated quoted string")
                assertParseFailure("@e[name=\"bad\\q\"]", 12, "Invalid quoted-string escape")
            }

            "rejects malformed keys and tokens" {
                assertParseFailure("@e[type=!!minecraft:zombie]", 9, "Invalid namespaced key")
                assertParseFailure("@e[type=#Bad:Key]", 9, "Invalid namespaced key")
                assertParseFailure("@e[tag=bad value]", 7, "Invalid unquoted selector token")
            }

            "rejects unsupported game modes" {
                assertParseFailure("@e[gamemode=!builder]", 13, "Unsupported game mode")
            }
        },
    )
