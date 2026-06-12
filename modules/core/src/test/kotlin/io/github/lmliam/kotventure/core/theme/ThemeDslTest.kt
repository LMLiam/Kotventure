package io.github.lmliam.kotventure.core.theme

import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.color.hex
import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.registry.AdventureDsl
import io.github.lmliam.kotventure.core.style.styled
import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveFont
import io.github.lmliam.kotventure.test.text.shouldHaveStyle
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

private object BrandTheme : Theme("brand") {
    val primary = hex("#5865F2")
    val headerFont: Key = Key.key("minecraft", "uniform")

    val header: Style by style {
        color(primary)
        bold()
        italic(false)
        font(headerFont)
    }

    val saved: Style by style {
        color(green)
    }

    val danger: Style by style {
        color(red)
        bold()
        underlined(false)
    }

    val callout: Style by style("call-out") {
        color(primary)
        underlined()
    }
}

class ThemeDslTest :
    StringSpec(
        {
            "exposes semantic styles as compile-checked properties" {
                val title = text("Welcome") styled BrandTheme.header

                title shouldContainText "Welcome"
                title shouldHaveColor BrandTheme.primary
                title shouldHaveFont BrandTheme.headerFont
                title.shouldHaveDecoration(TextDecoration.BOLD, State.TRUE)
                title.shouldHaveDecoration(TextDecoration.ITALIC, State.FALSE)
            }

            "applies semantic styles to nested text children" {
                val message =
                    component {
                        text("Welcome") {
                            style(BrandTheme.header)
                        }
                        text("Careful") {
                            style(BrandTheme.danger)
                        }
                    }

                message shouldHaveChildCount 2
                message.childAt(0) shouldHaveStyle BrandTheme.header
                message.childAt(1) shouldHaveStyle BrandTheme.danger
            }

            "resolves semantic styles dynamically by property name" {
                BrandTheme.style("header") shouldBe BrandTheme.header
                BrandTheme.style("saved") shouldBe BrandTheme.saved
                BrandTheme.style("danger") shouldBe BrandTheme.danger
            }

            "uses an explicit key instead of the property name when one is given" {
                BrandTheme.style("call-out") shouldBe BrandTheme.callout
                BrandTheme.style("callout").shouldBeNull()
            }

            "returns null for missing dynamic style lookups" {
                BrandTheme.style("missing").shouldBeNull()
            }

            "exposes an immutable snapshot of declared styles" {
                val snapshot = BrandTheme.styles()

                snapshot shouldContainExactly
                        mapOf(
                            "header" to BrandTheme.header,
                            "saved" to BrandTheme.saved,
                            "danger" to BrandTheme.danger,
                            "call-out" to BrandTheme.callout,
                        )
                snapshot.keys.toList() shouldBe listOf("header", "saved", "danger", "call-out")
                shouldThrow<UnsupportedOperationException> {
                    @Suppress("UNCHECKED_CAST")
                    (snapshot as MutableMap<String, Style>)["other"] = Style.empty()
                }
            }

            "can be defined, registered, and applied end to end" {
                AdventureDsl.reset()
                try {
                    BrandTheme.register()

                    val registered = AdventureDsl.theme("brand")
                    registered shouldBe BrandTheme
                    val header = registered?.style("header")
                    header shouldBe BrandTheme.header
                    val title = text("Welcome") styled checkNotNull(header)
                    title shouldHaveColor BrandTheme.primary
                } finally {
                    AdventureDsl.reset()
                }
            }

            "rejects blank theme names" {
                val failure =
                    shouldThrow<IllegalArgumentException> {
                        object : Theme(" ") {}
                    }

                failure.message shouldContain "blank"
            }

            "rejects blank explicit style keys" {
                val failure =
                    shouldThrow<IllegalArgumentException> {
                        object : Theme("blank-key") {
                            @Suppress("unused")
                            val broken: Style by style(" ") {}
                        }
                    }

                failure.message shouldContain "blank"
            }

            "rejects duplicate style keys within one theme" {
                val failure =
                    shouldThrow<IllegalArgumentException> {
                        object : Theme("duplicate") {
                            @Suppress("unused")
                            val header: Style by style {}

                            @Suppress("unused")
                            val other: Style by style("header") {}
                        }
                    }

                failure.message shouldContain "header"
                failure.message shouldContain "duplicate"
            }
        },
    )
