package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.test.selector.shouldRenderAs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class EntitySelectorStringFilterTest :
    StringSpec(
        {
            "entities with type and limit" {
                entities {
                    type("armor_stand")
                    limit(1)
                } shouldRenderAs "@e[type=minecraft:armor_stand,limit=1]"
            }

            "type with Adventure Key" {
                entities { type(key("minecraft", "creeper")) } shouldRenderAs "@e[type=minecraft:creeper]"
            }

            "type with already prefixed namespace does not double-prefix" {
                entities { type("minecraft:zombie") } shouldRenderAs "@e[type=minecraft:zombie]"
            }

            "type with custom namespace is preserved" {
                entities { type("mymod:custom_entity") } shouldRenderAs "@e[type=mymod:custom_entity]"
            }

            "type with an invalid key is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { type("Bad Key") }
                }
            }

            "name with special characters is quoted" {
                nearestPlayer { name("Player, [Admin]") } shouldRenderAs "@p[name=\"Player, [Admin]\"]"
            }

            "name with quotes is escaped" {
                nearestPlayer { name("Bob's \"Special\" Name") } shouldRenderAs
                        "@p[name=\"Bob's \\\"Special\\\" Name\"]"
            }

            "name without special characters is not quoted" {
                nearestPlayer { name("SimplePlayer") } shouldRenderAs "@p[name=SimplePlayer]"
            }

            "name outside Brigadier's unquoted character set is quoted" {
                nearestPlayer { name("namespace:value") } shouldRenderAs "@p[name=\"namespace:value\"]"
            }

            "name with backslash and special chars is escaped" {
                nearestPlayer { name("path\\to [file]") } shouldRenderAs "@p[name=\"path\\\\to [file]\"]"
            }

            "allPlayers with tag" {
                allPlayers { tag("admin") } shouldRenderAs "@a[tag=admin]"
            }

            "tag filters support presence and mixed repeatable forms" {
                allPlayers {
                    tag(any)
                    tag("vip")
                    !tag("muted")
                    tag(none)
                } shouldRenderAs "@a[tag=!,tag=vip,tag=!muted,tag=]"
            }

            "multiple tags are all preserved" {
                entities {
                    tag("admin")
                    tag("vip")
                } shouldRenderAs "@e[tag=admin,tag=vip]"
            }

            "team filter with named team" {
                allPlayers { team("red") } shouldRenderAs "@a[team=red]"
            }

            "team exclusions accumulate" {
                entities {
                    !team("red")
                    !team("blue")
                } shouldRenderAs "@e[team=!red,team=!blue]"
            }

            "team presence renders vanilla forms" {
                entities { team(any) } shouldRenderAs "@e[team=!]"
                entities { team(none) } shouldRenderAs "@e[team=]"
            }

            "team presence combines with named exclusions" {
                entities {
                    team(any)
                    !team("red")
                } shouldRenderAs "@e[team=!,team=!red]"
            }

            "team is available on the self scope" {
                self { team("red") } shouldRenderAs "@s[team=red]"
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
        },
    )
