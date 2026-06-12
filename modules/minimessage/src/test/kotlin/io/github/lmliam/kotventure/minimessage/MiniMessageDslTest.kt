package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldNotHaveColor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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
        },
    )
