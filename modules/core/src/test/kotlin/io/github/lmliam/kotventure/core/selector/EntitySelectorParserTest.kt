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
                    buildString {
                        append("@e[")
                        append("type=!#my_pack:hostile,")
                        append("name=\"Boss Mob\",")
                        append("x=1.5,y=-2,z=3,dx=0,dy=1,dz=-1,")
                        append("distance=..10,x_rotation=170..-170,y_rotation=-45..45,")
                        append("level=1..30,gamemode=!creative,limit=2,sort=nearest,")
                        append("tag=!,tag=!hidden,team=!red,team=!blue,")
                        append("nbt={Tags:[\"boss\"],Data:[I;1,2]},")
                        append("nbt={Health:20.0f},")
                        append("scores={kills=5,balance=-10..},")
                        append("predicate=!my_pack:hidden,")
                        append("predicate=my_pack:other,")
                        append("advancements={minecraft:story/root=true,my_pack:secret={found_item=false}}")
                        append(']')
                    }

                source.shouldBeCanonicalSelector()
            }

            "canonicalizes empty argument lists" {
                parseSelector("@e[]") shouldRenderAs "@e"
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

            "rejects duplicate singleton arguments" {
                "@e[limit=1," shouldFailToParseAt "limit=2]"
                "@e[x=1," shouldFailToParseAt "x=2]"
                "@e[distance=..5," shouldFailToParseAt "distance=1..]"
                "@e[sort=nearest," shouldFailToParseAt "sort=furthest]"
            }

            "allows repeated filter-group arguments" {
                "@e[tag=a,tag=b,nbt={},nbt=!{}]".shouldBeCanonicalSelector()
            }

            "allows multiple exclusive exclusions without a positive" {
                "@e[name=!a,name=!b,type=!minecraft:zombie,type=!minecraft:skeleton]".shouldBeCanonicalSelector()
                "@a[gamemode=!survival,gamemode=!creative,team=!red,team=!blue]".shouldBeCanonicalSelector()
                "@e[team=!,team=!red]".shouldBeCanonicalSelector()
            }

            "rejects two positive exclusive filter-group values" {
                "@e[name=a," shouldFailToParseAt "name=b]"
                "@e[type=minecraft:zombie," shouldFailToParseAt "type=minecraft:skeleton]"
                "@a[gamemode=survival," shouldFailToParseAt "gamemode=creative]"
                "@e[team=red," shouldFailToParseAt "team=blue]"
                "@e[team=," shouldFailToParseAt "team=red]"
            }

            "rejects exclusive positive mixed with exclusions" {
                "@e[name=a," shouldFailToParseAt "name=!b]"
                "@e[name=!a," shouldFailToParseAt "name=b]"
                "@e[type=minecraft:zombie," shouldFailToParseAt "type=!minecraft:skeleton]"
                "@e[type=!minecraft:zombie," shouldFailToParseAt "type=minecraft:skeleton]"
                "@a[gamemode=survival," shouldFailToParseAt "gamemode=!creative]"
                "@a[gamemode=!survival," shouldFailToParseAt "gamemode=creative]"
                "@e[team=red," shouldFailToParseAt "team=!blue]"
                "@e[team=!red," shouldFailToParseAt "team=blue]"
                "@e[team=," shouldFailToParseAt "team=!red]"
                "@e[team=!," shouldFailToParseAt "team=red]"
            }
        },
    )
