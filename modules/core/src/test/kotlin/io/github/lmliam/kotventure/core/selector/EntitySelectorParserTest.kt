package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.test.selector.shouldBeCanonicalSelector
import io.github.lmliam.kotventure.test.selector.shouldFailToParseAt
import io.github.lmliam.kotventure.test.selector.shouldRenderAs
import io.kotest.core.spec.style.StringSpec

class EntitySelectorParserTest :
    StringSpec(
        {
            "parses and renders all six selector heads" {
                listOf("@p", "@a", "@r", "@s", "@e", "@n").forEach { source ->
                    source.shouldBeCanonicalSelector()
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

                source.shouldBeCanonicalSelector()
            }

            "canonicalizes empty argument lists" {
                entitySelector("@e[]") shouldRenderAs "@e"
            }

            "reports malformed heads, delimiters, and unsupported arguments at their source offset" {
                "" shouldFailToParseAt "e"
                "@" shouldFailToParseAt "q"
                "@" shouldFailToParseAt "entity"
                "@e[" shouldFailToParseAt "unknown=value]"
                "@e[tag=admin" shouldFailToParseAt ""
            }

            "rejects arguments unavailable to a selector head" {
                "@a[" shouldFailToParseAt "type=minecraft:player]"
                "@s[" shouldFailToParseAt "limit=1]"
                "@s[" shouldFailToParseAt "sort=nearest]"
            }
        },
    )
