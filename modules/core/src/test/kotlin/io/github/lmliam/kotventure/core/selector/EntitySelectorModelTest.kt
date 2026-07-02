package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
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
                val parsed = entitySelector("@e[type=minecraft:zombie,name=\"Boss Mob\",tag=!hidden]")

                parsed.head shouldBe EntitySelectorHead.ENTITIES
                parsed.arguments shouldHaveSize 3
                parsed.arguments.first().shouldBeInstanceOf<EntitySelectorArgument.Type>()
                parsed.arguments[1].shouldBeInstanceOf<EntitySelectorArgument.Name>()
                parsed.arguments[2].shouldBeInstanceOf<EntitySelectorArgument.Tag>()
                val transformed =
                    parsed.copy(
                        arguments = parsed.arguments.filterNot { it is EntitySelectorArgument.Name },
                    )

                transformed.asString() shouldBe "@e[type=minecraft:zombie,tag=!hidden]"
            }

            "exposes negation through the shared Negatable interface" {
                val parsed = entitySelector("@e[type=!minecraft:zombie,tag=boss,nbt=!{},limit=1]")
                val negatable = parsed.arguments.filterIsInstance<EntitySelectorArgument.Negatable>()

                negatable shouldHaveSize 3
                negatable.count { it.isNegated } shouldBe 2
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
                                key("minecraft", "zombie"),
                                isTag = false,
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
                        SelectorStringCondition.Named("blue"),
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
                        EntitySelectorArgument.Tag(
                            SelectorStringCondition.Named("admin"),
                            isNegated = false,
                        ),
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

                val scoreSource = mutableListOf(ParsedSelectorScore("kills", exactly(1)))
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
                        .shouldBeInstanceOf<ParsedAdvancementProgress.Criteria>()
                shouldThrow<UnsupportedOperationException> {
                    @Suppress("UNCHECKED_CAST")
                    (structured.advancements as MutableList<ParsedSelectorAdvancement>).clear()
                }
                shouldThrow<UnsupportedOperationException> {
                    @Suppress("UNCHECKED_CAST")
                    (criteria.criteria as MutableList<ParsedAdvancementCriterion>).clear()
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
