package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.nbt.list
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EntitySelectorTest :
    StringSpec(
        {
            "self returns @s" {
                self().asString() shouldBe "@s"
            }

            "nearestPlayer with no arguments returns @p" {
                nearestPlayer().asString() shouldBe "@p"
            }

            "allPlayers with no arguments returns @a" {
                allPlayers().asString() shouldBe "@a"
            }

            "randomPlayer with no arguments returns @r" {
                randomPlayer().asString() shouldBe "@r"
            }

            "entities with no arguments returns @e" {
                entities().asString() shouldBe "@e"
            }

            "nearestEntity with no arguments returns @n" {
                nearestEntity().asString() shouldBe "@n"
            }

            "self accepts common and entity type arguments" {
                self {
                    type("minecraft:zombie")
                    name("Boss")
                }.asString() shouldBe "@s[type=minecraft:zombie,name=Boss]"
            }

            "player selector scopes do not expose entity type" {
                assertDoesNotCompile(
                    "PlayerSelectorTypeTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidPlayerSelector() {
                        nearestPlayer {
                            type("minecraft:zombie")
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'type'",
                )
            }

            "player selector scopes retain limit and sort" {
                val selector =
                    nearestPlayer {
                        limit(3)
                        sort(nearest)
                    }

                selector.asString() shouldBe "@p[limit=3,sort=nearest]"
            }

            "self selector scope does not expose limit or sort" {
                assertDoesNotCompile(
                    "SelfSelectorLimitTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidSelfSelector() {
                        self {
                            limit(1)
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'limit'",
                )
                assertDoesNotCompile(
                    "SelfSelectorSortTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidSelfSelector() {
                        self {
                            sort(nearest)
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'sort'",
                )
            }

            "entities with type and limit" {
                val selector =
                    entities {
                        type("armor_stand")
                        limit(1)
                    }

                selector.asString() shouldBe "@e[type=minecraft:armor_stand,limit=1]"
            }

            "nearestPlayer with typed distance range" {
                val selector = nearestPlayer { distance(atMost(10.0)) }

                selector.asString() shouldBe "@p[distance=..10]"
            }

            "allPlayers with tag" {
                val selector = allPlayers { tag("admin") }

                selector.asString() shouldBe "@a[tag=admin]"
            }

            "entities with name and scoped sort constant" {
                val selector =
                    entities {
                        name("Dinnerbone")
                        sort(nearest)
                    }

                selector.asString() shouldBe "@e[name=Dinnerbone,sort=nearest]"
            }

            "distance with Kotlin range" {
                val selector = nearestPlayer { distance(5.0..20.0) }

                selector.asString() shouldBe "@p[distance=5..20]"
            }

            "distance with atLeast" {
                val selector = entities { distance(atLeast(3.0)) }

                selector.asString() shouldBe "@e[distance=3..]"
            }

            "distance with exactly" {
                val selector = entities { distance(exactly(5.0)) }

                selector.asString() shouldBe "@e[distance=5]"
            }

            "distance ranges render without unsupported exponent notation" {
                entities {
                    distance(exactly(1e20))
                }.asString() shouldBe "@e[distance=100000000000000000000]"

                entities {
                    distance(atMost(1e-7))
                }.asString() shouldBe "@e[distance=..0.0000001]"
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
                entities { distance(5.0..5.0) }.asString() shouldBe "@e[distance=5]"
            }

            "pitch with typed range" {
                entities { pitch(atMost(-45.0)) }.asString() shouldBe "@e[x_rotation=..-45]"
            }

            "yaw with typed range" {
                allPlayers { yaw(atLeast(90.0)) }.asString() shouldBe "@a[y_rotation=90..]"
            }

            "pitch with exact value" {
                entities { pitch(exactly(0.0)) }.asString() shouldBe "@e[x_rotation=0]"
            }

            "pitch and yaw with Kotlin ranges render in vanilla order" {
                entities {
                    yaw(0.0..90.0)
                    distance(atMost(10.0))
                    pitch(-90.0..-45.0)
                }.asString() shouldBe "@e[distance=..10,x_rotation=-90..-45,y_rotation=0..90]"
            }

            "yaw with descending Kotlin range renders vanilla wrap-around" {
                entities { yaw(170.0..-170.0) }.asString() shouldBe "@e[y_rotation=170..-170]"
            }

            "rotation is available on the self scope" {
                self { pitch(atMost(0.0)) }.asString() shouldBe "@s[x_rotation=..0]"
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
                val selector = allPlayers { level(5..30) }

                selector.asString() shouldBe "@a[level=5..30]"
            }

            "level with open-ended range" {
                val selector = allPlayers { level(atLeast(10)) }

                selector.asString() shouldBe "@a[level=10..]"
            }

            "level with atMost bound" {
                val selector = allPlayers { level(atMost(30)) }

                selector.asString() shouldBe "@a[level=..30]"
            }

            "level with exact value" {
                val selector = allPlayers { level(exactly(5)) }

                selector.asString() shouldBe "@a[level=5]"
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
                allPlayers { level(5..5) }.asString() shouldBe "@a[level=5]"
            }

            "scores render exact open closed and negative ranges in declaration order" {
                val selector =
                    entities {
                        scores {
                            "kills" eq exactly(5)
                            "level_up" eq atLeast(1)
                            "deaths" eq atMost(3)
                            "balance" eq -10..-1
                            "progress" eq atLeast(-5)
                        }
                    }

                selector.asString() shouldBe
                        "@e[scores={kills=5,level_up=1..,deaths=..3,balance=-10..-1,progress=-5..}]"
            }

            "an exact closed score range collapses to the exact form" {
                allPlayers { scores { "kills" eq 5..5 } }.asString() shouldBe "@a[scores={kills=5}]"
            }

            "an empty scores block renders an empty map" {
                entities { scores {} }.asString() shouldBe "@e[scores={}]"
            }

            "scores are available on the self scope" {
                self { scores { "kills" eq exactly(1) } }.asString() shouldBe "@s[scores={kills=1}]"
            }

            "repeated score objectives are rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        scores {
                            "kills" eq atLeast(10)
                            "kills" eq exactly(5)
                        }
                    }
                }
            }

            "a duplicate scores block is rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        scores { "kills" eq exactly(5) }
                        scores { "deaths" eq exactly(0) }
                    }
                }
            }

            "score objectives may use every allowed unquoted-token punctuation class" {
                entities { scores { "obj_1.kills-total+x" eq exactly(1) } }.asString() shouldBe
                        "@e[scores={obj_1.kills-total+x=1}]"
            }

            "a scores block cannot be negated" {
                assertDoesNotCompile(
                    "NegatedScoresTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun negatedScores() {
                        entities {
                            !scores { "kills" eq exactly(5) }
                        }
                    }
                    """.trimIndent(),
                    "receiver type mismatch",
                )
            }

            "score objectives outside vanilla's unquoted-token syntax are rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { scores { "bad name" eq exactly(1) } }
                }
                shouldThrow<IllegalArgumentException> {
                    entities { scores { "" eq exactly(1) } }
                }
            }

            "a score with an inverted IntRange is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { scores { "kills" eq 5..1 } }
                }
            }

            "advancements render completion and criterion conditions in declaration order" {
                val selector =
                    allPlayers {
                        advancements {
                            key("minecraft", "story/smelt_iron") eq true
                            key("my_pack", "boss") eq {
                                "kill_dragon" eq true
                                "no_deaths" eq false
                            }
                            key("my_pack", "secret") eq false
                        }
                    }

                selector.asString() shouldBe
                        "@a[advancements={minecraft:story/smelt_iron=true," +
                        "my_pack:boss={kill_dragon=true,no_deaths=false},my_pack:secret=false}]"
            }

            "an empty advancement criterion block renders the valid vanilla form" {
                allPlayers { advancements { key("my_pack", "boss") eq {} } }.asString() shouldBe
                        "@a[advancements={my_pack:boss={}}]"
            }

            "an empty advancements block renders an empty map" {
                entities { advancements {} }.asString() shouldBe "@e[advancements={}]"
            }

            "advancements are available on the self scope" {
                self { advancements { key("minecraft", "story/root") eq true } }.asString() shouldBe
                        "@s[advancements={minecraft:story/root=true}]"
            }

            "repeated advancements are rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        advancements {
                            key("my_pack", "boss") eq true
                            key("my_pack", "boss") eq { "kill_dragon" eq true }
                        }
                    }
                }
            }

            "a duplicate advancements block is rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        advancements { key("my_pack", "boss") eq true }
                        advancements { key("my_pack", "secret") eq false }
                    }
                }
            }

            "repeated advancement criteria are rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        advancements {
                            key("my_pack", "boss") eq {
                                "kill_dragon" eq true
                                "kill_dragon" eq false
                            }
                        }
                    }
                }
            }

            "advancement criteria outside vanilla's unquoted-token syntax are rejected" {
                shouldThrow<IllegalArgumentException> {
                    allPlayers { advancements { key("my_pack", "boss") eq { "bad name" eq true } } }
                }
                shouldThrow<IllegalArgumentException> {
                    allPlayers { advancements { key("my_pack", "boss") eq { "" eq true } } }
                }
            }

            "an advancements block cannot be negated" {
                assertDoesNotCompile(
                    "NegatedAdvancementsTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.key.key
                    import io.github.lmliam.kotventure.core.selector.*

                    fun negatedAdvancements() {
                        entities {
                            !advancements { key("my_pack", "boss") eq true }
                        }
                    }
                    """.trimIndent(),
                    "receiver type mismatch",
                )
            }

            "type with Adventure Key" {
                val selector = entities { type(key("minecraft", "creeper")) }

                selector.asString() shouldBe "@e[type=minecraft:creeper]"
            }

            "gamemode filter with typed constant" {
                val selector = allPlayers { gamemode(survival) }

                selector.asString() shouldBe "@a[gamemode=survival]"
            }

            "gamemode with all constant variants" {
                allPlayers { gamemode(survival) }.asString() shouldBe "@a[gamemode=survival]"
                allPlayers { gamemode(creative) }.asString() shouldBe "@a[gamemode=creative]"
                allPlayers { gamemode(adventure) }.asString() shouldBe "@a[gamemode=adventure]"
                allPlayers { gamemode(spectator) }.asString() shouldBe "@a[gamemode=spectator]"
            }

            "negated filters accumulate in call order" {
                val selector =
                    entities {
                        !type(key("minecraft", "zombie"))
                        !typeTag(key("minecraft", "raiders"))
                        !name("Boss")
                        !name("Boss Mob")
                        !gamemode(survival)
                        !gamemode(creative)
                    }

                selector.asString() shouldBe
                        "@e[type=!minecraft:zombie,type=!#minecraft:raiders,name=!Boss,name=!\"Boss Mob\"," +
                        "gamemode=!survival,gamemode=!creative]"
            }

            "entity type tags use Adventure keys and preserve custom namespaces" {
                entities {
                    typeTag(key("mymod", "hostile"))
                }.asString() shouldBe "@e[type=#mymod:hostile]"

                self {
                    !typeTag(key("mymod", "ignored"))
                }.asString() shouldBe "@s[type=!#mymod:ignored]"
            }

            "negated string types apply the default namespace" {
                entities {
                    !type("creeper")
                }.asString() shouldBe "@e[type=!minecraft:creeper]"
            }

            "tag filters support presence and mixed repeatable forms" {
                val selector =
                    allPlayers {
                        tag(any)
                        tag("vip")
                        !tag("muted")
                        tag(none)
                    }

                selector.asString() shouldBe "@a[tag=!,tag=vip,tag=!muted,tag=]"
            }

            "team filter with named team" {
                allPlayers { team("red") }.asString() shouldBe "@a[team=red]"
            }

            "team exclusions accumulate" {
                entities {
                    !team("red")
                    !team("blue")
                }.asString() shouldBe "@e[team=!red,team=!blue]"
            }

            "team presence renders vanilla forms" {
                entities { team(any) }.asString() shouldBe "@e[team=!]"
                entities { team(none) }.asString() shouldBe "@e[team=]"
            }

            "team presence combines with named exclusions" {
                entities {
                    team(any)
                    !team("red")
                }.asString() shouldBe "@e[team=!,team=!red]"
            }

            "team is available on the self scope" {
                self { team("red") }.asString() shouldBe "@s[team=red]"
            }

            "NBT filters preserve positive and negated call order" {
                val selector =
                    entities {
                        nbt {
                            "Health" eq 20.0f
                            "Tags" eq list("boss", "hostile")
                        }
                        !nbt { "Invisible" eq true }
                        nbt {}
                    }

                selector.asString() shouldBe
                        "@e[nbt={Health:20.0f,Tags:[\"boss\",\"hostile\"]},nbt=!{Invisible:1b},nbt={}]"
            }

            "NBT filters are available on every selector head" {
                fun CommonEntitySelectorScope.bothPolarities() {
                    nbt {}
                    !nbt {}
                }

                nearestPlayer { bothPolarities() }.asString() shouldBe "@p[nbt={},nbt=!{}]"
                allPlayers { bothPolarities() }.asString() shouldBe "@a[nbt={},nbt=!{}]"
                randomPlayer { bothPolarities() }.asString() shouldBe "@r[nbt={},nbt=!{}]"
                self { bothPolarities() }.asString() shouldBe "@s[nbt={},nbt=!{}]"
                entities { bothPolarities() }.asString() shouldBe "@e[nbt={},nbt=!{}]"
                nearestEntity { bothPolarities() }.asString() shouldBe "@n[nbt={},nbt=!{}]"
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
                val selector =
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
                    }

                selector.asString() shouldBe
                        "@s[nbt={\"display name\":\"say \\\"hello\\\"\",nested:" +
                        "{bytes:[B;1b,2b],ints:[I;3,4],longs:[L;5L,6L],rows:[[7,8],[9,10]]}}]"
            }

            "predicate filters preserve positive and negated keys in call order" {
                val selector =
                    entities {
                        predicate(key("minecraft", "is_baby"))
                        !predicate(key("my_pack", "hidden"))
                        predicate(key("my_pack", "active"))
                    }

                selector.asString() shouldBe
                        "@e[predicate=minecraft:is_baby,predicate=!my_pack:hidden,predicate=my_pack:active]"
            }

            "identical predicates accumulate rather than collapse" {
                val selector =
                    entities {
                        predicate(key("my_pack", "active"))
                        predicate(key("my_pack", "active"))
                    }

                selector.asString() shouldBe "@e[predicate=my_pack:active,predicate=my_pack:active]"
            }

            "predicates are available on the self scope" {
                self { !predicate(key("my_pack", "flying")) }.asString() shouldBe "@s[predicate=!my_pack:flying]"
            }

            "raw predicate strings do not compile" {
                assertDoesNotCompile(
                    "RawPredicateTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun rawPredicate() {
                        entities {
                            predicate("my_pack:active")
                        }
                    }
                    """.trimIndent(),
                    "Argument type mismatch",
                )
            }

            "duplicate positive team filters are rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        team("red")
                        team("blue")
                    }
                }
                shouldThrow<IllegalStateException> {
                    entities {
                        team(none)
                        team("red")
                    }
                }
            }

            "mixed team polarity is rejected in both orders" {
                shouldThrow<IllegalStateException> {
                    entities {
                        team("red")
                        !team("blue")
                    }
                }
                shouldThrow<IllegalStateException> {
                    entities {
                        team(any)
                        team(none)
                    }
                }
            }

            "invalid team names are rejected" {
                shouldThrow<IllegalArgumentException> {
                    allPlayers { team("") }
                }
                shouldThrow<IllegalArgumentException> {
                    allPlayers { !team("") }
                }
                shouldThrow<IllegalArgumentException> {
                    allPlayers { team("red team") }
                }
            }

            "empty tag names are rejected" {
                shouldThrow<IllegalArgumentException> {
                    allPlayers { tag("") }
                }
                shouldThrow<IllegalArgumentException> {
                    allPlayers { !tag("") }
                }
            }

            "origin and volume render full and partial coordinates" {
                entities {
                    origin(1.5.x, 64.y, (-2).z)
                    volume(0.dx, (-3.5).dy, 4.dz)
                }.asString() shouldBe "@e[x=1.5,y=64,z=-2,dx=0,dy=-3.5,dz=4]"

                allPlayers {
                    origin(80.y)
                    volume(0.dx, (-2).dz)
                }.asString() shouldBe "@a[y=80,dx=0,dz=-2]"
            }

            "origin and volume compose across disjoint axes" {
                val selector =
                    entities {
                        origin(1.x, 2.y)
                        origin(4.z)
                        volume(5.dx)
                        volume(6.dy, 7.dz)
                    }

                selector.asString() shouldBe "@e[x=1,y=2,z=4,dx=5,dy=6,dz=7]"
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
                val selector =
                    entities {
                        origin((1e20).x, (1e-7).z)
                    }

                selector.asString() shouldBe "@e[x=100000000000000000000,z=0.0000001]"
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

                selector.asString() shouldBe "@e[x=1,y=2,dx=3,dy=4]"
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

            "duplicate singleton arguments are rejected" {
                shouldThrow<IllegalStateException> {
                    entities {
                        limit(1)
                        limit(5)
                    }
                }
                shouldThrow<IllegalStateException> {
                    entities {
                        sort(nearest)
                        sort(furthest)
                    }
                }
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

            "duplicate positive singleton filters are rejected" {
                shouldThrow<IllegalStateException> {
                    entities {
                        type("zombie")
                        type("skeleton")
                    }
                }
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        name("Alex")
                        name("Steve")
                    }
                }
            }

            "mixed filter polarity is rejected in both orders" {
                shouldThrow<IllegalStateException> {
                    entities {
                        type("zombie")
                        !type("skeleton")
                    }
                }
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        !gamemode(survival)
                        gamemode(creative)
                    }
                }
            }

            "player selector scopes do not expose negated entity type filters" {
                assertDoesNotCompile(
                    "NegatedPlayerSelectorTypeTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidPlayerSelector() {
                        allPlayers {
                            !type("zombie")
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'type'",
                )
            }

            "the exclusion operator is not available outside selector scopes" {
                assertDoesNotCompile(
                    "UnscopedExclusionOperatorTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    lateinit var expression: SelectorFilterExpression
                    val excluded = !expression
                    """.trimIndent(),
                    "Unresolved reference 'not'",
                )
            }

            "presence filters cannot be prefix-negated" {
                assertDoesNotCompile(
                    "NegatedPresenceFilterTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidPresenceFilters() {
                        entities {
                            !tag(any)
                            !team(none)
                        }
                    }
                    """.trimIndent(),
                    "receiver type mismatch",
                )
            }

            "value-wrapped negation no longer compiles" {
                assertDoesNotCompile(
                    "ValueWrappedNegationTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun oldNegationSyntax() {
                        entities {
                            tag(!"hidden")
                            type(!"zombie")
                        }
                    }
                    """.trimIndent(),
                    "Argument type mismatch",
                )
            }

            "filter expressions cannot cross selector blocks" {
                lateinit var expression: SelectorFilterExpression
                entities {
                    expression = tag("hidden")
                }

                shouldThrow<IllegalStateException> {
                    entities {
                        !expression
                    }
                }
            }

            "filter expressions cannot be negated twice" {
                shouldThrow<IllegalStateException> {
                    entities {
                        val expression = tag("hidden")
                        !expression
                        !expression
                    }
                }
            }

            "sort with all constant variants" {
                entities { sort(nearest) }.asString() shouldBe "@e[sort=nearest]"
                entities { sort(furthest) }.asString() shouldBe "@e[sort=furthest]"
                entities { sort(random) }.asString() shouldBe "@e[sort=random]"
                entities { sort(arbitrary) }.asString() shouldBe "@e[sort=arbitrary]"
            }

            "escape hatch wraps a raw string" {
                val selector = entitySelector("@e[type=zombie,limit=5,nbt={NoAI:1b}]")

                selector.asString() shouldBe "@e[type=zombie,limit=5,nbt={NoAI:1b}]"
            }

            "toString returns the selector string" {
                self().toString() shouldBe "@s"
            }

            "type with already prefixed namespace does not double-prefix" {
                val selector = entities { type("minecraft:zombie") }

                selector.asString() shouldBe "@e[type=minecraft:zombie]"
            }

            "type with custom namespace is preserved" {
                val selector = entities { type("mymod:custom_entity") }

                selector.asString() shouldBe "@e[type=mymod:custom_entity]"
            }

            "type with an invalid key is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { type("Bad Key") }
                }
            }

            "name with special characters is quoted" {
                val selector = nearestPlayer { name("Player, [Admin]") }

                selector.asString() shouldBe "@p[name=\"Player, [Admin]\"]"
            }

            "name with quotes is escaped" {
                val selector = nearestPlayer { name("Bob's \"Special\" Name") }

                selector.asString() shouldBe "@p[name=\"Bob's \\\"Special\\\" Name\"]"
            }

            "name without special characters is not quoted" {
                val selector = nearestPlayer { name("SimplePlayer") }

                selector.asString() shouldBe "@p[name=SimplePlayer]"
            }

            "name outside Brigadier's unquoted character set is quoted" {
                val selector = nearestPlayer { name("namespace:value") }

                selector.asString() shouldBe "@p[name=\"namespace:value\"]"
            }

            "atMost rejects NaN" {
                shouldThrow<IllegalArgumentException> {
                    atMost(Double.NaN)
                }
            }

            "atMost rejects positive infinity" {
                shouldThrow<IllegalArgumentException> {
                    atMost(Double.POSITIVE_INFINITY)
                }
            }

            "atMost rejects negative infinity" {
                shouldThrow<IllegalArgumentException> {
                    atMost(Double.NEGATIVE_INFINITY)
                }
            }

            "atLeast rejects NaN" {
                shouldThrow<IllegalArgumentException> {
                    atLeast(Double.NaN)
                }
            }

            "atLeast rejects infinity" {
                shouldThrow<IllegalArgumentException> {
                    atLeast(Double.POSITIVE_INFINITY)
                }
            }

            "atLeast rejects negative infinity" {
                shouldThrow<IllegalArgumentException> {
                    atLeast(Double.NEGATIVE_INFINITY)
                }
            }

            "exactly rejects NaN" {
                shouldThrow<IllegalArgumentException> {
                    exactly(Double.NaN)
                }
            }

            "exactly rejects infinity" {
                shouldThrow<IllegalArgumentException> {
                    exactly(Double.POSITIVE_INFINITY)
                }
            }

            "exactly rejects negative infinity" {
                shouldThrow<IllegalArgumentException> {
                    exactly(Double.NEGATIVE_INFINITY)
                }
            }

            "multiple tags are all preserved" {
                val selector =
                    entities {
                        tag("admin")
                        tag("vip")
                    }

                selector.asString() shouldBe "@e[tag=admin,tag=vip]"
            }

            "name with backslash and special chars is escaped" {
                val selector = nearestPlayer { name("path\\to [file]") }

                selector.asString() shouldBe "@p[name=\"path\\\\to [file]\"]"
            }

            "limit rejects zero" {
                shouldThrow<IllegalArgumentException> {
                    entities { limit(0) }
                }
            }

            "limit rejects negative" {
                shouldThrow<IllegalArgumentException> {
                    entities { limit(-1) }
                }
            }
        },
    )
