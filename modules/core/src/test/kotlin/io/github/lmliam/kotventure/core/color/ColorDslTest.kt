package io.github.lmliam.kotventure.core.color

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

class ColorDslTest :
    StringSpec(
        {
            "builds exact Adventure colors from hex rgb and hsv constructors" {
                hex("#FF00AA") shouldBe TextColor.color(0xFF00AA)
                hex("#FF00AA").asHexString() shouldBe "#FF00AA"
                hex("#ffaa00") shouldBe NamedTextColor.GOLD

                rgb(255, 170, 0) shouldBe NamedTextColor.GOLD

                hsv(0f, 1f, 1f) shouldBe TextColor.color(0xFF0000)
                hsv(1f / 3f, 1f, 1f) shouldBe TextColor.color(0x00FF00)
                hsv(2f / 3f, 1f, 1f) shouldBe TextColor.color(0x0000FF)
            }

            "rejects malformed hex and out of range color components" {
                shouldThrow<IllegalArgumentException> {
                    hex("FF00AA")
                }.message shouldContain "#RRGGBB"

                shouldThrow<IllegalArgumentException> {
                    hex("#F0A")
                }.message shouldContain "#RRGGBB"

                shouldThrow<IllegalArgumentException> {
                    hex("#GG00AA")
                }.message shouldContain "#RRGGBB"

                shouldThrow<IllegalArgumentException> {
                    rgb(-1, 0, 0)
                }.message shouldContain "red"

                shouldThrow<IllegalArgumentException> {
                    rgb(0, 256, 0)
                }.message shouldContain "green"

                shouldThrow<IllegalArgumentException> {
                    hsv(-0.01f, 1f, 1f)
                }.message shouldContain "hue"

                shouldThrow<IllegalArgumentException> {
                    hsv(0f, 1.01f, 1f)
                }.message shouldContain "saturation"
            }

            "delegates interpolation to Adventure TextColor lerp behavior" {
                interpolate(0f, NamedTextColor.BLACK, NamedTextColor.WHITE) shouldBe NamedTextColor.BLACK
                interpolate(0.5f, NamedTextColor.BLACK, NamedTextColor.WHITE).asHexString() shouldBe "#808080"
                interpolate(1f, NamedTextColor.BLACK, NamedTextColor.WHITE) shouldBe NamedTextColor.WHITE
                interpolate(-1f, NamedTextColor.BLACK, NamedTextColor.WHITE) shouldBe NamedTextColor.BLACK
                interpolate(2f, NamedTextColor.BLACK, NamedTextColor.WHITE) shouldBe NamedTextColor.WHITE
            }

            "exposes the Adventure named palette through aliases and lookup helpers" {
                black shouldBe NamedTextColor.BLACK
                darkBlue shouldBe NamedTextColor.DARK_BLUE
                darkGreen shouldBe NamedTextColor.DARK_GREEN
                darkAqua shouldBe NamedTextColor.DARK_AQUA
                darkRed shouldBe NamedTextColor.DARK_RED
                darkPurple shouldBe NamedTextColor.DARK_PURPLE
                gold shouldBe NamedTextColor.GOLD
                gray shouldBe NamedTextColor.GRAY
                darkGray shouldBe NamedTextColor.DARK_GRAY
                blue shouldBe NamedTextColor.BLUE
                green shouldBe NamedTextColor.GREEN
                aqua shouldBe NamedTextColor.AQUA
                red shouldBe NamedTextColor.RED
                lightPurple shouldBe NamedTextColor.LIGHT_PURPLE
                yellow shouldBe NamedTextColor.YELLOW
                white shouldBe NamedTextColor.WHITE

                namedColor("dark_blue") shouldBe NamedTextColor.DARK_BLUE
                namedColor("missing").shouldBeNull()

                shouldThrow<NoSuchElementException> {
                    namedColorOrThrow("missing")
                }.message shouldContain "missing"
            }
        },
    )
