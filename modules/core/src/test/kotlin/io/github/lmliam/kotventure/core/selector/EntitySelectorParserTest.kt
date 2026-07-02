package io.github.lmliam.kotventure.core.selector

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EntitySelectorParserTest :
    StringSpec(
        {
            "parses and renders all six selector heads" {
                listOf("@p", "@a", "@r", "@s", "@e", "@n").forEach { source ->
                    entitySelector(source).asString() shouldBe source
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

                entitySelector(source).asString() shouldBe source
            }

            "canonicalizes empty argument lists" {
                entitySelector("@e[]").asString() shouldBe "@e"
            }

            "reports malformed heads, delimiters, and unsupported arguments with source offsets" {
                assertParseFailure("e", 0, "Expected '@'")
                assertParseFailure("@q", 1, "Unsupported selector head")
                assertParseFailure("@entity", 1, "Unsupported selector head")
                assertParseFailure("@e[unknown=value]", 3, "Unsupported selector argument 'unknown'")
                assertParseFailure("@e[tag=admin", 12, "Expected ']'")
            }

            "rejects arguments unavailable to a selector head" {
                assertParseFailure("@a[type=minecraft:player]", 3, "does not support 'type'")
                assertParseFailure("@s[limit=1]", 3, "does not support 'limit'")
                assertParseFailure("@s[sort=nearest]", 3, "does not support 'sort'")
            }
        },
    )
