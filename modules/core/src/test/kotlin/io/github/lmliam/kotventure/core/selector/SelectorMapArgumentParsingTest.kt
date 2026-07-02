package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.test.selector.shouldBeCanonicalSelector
import io.github.lmliam.kotventure.test.selector.shouldFailToParseAt
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

                source.shouldBeCanonicalSelector()
            }

            "exposes parsed scores and advancement structure" {
                val parsed = entitySelector("@e[scores={kills=5},advancements={my_pack:secret={found_item=false}}]")
                val scores = parsed.arguments.filterIsInstance<EntitySelectorArgument.Scores>().single()
                val advancements =
                    parsed.arguments.filterIsInstance<EntitySelectorArgument.Advancements>().single()

                scores.scores shouldBe listOf(SelectorScoreRequirement("kills", exactly(5)))
                advancements.advancements shouldBe
                    listOf(
                        SelectorAdvancementRequirement(
                            key("my_pack", "secret"),
                            SelectorAdvancementProgress.Criteria(
                                listOf(SelectorAdvancementCriterion("found_item", completed = false)),
                            ),
                        ),
                    )
            }

            "rejects malformed scores entries" {
                "@e[scores={kills=1," shouldFailToParseAt "}]"
                "@e[scores={kills" shouldFailToParseAt "}]"
            }

            "rejects malformed advancement progress" {
                "@e[advancements={minecraft:story/root=" shouldFailToParseAt "maybe}]"
                "@e[advancements={minecraft:story/root={criterion=" shouldFailToParseAt "maybe}}]"
            }
        },
    )
