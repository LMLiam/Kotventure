package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.selector.shouldRenderAs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EntitySelectorCoordinateTest :
    StringSpec(
        {
            "origin and volume render full and partial coordinates" {
                entities {
                    origin(1.5.x, 64.y, (-2).z)
                    volume(0.dx, (-3.5).dy, 4.dz)
                } shouldRenderAs "@e[x=1.5,y=64,z=-2,dx=0,dy=-3.5,dz=4]"

                allPlayers {
                    origin(80.y)
                    volume(0.dx, (-2).dz)
                } shouldRenderAs "@a[y=80,dx=0,dz=-2]"
            }

            "origin and volume share the selector coordinate model" {
                originCoordinate(SelectorCoordinate.X, 1).coordinate shouldBe SelectorCoordinate.X
                volumeDelta(SelectorCoordinate.DZ, 2).coordinate shouldBe SelectorCoordinate.DZ
            }

            "origin and volume compose across disjoint axes" {
                entities {
                    origin(1.x, 2.y)
                    origin(4.z)
                    volume(5.dx)
                    volume(6.dy, 7.dz)
                } shouldRenderAs "@e[x=1,y=2,z=4,dx=5,dy=6,dz=7]"
            }

            "rebinding an origin or volume axis is rejected" {
                shouldThrow<IllegalStateException> {
                    entities {
                        origin(1.x, 2.y)
                        origin(3.y)
                    }
                }

                shouldThrow<IllegalStateException> {
                    entities {
                        volume(6.dy)
                        volume((-6).dy)
                    }
                }

                shouldThrow<IllegalStateException> {
                    entities {
                        origin(1.x, 2.x)
                    }
                }
            }

            "finite coordinates render without unsupported exponent notation" {
                entities {
                    origin((1e20).x, (1e-7).z)
                } shouldRenderAs "@e[x=100000000000000000000,z=0.0000001]"
            }

            "non-finite coordinates are rejected at construction" {
                shouldThrow<IllegalArgumentException> {
                    entities { origin(Double.POSITIVE_INFINITY.z) }
                }

                shouldThrow<IllegalArgumentException> {
                    entities { volume(Double.NaN.dy) }
                }
            }

            "failed coordinate updates do not partially mutate the selector" {
                val selector =
                    entities {
                        origin(1.x, 2.y)
                        shouldThrow<IllegalArgumentException> {
                            origin(9.x, Double.NaN.z)
                        }
                        volume(3.dx, 4.dy)
                        shouldThrow<IllegalArgumentException> {
                            volume(8.dx, Double.POSITIVE_INFINITY.dz)
                        }
                    }

                selector shouldRenderAs "@e[x=1,y=2,dx=3,dy=4]"
            }

            "origin arguments are compile-time checked" {
                assertDoesNotCompile(
                    "InvalidOriginTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidOrigin() {
                        entities {
                            origin()
                            origin(16.dx)
                        }
                    }
                    """.trimIndent(),
                    "No value passed for parameter 'first'",
                    "Argument type mismatch",
                )
            }
        },
    )
