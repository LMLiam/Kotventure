package io.github.lmliam.kotventure.core.selector

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

                entitySelector(source).asString() shouldBe source
            }

            "rejects malformed coordinate values" {
                assertParseFailure("@e[x=NaN]", 5, "finite decimal")
                assertParseFailure("@e[x=1..2]", 5, "finite decimal")
            }

            "rejects malformed range bounds" {
                assertParseFailure("@e[distance=..]", 12, "at least one bound")
                assertParseFailure("@e[distance=..-1]", 14, "non-negative")
                assertParseFailure("@e[distance=10..1]", 16, "must not exceed")
                assertParseFailure("@e[x_rotation=1...2]", 17, "more than one")
                assertParseFailure("@e[level=-1]", 9, "non-negative")
                assertParseFailure("@e[level=5..2]", 12, "must not exceed")
            }

            "rejects malformed result controls" {
                assertParseFailure("@e[limit=0]", 9, "positive")
                assertParseFailure("@e[sort=closest]", 8, "Unsupported selector sort")
            }
        },
    )
