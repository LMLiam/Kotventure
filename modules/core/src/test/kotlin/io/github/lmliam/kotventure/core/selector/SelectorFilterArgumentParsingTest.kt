package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.test.selector.shouldBeCanonicalSelector
import io.github.lmliam.kotventure.test.selector.shouldFailToParseAt
import io.github.lmliam.kotventure.test.selector.shouldRenderAs
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

                source.shouldBeCanonicalSelector()
            }

            "exposes parsed filter structure" {
                val parsed = entitySelector("@e[type=!#my_pack:hostile,name=\"Boss Mob\",gamemode=!creative]")

                parsed.arguments shouldBe
                    listOf(
                        EntitySelectorArgument.Type(
                            SelectorEntityType.Tag(key("my_pack", "hostile")),
                            isNegated = true,
                        ),
                        EntitySelectorArgument.Name("Boss Mob", isNegated = false),
                        EntitySelectorArgument.GameMode(GameMode.CREATIVE, isNegated = true),
                    )
            }

            "renders decoded selector names canonically" {
                entitySelector("@e[name='Boss Mob']") shouldRenderAs "@e[name=\"Boss Mob\"]"
                "@e[name=\"Boss \\\"Mob\\\"\"]".shouldBeCanonicalSelector()
            }

            "preserves repeated empty-value filters" {
                "@e[tag=,tag=!,team=,team=!]".shouldBeCanonicalSelector()
            }

            "rejects malformed names" {
                "@e[name=" shouldFailToParseAt "]"
                "@e[name=" shouldFailToParseAt "\"Boss]"
                "@e[name=\"bad" shouldFailToParseAt "\\q\"]"
            }

            "rejects malformed keys and tokens" {
                "@e[type=!" shouldFailToParseAt "!minecraft:zombie]"
                "@e[type=#" shouldFailToParseAt "Bad:Key]"
                "@e[tag=" shouldFailToParseAt "bad value]"
            }

            "rejects unsupported game modes" {
                "@e[gamemode=!" shouldFailToParseAt "builder]"
            }
        },
    )
