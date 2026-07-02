package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.test.selector.shouldBeCanonicalSelector
import io.github.lmliam.kotventure.test.selector.shouldFailToParseAt
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SelectorScalarArgumentParsingTest :
    StringSpec(
        {
            "round trips coordinate, range, and result-control arguments" {
                val source =
                    "@e[" +
                        "x=1.5,y=-2,z=3,dx=0,dy=1,dz=-1," +
                        "distance=..10,x_rotation=170..-170,y_rotation=-45..45," +
                        "level=1..30,limit=2,sort=nearest" +
                        "]"

                source.shouldBeCanonicalSelector()
            }

            "exposes parsed scalar structure" {
                val parsed = entitySelector("@e[x=1.5,limit=2,sort=nearest]")

                parsed.arguments shouldBe
                    listOf(
                        EntitySelectorArgument.Coordinate(SelectorCoordinate.X, 1.5),
                        EntitySelectorArgument.Limit(2),
                        EntitySelectorArgument.Sort(SelectorSort.NEAREST),
                    )
            }

            "rejects malformed coordinate values" {
                "@e[x=" shouldFailToParseAt "NaN]"
                "@e[x=" shouldFailToParseAt "1..2]"
            }

            "rejects malformed range bounds" {
                "@e[distance=" shouldFailToParseAt "..]"
                "@e[distance=.." shouldFailToParseAt "-1]"
                "@e[distance=10.." shouldFailToParseAt "1]"
                "@e[x_rotation=1.." shouldFailToParseAt ".2]"
                "@e[level=" shouldFailToParseAt "-1]"
                "@e[level=5.." shouldFailToParseAt "2]"
            }

            "rejects malformed result controls" {
                "@e[limit=" shouldFailToParseAt "0]"
                "@e[sort=" shouldFailToParseAt "closest]"
            }
        },
    )
