package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.selector.shouldFailToParseAt
import io.github.lmliam.kotventure.test.selector.shouldRenderAs
import io.github.lmliam.kotventure.test.text.shouldBeSelectorComponent
import io.github.lmliam.kotventure.test.text.shouldHaveSelectorPattern
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class EntitySelectorModelTest :
    StringSpec(
        {
            "exposes an immutable model that can be transformed" {
                val parsed =
                    parseSelector("""@e[type=minecraft:zombie,name="Boss Mob",tag=!hidden]""")

                parsed.head shouldBe EntitySelectorHead.ENTITIES
                parsed.arguments shouldBe
                        listOf(
                            EntitySelectorArgument.Type(
                                SelectorEntityType.Direct(key("minecraft", "zombie")),
                                isNegated = false,
                            ),
                            EntitySelectorArgument.Name("Boss Mob", isNegated = false),
                            EntitySelectorArgument.Tag(SelectorStringCondition.Named("hidden", isNegated = true)),
                        )

                EntitySelector(
                    parsed.head,
                    parsed.arguments.filterNot { it is EntitySelectorArgument.Name },
                ) shouldRenderAs "@e[type=minecraft:zombie,tag=!hidden]"
            }

            "hides invariant-bypassing generated copy methods" {
                assertDoesNotCompile(
                    "SelectorCopyVisibilityTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.EntitySelector
                    import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument

                    fun invalid(selector: EntitySelector) {
                        selector.copy(arguments = mutableListOf<EntitySelectorArgument>())
                    }
                    """.trimIndent(),
                    "Cannot access",
                )
            }

            "exposes negation through the shared Negatable interface" {
                val negatable =
                    parseSelector("@e[type=!minecraft:zombie,tag=boss,nbt=!{},limit=1]")
                        .arguments
                        .filterIsInstance<EntitySelectorArgument.Negatable>()

                negatable shouldHaveSize 3
                negatable.count { it.isNegated } shouldBe 2
            }

            "represents direct entity types and type tags without a boolean flag" {
                val direct =
                    parseSelector("@e[type=minecraft:zombie]")
                        .arguments
                        .filterIsInstance<EntitySelectorArgument.Type>()
                        .single()
                val tag =
                    parseSelector("@e[type=#minecraft:raiders]")
                        .arguments
                        .filterIsInstance<EntitySelectorArgument.Type>()
                        .single()

                direct.target shouldBe SelectorEntityType.Direct(key("minecraft", "zombie"))
                tag.target shouldBe SelectorEntityType.Tag(key("minecraft", "raiders"))
            }

            "stores named string-filter negation inside the condition" {
                val tag =
                    parseSelector("@e[tag=!hidden]")
                        .arguments
                        .filterIsInstance<EntitySelectorArgument.Tag>()
                        .single()

                tag.condition shouldBe SelectorStringCondition.Named("hidden", isNegated = true)
                tag.isNegated shouldBe true
            }

            "uses semantic requirement names for scores and advancements" {
                val scores =
                    EntitySelectorArgument.Scores(
                        listOf(SelectorScoreRequirement("kills", exactly(1))),
                    )
                val advancements =
                    EntitySelectorArgument.Advancements(
                        listOf(
                            SelectorAdvancementRequirement(
                                key("minecraft", "story/root"),
                                SelectorAdvancementProgress.Completion(true),
                            ),
                        ),
                    )

                scores.scores.single().objective shouldBe "kills"
                advancements.advancements.single().advancement shouldBe key("minecraft", "story/root")
            }

            "rejects invalid public argument construction" {
                shouldThrow<IllegalArgumentException> {
                    EntitySelectorArgument.Limit(0)
                }
                shouldThrow<IllegalArgumentException> {
                    EntitySelectorArgument.Coordinate(SelectorCoordinate.X, Double.NaN)
                }
                shouldThrow<IllegalArgumentException> {
                    SelectorStringCondition.Named("")
                }
                shouldThrow<IllegalArgumentException> {
                    SelectorScoreRequirement("bad objective", exactly(1))
                }
                shouldThrow<IllegalArgumentException> {
                    SelectorAdvancementCriterion("bad criterion", completed = true)
                }
                shouldThrow<EntitySelectorParseException> {
                    SnbtCompoundSource.parse("definitely not SNBT")
                }
            }

            "rejects arguments incompatible with the selector head" {
                shouldThrow<IllegalArgumentException> {
                    EntitySelector(
                        EntitySelectorHead.ALL_PLAYERS,
                        listOf(
                            EntitySelectorArgument.Type(
                                SelectorEntityType.Direct(key("minecraft", "zombie")),
                                isNegated = false,
                            ),
                        ),
                    )
                }
                shouldThrow<IllegalArgumentException> {
                    EntitySelector(
                        EntitySelectorHead.SELF,
                        listOf(EntitySelectorArgument.Limit(1)),
                    )
                }
            }

            "uses the same head compatibility policy for parsing and construction" {
                val type =
                    EntitySelectorArgument.Type(
                        SelectorEntityType.Direct(key("minecraft", "zombie")),
                        isNegated = false,
                    )

                shouldThrow<IllegalArgumentException> {
                    EntitySelector(EntitySelectorHead.ALL_PLAYERS, listOf(type))
                }

                "@a[" shouldFailToParseAt "type=minecraft:zombie]"
            }

            "models tag and team presence explicitly" {
                val parsed = parseSelector("@e[tag=,tag=!,team=red,team=!blue]")

                parsed.arguments.filterIsInstance<EntitySelectorArgument.Tag>() shouldBe
                        listOf(
                            EntitySelectorArgument.Tag(SelectorStringCondition.Presence(SelectorPresence.NONE)),
                            EntitySelectorArgument.Tag(SelectorStringCondition.Presence(SelectorPresence.ANY)),
                        )
                parsed.arguments.filterIsInstance<EntitySelectorArgument.Team>() shouldBe
                        listOf(
                            EntitySelectorArgument.Team(SelectorStringCondition.Named("red")),
                            EntitySelectorArgument.Team(SelectorStringCondition.Named("blue", isNegated = true)),
                        )
            }

            "exposes validated SNBT source" {
                val nbt =
                    parseSelector("@e[nbt=!{Health:20.0f}]")
                        .arguments
                        .filterIsInstance<EntitySelectorArgument.Nbt>()
                        .single()

                nbt.snbt.value shouldBe "{Health:20.0f}"
            }

            "defensively snapshots collection-backed model values" {
                val sourceArguments =
                    mutableListOf(
                        EntitySelectorArgument.Tag(SelectorStringCondition.Named("admin")),
                    )
                val parsed = EntitySelector(EntitySelectorHead.ENTITIES, sourceArguments)
                sourceArguments.clear()

                parsed.arguments shouldBe
                        listOf(
                            EntitySelectorArgument.Tag(SelectorStringCondition.Named("admin")),
                        )

                shouldThrow<UnsupportedOperationException> {
                    @Suppress("UNCHECKED_CAST")
                    (parsed.arguments as MutableList<EntitySelectorArgument>).clear()
                }

                val scoreSource = mutableListOf(SelectorScoreRequirement("kills", exactly(1)))
                val scores = EntitySelectorArgument.Scores(scoreSource)
                scoreSource.clear()
                scores.scores shouldHaveSize 1
            }

            "exposes parsed range bounds without reparsing rendered strings" {
                val parsed = parseSelector("@e[distance=..10,level=2..5,scores={kills=-1..}]")
                val distance = parsed.arguments.filterIsInstance<EntitySelectorArgument.Range>().single()
                val level = parsed.arguments.filterIsInstance<EntitySelectorArgument.Level>().single()
                val scores = parsed.arguments.filterIsInstance<EntitySelectorArgument.Scores>().single()

                distance.range.minimum shouldBe null
                distance.range.maximum shouldBe 10.0
                level.range.minimum shouldBe 2
                level.range.maximum shouldBe 5
                scores.scores
                    .single()
                    .range.minimum shouldBe -1
                scores.scores
                    .single()
                    .range.maximum shouldBe null
            }

            "DSL factories return the shared structured selector model" {
                val built =
                    entities {
                        !type("zombie")
                        tag("boss")
                    }

                built.head shouldBe EntitySelectorHead.ENTITIES
                built.arguments shouldHaveSize 2
                built.arguments.first().shouldBeInstanceOf<EntitySelectorArgument.Type>()
                built.arguments.last().shouldBeInstanceOf<EntitySelectorArgument.Tag>()
            }

            "supplies parsed selectors directly to selector components" {
                val parsed = parseSelector("@a[tag=admin]")

                selector(parsed)
                    .shouldBeSelectorComponent()
                    .shouldHaveSelectorPattern("@a[tag=admin]")
            }
        },
    )
