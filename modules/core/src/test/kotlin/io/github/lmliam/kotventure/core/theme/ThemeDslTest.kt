package io.github.lmliam.kotventure.core.theme

import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.color.hex
import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.style.styled
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
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

private object BrandTheme : Theme("brand") {
    val primary = hex("#5865F2")
    val headerFont: Key = key("minecraft", "uniform")

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

    val callout: Style by style {
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
                            "callout" to BrandTheme.callout,
                        )
                snapshot.keys.toList() shouldBe listOf("header", "saved", "danger", "callout")
                shouldThrow<UnsupportedOperationException> {
                    @Suppress("UNCHECKED_CAST")
                    (snapshot as MutableMap<String, Style>)["other"] = Style.empty()
                }
            }

            "can be defined, registered, and applied end to end" {
                val registry = ThemeRegistry()

                registry.register(BrandTheme)

                val registered = registry.theme("brand")
                registered shouldBe BrandTheme
                val header = registered?.style("header")
                header shouldBe BrandTheme.header
                val title = text("Welcome") styled checkNotNull(header)
                title shouldHaveColor BrandTheme.primary
            }

            "rejects blank theme names" {
                shouldThrow<IllegalArgumentException> {
                    object : Theme(" ") {}
                }
            }
        },
    )
