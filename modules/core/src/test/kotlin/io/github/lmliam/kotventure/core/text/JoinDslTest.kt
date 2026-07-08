package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.color.darkGray
import io.github.lmliam.kotventure.core.color.gray
import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class JoinDslTest :
    StringSpec(
        {
            val alex = text("Alex")
            val steve = text("Steve")
            val notch = text("Notch")

            "joins three components with a separator between each" {
                val result =
                    listOf(alex, steve, notch).join {
                        separator(", ")
                    }

                result shouldHaveChildCount 5
                result.childAt(0) shouldBe alex
                result.childAt(1) shouldContainText ", "
                result.childAt(2) shouldBe steve
                result.childAt(3) shouldContainText ", "
                result.childAt(4) shouldBe notch
            }

            "uses lastSeparator before the final element" {
                val result =
                    listOf(alex, steve, notch).join {
                        separator(", ")
                        lastSeparator(" and ")
                    }

                result shouldHaveChildCount 5
                result.childAt(0) shouldBe alex
                result.childAt(1) shouldContainText ", "
                result.childAt(2) shouldBe steve
                result.childAt(3) shouldContainText " and "
                result.childAt(4) shouldBe notch
            }

            "falls back to separator when lastSeparator is not set" {
                val result =
                    listOf(alex, steve, notch).join {
                        separator(", ")
                    }

                result.childAt(1) shouldContainText ", "
                result.childAt(3) shouldContainText ", "
            }

            "wraps the result with prefix and suffix" {
                val result =
                    listOf(alex, steve, notch).join {
                        separator(", ")
                        prefix("Online: ")
                        suffix(".")
                    }

                result shouldHaveChildCount 7
                result.childAt(0) shouldContainText "Online: "
                result.childAt(6) shouldContainText "."
                result shouldContainText "Alex"
                result shouldContainText "Steve"
                result shouldContainText "Notch"
            }

            "string knob with styling block produces a styled separator" {
                val result =
                    listOf(alex, steve, notch).join {
                        separator(", ") { color(gray) }
                    }

                result.childAt(1) shouldHaveColor gray
                result.childAt(3) shouldHaveColor gray
            }

            "component knob form accepts a prebuilt separator unchanged" {
                val dot = text(" • ") { color(darkGray) }

                val result =
                    listOf(alex, steve, notch).join {
                        separator(dot)
                    }

                result.childAt(1) shouldBe dot
                result.childAt(3) shouldBe dot
            }

            "component knob form accepts a prebuilt lastSeparator unchanged" {
                val and = text(" and ") { color(gray) }

                val result =
                    listOf(alex, steve, notch).join {
                        separator(", ")
                        lastSeparator(and)
                    }

                result.childAt(3) shouldBe and
            }

            "component knob form accepts a prebuilt prefix unchanged" {
                val online = text("Online: ") { color(green) }

                val result =
                    listOf(alex, steve).join {
                        separator(", ")
                        prefix(online)
                    }

                result.childAt(0) shouldBe online
            }

            "component knob form accepts a prebuilt suffix unchanged" {
                val period = text(".") { color(gray) }

                val result =
                    listOf(alex, steve).join {
                        separator(", ")
                        suffix(period)
                    }

                result.childAt(3) shouldBe period
            }

            "empty list produces a component with no element children" {
                val result =
                    emptyList<Component>().join {
                        separator(", ")
                    }

                result shouldHaveChildCount 0
            }

            "empty list with prefix and suffix still applies them" {
                val result =
                    emptyList<Component>().join {
                        prefix("Online: ")
                        suffix(".")
                    }

                result shouldHaveChildCount 2
                result.childAt(0) shouldContainText "Online: "
                result.childAt(1) shouldContainText "."
            }

            "singleton list preserves the element with no separators" {
                val result =
                    listOf(alex).join {
                        separator(", ")
                    }

                result shouldHaveChildCount 0
                result shouldContainText "Alex"
            }

            "singleton list with prefix and suffix still applies them" {
                val result =
                    listOf(alex).join {
                        separator(", ")
                        prefix("Online: ")
                        suffix(".")
                    }

                result shouldHaveChildCount 3
                result.childAt(0) shouldContainText "Online: "
                result.childAt(1) shouldBe alex
                result.childAt(2) shouldContainText "."
            }

            "no configuration produces plain concatenation of inputs" {
                val result = listOf(alex, steve, notch).join()

                result shouldHaveChildCount 3
                result.childAt(0) shouldBe alex
                result.childAt(1) shouldBe steve
                result.childAt(2) shouldBe notch
            }

            "rejects a knob called multiple times" {
                shouldThrow<IllegalStateException> {
                    listOf(alex, steve).join {
                        separator(", ")
                        separator(" | ")
                    }
                }
                shouldThrow<IllegalStateException> {
                    listOf(alex, steve).join {
                        lastSeparator(" and ")
                        lastSeparator(text(" & "))
                    }
                }
                shouldThrow<IllegalStateException> {
                    listOf(alex, steve).join {
                        prefix("[")
                        prefix("(")
                    }
                }
                shouldThrow<IllegalStateException> {
                    listOf(alex, steve).join {
                        suffix("]")
                        suffix(")")
                    }
                }
            }
        },
    )
