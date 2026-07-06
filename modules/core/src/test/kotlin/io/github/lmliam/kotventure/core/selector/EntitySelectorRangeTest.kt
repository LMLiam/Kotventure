package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.test.selector.shouldRenderAs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class EntitySelectorRangeTest :
    StringSpec(
        {
            "nearestPlayer with typed distance range" {
                nearestPlayer { distance(atMost(10.0)) } shouldRenderAs "@p[distance=..10]"
            }

            "distance with Kotlin range" {
                nearestPlayer { distance(5.0..20.0) } shouldRenderAs "@p[distance=5..20]"
            }

            "distance with atLeast" {
                entities { distance(atLeast(3.0)) } shouldRenderAs "@e[distance=3..]"
            }

            "distance with exactly" {
                entities { distance(exactly(5.0)) } shouldRenderAs "@e[distance=5]"
            }

            "distance ranges render without unsupported exponent notation" {
                entities { distance(exactly(1e20)) } shouldRenderAs "@e[distance=100000000000000000000]"
                entities { distance(atMost(1e-7)) } shouldRenderAs "@e[distance=..0.0000001]"
            }

            "distance with inverted Kotlin range is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { distance(10.0..1.0) }
                }
            }

            "distance with non-finite Kotlin range is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { distance(1.0..Double.POSITIVE_INFINITY) }
                }
            }

            "distance with negative bounds is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { distance(atLeast(-1.0)) }
                }
                shouldThrow<IllegalArgumentException> {
                    entities { distance(-5.0..5.0) }
                }
            }

            "distance with equal Kotlin range bounds renders as exact value" {
                entities { distance(5.0..5.0) } shouldRenderAs "@e[distance=5]"
            }

            "pitch with typed range" {
                entities { pitch(atMost(-45.0)) } shouldRenderAs "@e[x_rotation=..-45]"
            }

            "yaw with typed range" {
                allPlayers { yaw(atLeast(90.0)) } shouldRenderAs "@a[y_rotation=90..]"
            }

            "pitch with exact value" {
                entities { pitch(exactly(0.0)) } shouldRenderAs "@e[x_rotation=0]"
            }

            "pitch and yaw with Kotlin ranges render in vanilla order" {
                entities {
                    yaw(0.0..90.0)
                    distance(atMost(10.0))
                    pitch(-90.0..-45.0)
                } shouldRenderAs "@e[distance=..10,x_rotation=-90..-45,y_rotation=0..90]"
            }

            "yaw with descending Kotlin range renders vanilla wrap-around" {
                entities { yaw(170.0..-170.0) } shouldRenderAs "@e[y_rotation=170..-170]"
            }

            "rotation is available on the self scope" {
                self { pitch(atMost(0.0)) } shouldRenderAs "@s[x_rotation=..0]"
            }

            "rotation with non-finite bounds is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { pitch(Double.NaN..0.0) }
                }

                shouldThrow<IllegalArgumentException> {
                    entities { yaw(0.0..Double.POSITIVE_INFINITY) }
                }
            }

            "level with Kotlin IntRange" {
                allPlayers { level(5..30) } shouldRenderAs "@a[level=5..30]"
            }

            "level with open-ended range" {
                allPlayers { level(atLeast(10)) } shouldRenderAs "@a[level=10..]"
            }

            "level with atMost bound" {
                allPlayers { level(atMost(30)) } shouldRenderAs "@a[level=..30]"
            }

            "level with exact value" {
                allPlayers { level(exactly(5)) } shouldRenderAs "@a[level=5]"
            }

            "level with inverted IntRange is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { level(5..1) }
                }
            }

            "level with a negative bound is rejected" {
                shouldThrow<IllegalArgumentException> {
                    allPlayers { level(atLeast(-1)) }
                }
                shouldThrow<IllegalArgumentException> {
                    allPlayers { level(atMost(-1)) }
                }
                shouldThrow<IllegalArgumentException> {
                    allPlayers { level(-5..5) }
                }
            }

            "level with an exact closed range collapses to the exact form" {
                allPlayers { level(5..5) } shouldRenderAs "@a[level=5]"
            }

            "duplicate range arguments are rejected" {
                shouldThrow<IllegalStateException> {
                    entities {
                        distance(atMost(1.0))
                        distance(atLeast(2.0))
                    }
                }
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        level(exactly(3))
                        level(5..10)
                    }
                }
                shouldThrow<IllegalStateException> {
                    entities {
                        pitch(atMost(0.0))
                        pitch(exactly(45.0))
                    }
                }
                shouldThrow<IllegalStateException> {
                    entities {
                        yaw(0.0..90.0)
                        yaw(atLeast(90.0))
                    }
                }
            }

            "atMost rejects non-finite values" {
                shouldThrow<IllegalArgumentException> {
                    atMost(Double.NaN)
                }
                shouldThrow<IllegalArgumentException> {
                    atMost(Double.POSITIVE_INFINITY)
                }
                shouldThrow<IllegalArgumentException> {
                    atMost(Double.NEGATIVE_INFINITY)
                }
            }

            "atLeast rejects non-finite values" {
                shouldThrow<IllegalArgumentException> {
                    atLeast(Double.NaN)
                }
                shouldThrow<IllegalArgumentException> {
                    atLeast(Double.POSITIVE_INFINITY)
                }
                shouldThrow<IllegalArgumentException> {
                    atLeast(Double.NEGATIVE_INFINITY)
                }
            }

            "exactly rejects non-finite values" {
                shouldThrow<IllegalArgumentException> {
                    exactly(Double.NaN)
                }
                shouldThrow<IllegalArgumentException> {
                    exactly(Double.POSITIVE_INFINITY)
                }
                shouldThrow<IllegalArgumentException> {
                    exactly(Double.NEGATIVE_INFINITY)
                }
            }
        },
    )
