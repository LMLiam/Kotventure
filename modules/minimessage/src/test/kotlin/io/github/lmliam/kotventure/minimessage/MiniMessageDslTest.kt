package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldNotHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class MiniMessageDslTest :
    StringSpec(
        {
            "parses MiniMessage markup to an Adventure component" {
                val parsed = mini("<red>hi")

                parsed shouldContainText "hi"
                parsed shouldHaveColor NamedTextColor.RED
            }

            "applies parsed placeholders through the resolver DSL" {
                val parsed =
                    mini("<name>") {
                        parsed("name", "<gold>Alex</gold>")
                    }

                parsed shouldContainText "Alex"
                parsed shouldHaveColor NamedTextColor.GOLD
            }

            "applies unparsed placeholders without interpreting nested markup" {
                val parsed =
                    mini("<name>") {
                        unparsed("name", "<red>Alex")
                    }

                parsed shouldContainText "<red>Alex"
                parsed.shouldNotHaveColor()
            }

            "applies component placeholders from existing Adventure components" {
                val badge = Component.text("VIP", NamedTextColor.AQUA)
                val parsed =
                    mini("<badge> joined") {
                        component("badge", badge)
                    }

                parsed shouldHaveChildCount 2
                parsed.childAt(0) shouldBe badge
                parsed.childAt(1) shouldContainText " joined"
                parsed.childAt(1).shouldNotHaveColor()
            }

            "builds component placeholders from the Kotventure component DSL" {
                val parsed =
                    mini("<badge> joined") {
                        component("badge") {
                            text("VIP") {
                                color(NamedTextColor.AQUA)
                            }
                        }
                    }

                parsed shouldHaveChildCount 2
                parsed.childAt(0) shouldContainText "VIP"
                parsed.childAt(0) shouldHaveColor NamedTextColor.AQUA
                parsed.childAt(1) shouldContainText " joined"
                parsed.childAt(1).shouldNotHaveColor()
            }

            "appends parsed MiniMessage components inside the component DSL" {
                val message =
                    component {
                        text("Notice: ")
                        mini("<gold><player></gold> joined") {
                            unparsed("player", "Alex")
                        }
                    }

                message shouldHaveChildCount 2
                message.childAt(0) shouldContainText "Notice: "
                message.childAt(1) shouldContainText "Alex joined"
                message.childAt(1) shouldHaveChildCount 2
                message.childAt(1).childAt(0) shouldContainText "Alex"
                message.childAt(1).childAt(0) shouldHaveColor NamedTextColor.GOLD
                message.childAt(1).childAt(1) shouldContainText " joined"
                message.childAt(1).childAt(1).shouldNotHaveColor()
            }

            "resolves component typed placeholders" {
                val badge = placeholder<Component>("badge")
                val parsed =
                    mini("<badge> joined") {
                        resolve(badge, Component.text("VIP", NamedTextColor.AQUA))
                    }

                parsed shouldHaveChildCount 2
                parsed.childAt(0) shouldContainText "VIP"
                parsed.childAt(0) shouldHaveColor NamedTextColor.AQUA
                parsed.childAt(1) shouldContainText " joined"
                parsed.childAt(1).shouldNotHaveColor()
            }

            "resolves string typed placeholders as literal text" {
                val name = placeholder<String>("name")
                val parsed =
                    mini("<name>") {
                        resolve(name, "<red>Alex")
                    }

                parsed shouldContainText "<red>Alex"
                parsed.shouldNotHaveColor()
            }

            "resolves numeric and boolean typed placeholders as literal text" {
                val count = placeholder<Int>("count")
                val ratio = placeholder<Double>("ratio")
                val online = placeholder<Boolean>("online")
                val parsed =
                    mini("<count> / <ratio> / <online>") {
                        resolve(count, 3)
                        resolve(ratio, 1.5)
                        resolve(online, true)
                    }

                parsed shouldContainText "3 / 1.5 / true"
            }

            "resolves mixed typed placeholders in one message" {
                val channel = placeholder<String>("channel")
                val player = placeholder<Component>("player")
                val count = placeholder<Long>("count")
                val playerComponent = Component.text("Alex", NamedTextColor.AQUA)
                val parsed =
                    mini("<gray>[<channel>]</gray> <gold><player></gold> has <count> invites") {
                        resolve(channel, "chat")
                        resolve(player, playerComponent)
                        resolve(count, 2L)
                    }

                parsed shouldContainText "[chat] Alex has 2 invites"
                parsed shouldContainComponent playerComponent
            }

            "supports typed placeholders inside the component DSL" {
                val player = placeholder<String>("player")
                val message =
                    component {
                        text("Notice: ")
                        mini("<gold><player></gold> joined") {
                            resolve(player, "Alex")
                        }
                    }

                message shouldHaveChildCount 2
                message.childAt(1) shouldContainText "Alex joined"
                message.childAt(1).childAt(0) shouldHaveColor NamedTextColor.GOLD
            }

            "keeps parsed string bridge available for markup-aware substitutions" {
                val parsed =
                    mini("<prefix> <name>") {
                        parsed("prefix", "<gray>[chat]</gray>")
                        resolve(placeholder<String>("name"), "Alex")
                    }

                parsed shouldContainText "[chat] Alex"
                parsed.childAt(0) shouldHaveColor NamedTextColor.GRAY
            }

            "rejects unsupported placeholder value families" {
                val error =
                    shouldThrow<IllegalArgumentException> {
                        placeholder<List<String>>("items")
                    }

                error.message shouldContain "Supported MiniMessage placeholder types"
            }

            "treats equivalent typed placeholders as equal values" {
                val first = placeholder<String>("name")
                val second = placeholder<String>("name")

                first shouldBe second
                setOf(first, second) shouldHaveSize 1
            }

            "distinguishes typed placeholders with different value types" {
                val count = placeholder<Int>("count")
                val label = placeholder<String>("count")

                count shouldNotBe label
                setOf(count, label) shouldHaveSize 2
            }

            "rejects invalid placeholder names" {
                val error =
                    shouldThrow<IllegalArgumentException> {
                        placeholder<String>("BadName")
                    }

                error.message shouldContain "placeholder names must match"
            }

            "does not compile when a typed placeholder is resolved with the wrong value type" {
                assertDoesNotCompile(
                    fileName = "TypedPlaceholderMismatchTest.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.minimessage.mini
                        import io.github.lmliam.kotventure.minimessage.placeholder

                        fun shouldNotCompile() {
                            val count = placeholder<Int>("count")

                            mini("<count>") {
                                resolve(count, "three")
                            }
                        }
                        """.trimIndent(),
                    "Argument type mismatch",
                    "String",
                    "Int",
                )

                assertDoesNotCompile(
                    fileName = "ComponentPlaceholderMismatchTest.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.minimessage.mini
                        import io.github.lmliam.kotventure.minimessage.placeholder
                        import net.kyori.adventure.text.Component

                        fun shouldNotCompile() {
                            val player = placeholder<Component>("player")

                            mini("<player>") {
                                resolve(player, 3)
                            }
                        }
                        """.trimIndent(),
                    "Argument type mismatch",
                    "Int",
                    "Component",
                )
            }
        },
    )
