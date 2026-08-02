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
                """@e[type=!#my_pack:hostile,name="Boss Mob",gamemode=!creative,tag=!hidden,team=blue,predicate=!my_pack:hidden]"""
                    .shouldBeCanonicalSelector()
            }

            "exposes parsed filter structure" {
                parseSelector(
                    """@e[type=!#my_pack:hostile,name="Boss Mob",gamemode=!creative]""",
                ).arguments shouldBe
                        listOf(
                            EntitySelectorArgument.Type(
                                SelectorEntityType.Tag(key("my_pack", "hostile")),
                                isNegated = true,
                            ),
                            EntitySelectorArgument.Name("Boss Mob", isNegated = false),
                            EntitySelectorArgument.GameMode(GameMode.CREATIVE, isNegated = true),
                        )
            }

            "preserves negation across every modeled filter argument" {
                val parsed =
                    parseSelector(
                        """@e[gamemode=!creative,name=!Boss,type=!minecraft:zombie,type=!#minecraft:raiders,tag=!hidden,team=!red,nbt=!{Health:1b},predicate=!my_pack:hidden]""",
                    )

                parsed.arguments.filterIsInstance<EntitySelectorArgument.GameMode>().single() shouldBe
                        EntitySelectorArgument.GameMode(GameMode.CREATIVE, isNegated = true)
                parsed.arguments.filterIsInstance<EntitySelectorArgument.Name>().single() shouldBe
                        EntitySelectorArgument.Name("Boss", isNegated = true)
                parsed.arguments.filterIsInstance<EntitySelectorArgument.Type>() shouldBe
                        listOf(
                            EntitySelectorArgument.Type(
                                SelectorEntityType.Direct(key("minecraft", "zombie")),
                                isNegated = true,
                            ),
                            EntitySelectorArgument.Type(
                                SelectorEntityType.Tag(key("minecraft", "raiders")),
                                isNegated = true,
                            ),
                        )
                parsed.arguments.filterIsInstance<EntitySelectorArgument.Tag>().single() shouldBe
                        EntitySelectorArgument.Tag(SelectorStringCondition.Named("hidden", isNegated = true))
                parsed.arguments.filterIsInstance<EntitySelectorArgument.Team>().single() shouldBe
                        EntitySelectorArgument.Team(SelectorStringCondition.Named("red", isNegated = true))

                val nbt = parsed.arguments.filterIsInstance<EntitySelectorArgument.Nbt>().single()
                nbt.isNegated shouldBe true
                nbt.snbt.value shouldBe "{Health:1b}"

                parsed.arguments.filterIsInstance<EntitySelectorArgument.Predicate>().single() shouldBe
                        EntitySelectorArgument.Predicate(key("my_pack", "hidden"), isNegated = true)
            }

            "does not consume exclamation marks inside non-negated values" {
                val parsed =
                    parseSelector(
                        """@e[gamemode=creative,name="!Boss",type=minecraft:zombie,tag=visible,team=red,predicate=my_pack:hidden,nbt={value:"!kept"}]""",
                    )

                parsed.arguments.filterIsInstance<EntitySelectorArgument.GameMode>().single() shouldBe
                        EntitySelectorArgument.GameMode(GameMode.CREATIVE, isNegated = false)
                parsed.arguments.filterIsInstance<EntitySelectorArgument.Name>().single() shouldBe
                        EntitySelectorArgument.Name("!Boss", isNegated = false)
                parsed.arguments.filterIsInstance<EntitySelectorArgument.Type>().single() shouldBe
                        EntitySelectorArgument.Type(
                            SelectorEntityType.Direct(key("minecraft", "zombie")),
                            isNegated = false,
                        )
                parsed.arguments.filterIsInstance<EntitySelectorArgument.Tag>().single() shouldBe
                        EntitySelectorArgument.Tag(SelectorStringCondition.Named("visible"))
                parsed.arguments.filterIsInstance<EntitySelectorArgument.Team>().single() shouldBe
                        EntitySelectorArgument.Team(SelectorStringCondition.Named("red"))
                parsed.arguments.filterIsInstance<EntitySelectorArgument.Predicate>().single() shouldBe
                        EntitySelectorArgument.Predicate(key("my_pack", "hidden"), isNegated = false)

                val nbt = parsed.arguments.filterIsInstance<EntitySelectorArgument.Nbt>().single()
                nbt.isNegated shouldBe false
                nbt.snbt.value shouldBe "{value:\"!kept\"}"
            }

            "rejects repeated filter negation" {
                "@e[name=!" shouldFailToParseAt "!Boss]"
                "@e[type=!" shouldFailToParseAt "!minecraft:zombie]"
                "@e[nbt=!" shouldFailToParseAt "!{}]"
            }

            "renders decoded selector names canonically" {
                parseSelector("""@e[name='Boss Mob']""") shouldRenderAs """@e[name="Boss Mob"]"""
                """@e[name="Boss \"Mob\""]""".shouldBeCanonicalSelector()
            }

            "preserves repeated empty-value filters on repeatable groups" {
                """@e[tag=,tag=!]""".shouldBeCanonicalSelector()
                """@e[team=]""".shouldBeCanonicalSelector()
                """@e[team=!]""".shouldBeCanonicalSelector()
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
