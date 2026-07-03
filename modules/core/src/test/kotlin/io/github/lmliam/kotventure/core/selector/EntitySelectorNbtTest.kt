package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.list
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.selector.shouldRenderAs
import io.kotest.core.spec.style.StringSpec

class EntitySelectorNbtTest :
    StringSpec(
        {
            "NBT filters preserve positive and negated call order" {
                entities {
                    nbt {
                        "Health" eq 20.0f
                        "Tags" eq list("boss", "hostile")
                    }
                    !nbt { "Invisible" eq true }
                    nbt {}
                } shouldRenderAs "@e[nbt={Health:20.0f,Tags:[\"boss\",\"hostile\"]},nbt=!{Invisible:1b},nbt={}]"
            }

            "NBT filters are available on every selector head" {
                fun CommonEntitySelectorScope.bothPolarities() {
                    nbt {}
                    !nbt {}
                }

                nearestPlayer { bothPolarities() } shouldRenderAs "@p[nbt={},nbt=!{}]"
                allPlayers { bothPolarities() } shouldRenderAs "@a[nbt={},nbt=!{}]"
                randomPlayer { bothPolarities() } shouldRenderAs "@r[nbt={},nbt=!{}]"
                self { bothPolarities() } shouldRenderAs "@s[nbt={},nbt=!{}]"
                entities { bothPolarities() } shouldRenderAs "@e[nbt={},nbt=!{}]"
                nearestEntity { bothPolarities() } shouldRenderAs "@n[nbt={},nbt=!{}]"
            }

            "raw SNBT strings do not compile as selector NBT filters" {
                assertDoesNotCompile(
                    "RawSelectorNbtFilterTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun rawNbtFilter() {
                        entities {
                            nbt("{Health:20f}")
                        }
                    }
                    """.trimIndent(),
                    "Argument type mismatch",
                )
            }

            "NBT filters reuse nested compound array list and escaping rules" {
                self {
                    nbt {
                        "display name" eq "say \"hello\""
                        "nested" eq {
                            "bytes" eq byteArrayOf(1, 2)
                            "ints" eq intArrayOf(3, 4)
                            "longs" eq longArrayOf(5L, 6L)
                            "rows" eq list(list(7, 8), list(9, 10))
                        }
                    }
                } shouldRenderAs
                        "@s[nbt={\"display name\":\"say \\\"hello\\\"\",nested:" +
                        "{bytes:[B;1b,2b],ints:[I;3,4],longs:[L;5L,6L],rows:[[7,8],[9,10]]}}]"
            }
        },
    )
