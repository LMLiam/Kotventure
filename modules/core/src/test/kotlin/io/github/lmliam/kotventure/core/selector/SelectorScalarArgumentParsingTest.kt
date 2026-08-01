package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.test.selector.shouldBeCanonicalSelector
import io.github.lmliam.kotventure.test.selector.shouldFailToParseAt
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SelectorScalarArgumentParsingTest :
    StringSpec(
        {
            "round trips coordinate, range, and result-control arguments" {
                """@e[x=1.5,y=-2,z=3,dx=0,dy=1,dz=-1,distance=..10,x_rotation=170..-170,y_rotation=-45..45,level=1..30,limit=2,sort=nearest]"""
                    .shouldBeCanonicalSelector()
            }

            "exposes parsed scalar structure" {
                parseSelector("@e[x=1.5,limit=2,sort=nearest]").arguments shouldBe
                        listOf(
                            EntitySelectorArgument.Coordinate(SelectorCoordinate.X, 1.5),
                            EntitySelectorArgument.Limit(2),
                            EntitySelectorArgument.Sort(SelectorSort.NEAREST),
                        )
            }

            "parses decimal points as part of floating-point range bounds" {
                val parsed =
                    parseSelector("@e[distance=1.5,x_rotation=1.5..2.5,y_rotation=..2.5]")
                val ranges = parsed.arguments.filterIsInstance<EntitySelectorArgument.Range>()

                ranges.size shouldBe 3
                ranges[0].range.minimum shouldBe 1.5
                ranges[0].range.maximum shouldBe 1.5
                ranges[1].range.minimum shouldBe 1.5
                ranges[1].range.maximum shouldBe 2.5
                ranges[2].range.minimum shouldBe null
                ranges[2].range.maximum shouldBe 2.5
            }

            "parses open-ended and negative decimal floating-point bounds" {
                val openAbove =
                    parseSelector("@e[distance=1.5..]")
                        .arguments
                        .filterIsInstance<EntitySelectorArgument.Range>()
                        .single()
                        .range
                val negative =
                    parseSelector("@e[x_rotation=-1.5..-0.25]")
                        .arguments
                        .filterIsInstance<EntitySelectorArgument.Range>()
                        .single()
                        .range

                openAbove.minimum shouldBe 1.5
                openAbove.maximum shouldBe null
                negative.minimum shouldBe -1.5
                negative.maximum shouldBe -0.25
            }

            "parses integer range separators independently from decimal bounds" {
                val parsed = parseSelector("@e[level=1..2,scores={kills=-1..}]")
                val level =
                    parsed.arguments
                        .filterIsInstance<EntitySelectorArgument.Level>()
                        .single()
                        .range
                val score =
                    parsed.arguments
                        .filterIsInstance<EntitySelectorArgument.Scores>()
                        .single()
                        .scores
                        .single()
                        .range

                level.minimum shouldBe 1
                level.maximum shouldBe 2
                score.minimum shouldBe -1
                score.maximum shouldBe null
            }

            "rejects repeated range separators instead of accepting a valid prefix" {
                "@e[distance=1.." shouldFailToParseAt ".2]"
                "@e[distance=1.." shouldFailToParseAt "..2]"
                "@e[distance=1..2" shouldFailToParseAt "..3]"
                "@e[distance=..1" shouldFailToParseAt "..2]"
                "@e[distance=" shouldFailToParseAt "1.2.3]"
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
