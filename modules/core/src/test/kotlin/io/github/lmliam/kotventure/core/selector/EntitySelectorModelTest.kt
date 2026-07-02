package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.text.shouldBeSelectorComponent
import io.github.lmliam.kotventure.test.text.shouldHaveSelectorPattern
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class EntitySelectorModelTest :
    StringSpec(
        {
            "exposes an immutable model that can be transformed" {
                val parsed = entitySelector("@e[type=minecraft:zombie,name=\"Boss Mob\",tag=!hidden]")

                parsed.head shouldBe EntitySelectorHead.ENTITIES
                parsed.arguments shouldHaveSize 3
                parsed.arguments.first().shouldBeInstanceOf<EntitySelectorArgument.Type>()
                parsed.arguments[1].shouldBeInstanceOf<EntitySelectorArgument.Name>()
                parsed.arguments[2].shouldBeInstanceOf<EntitySelectorArgument.Tag>()
                val transformed =
                    EntitySelector(
                        parsed.head,
                        parsed.arguments.filterNot { it is EntitySelectorArgument.Name },
                    )

                transformed.asString() shouldBe "@e[type=minecraft:zombie,tag=!hidden]"
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
                val parsed = entitySelector("@e[type=!minecraft:zombie,tag=boss,nbt=!{},limit=1]")
                val negatable = parsed.arguments.filterIsInstance<EntitySelectorArgument.Negatable>()

                negatable shouldHaveSize 3
                negatable.count { it.isNegated } shouldBe 2
            }

            "represents direct entity types and type tags without a boolean flag" {
                val direct =
                    entitySelector("@e[type=minecraft:zombie]")
                        .arguments
                        .filterIsInstance<EntitySelectorArgument.Type>()
                        .single()
                val tag =
                    entitySelector("@e[type=#minecraft:raiders]")
                        .arguments
                        .filterIsInstance<EntitySelectorArgument.Type>()
                        .single()

                direct.target shouldBe SelectorEntityType.Direct(key("minecraft", "zombie"))
                tag.target shouldBe SelectorEntityType.Tag(key("minecraft", "raiders"))
            }

            "stores named string-filter negation inside the condition" {
                val tag =
                    entitySelector("@e[tag=!hidden]")
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

                scores.scores.single().objective shouldBe "kills"
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
                }.message shouldContain "does not support 'type'"

                assertParseFailure("@a[type=minecraft:zombie]", 3, "does not support 'type'")
            }

            "models tag and team presence explicitly" {
                val parsed = entitySelector("@e[tag=,tag=!,team=red,team=!blue]")
                val tags = parsed.arguments.filterIsInstance<EntitySelectorArgument.Tag>()
                val teams = parsed.arguments.filterIsInstance<EntitySelectorArgument.Team>()

                tags.map(EntitySelectorArgument.Tag::condition) shouldBe
                        listOf(
                            SelectorStringCondition.Presence(SelectorPresence.NONE),
                            SelectorStringCondition.Presence(SelectorPresence.ANY),
                        )
                teams.map(EntitySelectorArgument.Team::condition) shouldBe
                        listOf(
                            SelectorStringCondition.Named("red"),
                            SelectorStringCondition.Named("blue", isNegated = true),
                        )
                teams.map(EntitySelectorArgument.Team::isNegated) shouldBe listOf(false, true)
            }

            "exposes validated SNBT source" {
                val nbt =
                    entitySelector("@e[nbt=!{Health:20.0f}]")
                        .arguments
                        .filterIsInstance<EntitySelectorArgument.Nbt>()
                        .single()

                nbt.snbt.value shouldBe "{Health:20.0f}"
            }

            "defensively snapshots every collection-backed model value" {
                val sourceArguments =
                    mutableListOf<EntitySelectorArgument>(
                        EntitySelectorArgument.Tag(SelectorStringCondition.Named("admin")),
                    )
                val parsed = EntitySelector(EntitySelectorHead.ENTITIES, sourceArguments)
                sourceArguments.clear()

                parsed.arguments shouldHaveSize 1
                parsed shouldBe
                        EntitySelector(
                            EntitySelectorHead.ENTITIES,
                            parsed.arguments,
                        )
                shouldThrow<UnsupportedOperationException> {
                    @Suppress("UNCHECKED_CAST")
                    (parsed.arguments as MutableList<EntitySelectorArgument>).clear()
                }

                val scoreSource = mutableListOf(SelectorScoreRequirement("kills", exactly(1)))
                val scores = EntitySelectorArgument.Scores(scoreSource)
                scoreSource.clear()
                scores.scores shouldHaveSize 1

                val structured =
                    entitySelector(
                        "@e[advancements={minecraft:story/root={criterion=true}}]",
                    ).arguments
                        .filterIsInstance<EntitySelectorArgument.Advancements>()
                        .single()
                val criteria =
                    structured.advancements
                        .single()
                        .progress
                        .shouldBeInstanceOf<SelectorAdvancementProgress.Criteria>()
                shouldThrow<UnsupportedOperationException> {
                    @Suppress("UNCHECKED_CAST")
                    (structured.advancements as MutableList<SelectorAdvancementRequirement>).clear()
                }
                shouldThrow<UnsupportedOperationException> {
                    @Suppress("UNCHECKED_CAST")
                    (criteria.criteria as MutableList<SelectorAdvancementCriterion>).clear()
                }
            }

            "exposes parsed range bounds without reparsing rendered strings" {
                val parsed = entitySelector("@e[distance=..10,level=2..5,scores={kills=-1..}]")
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
                val parsed = entitySelector("@a[tag=admin]")

                selector(parsed)
                    .shouldBeSelectorComponent()
                    .shouldHaveSelectorPattern("@a[tag=admin]")
            }
        },
    )
