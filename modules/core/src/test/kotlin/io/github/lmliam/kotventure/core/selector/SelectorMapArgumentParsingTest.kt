package io.github.lmliam.kotventure.core.selector

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SelectorMapArgumentParsingTest :
    StringSpec(
        {
            "round trips scores and advancements arguments" {
                val source =
                    "@e[" +
                        "scores={kills=5,balance=-10..}," +
                        "advancements={minecraft:story/root=true,my_pack:secret={found_item=false}}" +
                        "]"

                entitySelector(source).asString() shouldBe source
            }

            "rejects malformed scores entries" {
                assertParseFailure("@e[scores={kills=1,}]", 19, "Expected score objective")
                assertParseFailure("@e[scores={kills}]", 16, "Expected '='")
            }

            "rejects malformed advancement progress" {
                assertParseFailure(
                    "@e[advancements={minecraft:story/root=maybe}]",
                    38,
                    "Expected 'true' or 'false'",
                )
                assertParseFailure(
                    "@e[advancements={minecraft:story/root={criterion=maybe}}]",
                    49,
                    "Expected 'true' or 'false'",
                )
            }
        },
    )
