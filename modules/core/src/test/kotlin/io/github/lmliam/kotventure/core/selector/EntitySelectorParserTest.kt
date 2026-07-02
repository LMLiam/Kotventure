package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.test.text.shouldBeSelectorComponent
import io.github.lmliam.kotventure.test.text.shouldHaveSelectorPattern
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class EntitySelectorParserTest :
    StringSpec(
        {
            "parses and renders all six selector heads" {
                listOf("@p", "@a", "@r", "@s", "@e", "@n").forEach { source ->
                    parseEntitySelector(source).asString() shouldBe source
                }
            }

            "round trips every typed selector argument" {
                val source =
                    "@e[" +
                        "type=!#my_pack:hostile," +
                        "name=\"Boss Mob\"," +
                        "x=1.5,y=-2,z=3,dx=0,dy=1,dz=-1," +
                        "distance=..10,x_rotation=170..-170,y_rotation=-45..45," +
                        "level=1..30,gamemode=!creative,limit=2,sort=nearest," +
                        "tag=!,tag=!hidden,team=!red,team=blue," +
                        "nbt={Tags:[\"boss\"],Data:[I;1,2]}," +
                        "nbt={Health:20.0f}," +
                        "scores={kills=5,balance=-10..}," +
                        "predicate=!my_pack:hidden," +
                        "predicate=my_pack:other," +
                        "advancements={minecraft:story/root=true,my_pack:secret={found_item=false}}" +
                        "]"

                parseEntitySelector(source).asString() shouldBe source
            }

            "preserves quoted selector strings" {
                parseEntitySelector("@e[name='Boss Mob']").asString() shouldBe "@e[name='Boss Mob']"
                parseEntitySelector("@e[name=\"Boss \\\"Mob\\\"\"]").asString() shouldBe
                    "@e[name=\"Boss \\\"Mob\\\"\"]"
            }

            "preserves explicit empty lists and repeated empty-value filters" {
                parseEntitySelector("@e[]").asString() shouldBe "@e[]"
                parseEntitySelector("@e[tag=,tag=!,team=,team=!]").asString() shouldBe
                    "@e[tag=,tag=!,team=,team=!]"
            }

            "accepts typed SNBT array boundaries" {
                val source =
                    "@e[nbt={" +
                        "Bytes:[B;-128b,+127b]," +
                        "Ints:[I;-2147483648,+2147483647]," +
                        "Longs:[L;-9223372036854775808L,+9223372036854775807L]" +
                        "}]"

                parseEntitySelector(source).asString() shouldBe source
            }

            "preserves Java Edition 26.2 SNBT container forms" {
                listOf(
                    "@e[nbt={foo:1b,}]",
                    "@e[nbt={Tags:[1b,\"mixed\",]}]",
                    "@e[nbt={Data:[B;+1b,]}]",
                ).forEach { source ->
                    parseEntitySelector(source).asString() shouldBe source
                }
            }

            "exposes an immutable model that can be transformed" {
                val parsed = parseEntitySelector("@e[type=minecraft:zombie,name=\"Boss Mob\",tag=!hidden]")

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
                val parsed = parseEntitySelector("@e[type=!minecraft:zombie,tag=boss,nbt=!{},limit=1]")
                val negatable = parsed.arguments.filterIsInstance<EntitySelectorArgument.Negatable>()

                negatable shouldHaveSize 3
                negatable.count { it.isNegated } shouldBe 2
            }

            "defensively snapshots every collection-backed model value" {
                val sourceArguments =
                    mutableListOf<EntitySelectorArgument>(
                        EntitySelectorArgument.Tag("admin", isNegated = false),
                    )
                val parsed = ParsedEntitySelector(EntitySelectorHead.ENTITIES, sourceArguments)
                sourceArguments.clear()

                parsed.arguments shouldHaveSize 1
                parsed.hasExplicitArgumentList shouldBe true
                parsed shouldBe
                    ParsedEntitySelector(
                        EntitySelectorHead.ENTITIES,
                        parsed.arguments,
                        hasExplicitArgumentList = false,
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
                    parseEntitySelector(
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
                val parsed = parseEntitySelector("@e[distance=..10,level=2..5,scores={kills=-1..}]")
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

            "round trips DSL-built selectors through the shared argument model" {
                val built =
                    entities {
                        !type("zombie")
                        name("Boss, Mob")
                        distance(atMost(10.0))
                        scores { "kills" eq atLeast(5) }
                        advancements { key("minecraft", "story/root") eq true }
                        tag("boss")
                        !nbt { "Health" eq 20.0f }
                        !predicate(key("my_pack", "hidden"))
                    }.asString()

                parseEntitySelector(built).asString() shouldBe built
            }

            "supplies parsed selectors to selector components" {
                val parsed = parseEntitySelector("@a[tag=admin]")

                selector(parsed.asEntitySelector())
                    .shouldBeSelectorComponent()
                    .shouldHaveSelectorPattern("@a[tag=admin]")
            }

            "reports malformed and unsupported syntax with source offsets" {
                assertParseFailure("e", 0, "Expected '@'")
                assertParseFailure("@q", 1, "Unsupported selector head")
                assertParseFailure("@e[unknown=value]", 3, "Unsupported selector argument 'unknown'")
                assertParseFailure("@e[name=\"Boss]", 8, "Unterminated quoted string")
                assertParseFailure("@e[distance=10..1]", 16, "must not exceed")
                assertParseFailure("@e[type=!!minecraft:zombie]", 9, "Invalid namespaced key")
                assertParseFailure("@e[nbt={foo}]", 11, "Expected ':'")
                assertParseFailure("@e[scores={kills=1,}]", 19, "Expected score objective")
                assertParseFailure(
                    "@e[advancements={minecraft:story/root={criterion=maybe}}]",
                    49,
                    "Expected 'true' or 'false'",
                )
                assertParseFailure("@e[tag=admin", 12, "Expected ']'")
            }

            "rejects malformed values in each structured grammar family" {
                assertParseFailure("@e[x=NaN]", 5, "finite decimal")
                assertParseFailure("@e[x=1..2]", 5, "finite decimal")
                assertParseFailure("@e[distance=..]", 12, "at least one bound")
                assertParseFailure("@e[distance=..-1]", 14, "non-negative")
                assertParseFailure("@e[x_rotation=1...2]", 17, "more than one")
                assertParseFailure("@e[level=-1]", 9, "non-negative")
                assertParseFailure("@e[level=5..2]", 12, "must not exceed")
                assertParseFailure("@e[limit=0]", 9, "positive")
                assertParseFailure("@e[sort=closest]", 8, "Unsupported selector sort")
                assertParseFailure("@e[gamemode=!builder]", 13, "Unsupported game mode")
                assertParseFailure("@e[name=\"bad\\q\"]", 12, "Invalid quoted-string escape")
                assertParseFailure("@e[type=#Bad:Key]", 9, "Invalid namespaced key")
                assertParseFailure("@e[tag=bad value]", 7, "Invalid unquoted selector token")
                assertParseFailure("@e[nbt={list:[1 2]}]", 16, "Expected ','")
                assertParseFailure("@e[nbt={id:minecraft:stone}]", 20, "Invalid unquoted SNBT token")
                listOf(
                    "@e[nbt={Data:[B;128b]}]" to "128b",
                    "@e[nbt={Data:[I;2147483648]}]" to "2147483648",
                    "@e[nbt={Data:[L;9223372036854775808L]}]" to "9223372036854775808L",
                ).forEach { (source, invalidValue) ->
                    assertParseFailure(source, source.indexOf(invalidValue), "Invalid")
                }
                assertParseFailure("@e[scores={kills}]", 16, "Expected '='")
                assertParseFailure(
                    "@e[advancements={minecraft:story/root=maybe}]",
                    38,
                    "Expected 'true' or 'false'",
                )
            }

            "rejects arguments unavailable to a selector head" {
                assertParseFailure("@a[type=minecraft:player]", 3, "does not support 'type'")
                assertParseFailure("@s[limit=1]", 3, "does not support 'limit'")
                assertParseFailure("@s[sort=nearest]", 3, "does not support 'sort'")
            }

            "keeps the raw selector escape hatch unchanged" {
                entitySelector("@future[unknown=value]").asString() shouldBe "@future[unknown=value]"
            }
        },
    )

private fun assertParseFailure(
    source: String,
    offset: Int,
    message: String,
) {
    val failure = shouldThrow<EntitySelectorParseException> { parseEntitySelector(source) }
    failure.offset shouldBe offset
    failure.message shouldContain message
}
