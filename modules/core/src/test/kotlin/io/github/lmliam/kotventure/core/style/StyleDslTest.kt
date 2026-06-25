package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveFont
import io.github.lmliam.kotventure.test.text.shouldHaveInsertion
import io.github.lmliam.kotventure.test.text.shouldHaveShadowColor
import io.github.lmliam.kotventure.test.text.shouldNotHaveColor
import io.github.lmliam.kotventure.test.text.shouldNotHaveFont
import io.github.lmliam.kotventure.test.text.shouldNotHaveInsertion
import io.github.lmliam.kotventure.test.text.shouldNotHaveShadowColor
import io.kotest.core.spec.style.StringSpec
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

class StyleDslTest :
    StringSpec(
        {
            val decorations =
                listOf(
                    TextDecoration.BOLD,
                    TextDecoration.ITALIC,
                    TextDecoration.UNDERLINED,
                    TextDecoration.STRIKETHROUGH,
                    TextDecoration.OBFUSCATED,
                )

            "builds a reusable style with color font insertion and decoration states" {
                val font = Key.key("minecraft", "uniform")
                val header =
                    style {
                        color(NamedTextColor.GOLD)
                        font(font)
                        insertion("/help")
                        bold()
                        italic(false)
                        underlined(null)
                        strikethrough(State.TRUE)
                        obfuscated(State.NOT_SET)
                    }

                val component = Component.text("Title").style(header)

                component shouldHaveColor NamedTextColor.GOLD
                component shouldHaveFont font
                component shouldHaveInsertion "/help"
                component.shouldHaveDecoration(TextDecoration.BOLD, State.TRUE)
                component.shouldHaveDecoration(TextDecoration.ITALIC, State.FALSE)
                component.shouldHaveDecoration(TextDecoration.UNDERLINED, State.NOT_SET)
                component.shouldHaveDecoration(TextDecoration.STRIKETHROUGH, State.TRUE)
                component.shouldHaveDecoration(TextDecoration.OBFUSCATED, State.NOT_SET)
            }

            "applies the same reusable style to multiple component builders" {
                val font = Key.key("minecraft", "uniform")
                val header =
                    style {
                        color(NamedTextColor.AQUA)
                        font(font)
                        insertion("/warp spawn")
                        bold()
                    }

                val component =
                    component {
                        text("Primary") {
                            style(header)
                        }
                        text("Secondary") {
                            style(header)
                        }
                    }

                component shouldHaveChildCount 2
                decorations.forEach { decoration ->
                    component.childAt(0).shouldHaveDecoration(decoration, header.decoration(decoration))
                    component.childAt(1).shouldHaveDecoration(decoration, header.decoration(decoration))
                }
                component.childAt(0) shouldHaveColor NamedTextColor.AQUA
                component.childAt(1) shouldHaveColor NamedTextColor.AQUA
                component.childAt(0) shouldHaveFont font
                component.childAt(1) shouldHaveFont font
                component.childAt(0) shouldHaveInsertion "/warp spawn"
                component.childAt(1) shouldHaveInsertion "/warp spawn"
            }

            "sets every named decoration helper to true false and unset states" {
                val enabled =
                    style {
                        bold(true)
                        italic(true)
                        underlined(true)
                        strikethrough(true)
                        obfuscated(true)
                    }
                val disabled =
                    style {
                        bold(false)
                        italic(false)
                        underlined(false)
                        strikethrough(false)
                        obfuscated(false)
                    }
                val unset =
                    style {
                        bold(null)
                        italic(null)
                        underlined(null)
                        strikethrough(null)
                        obfuscated(null)
                    }

                decorations.forEach { decoration ->
                    Component.text("enabled").style(enabled).shouldHaveDecoration(decoration, State.TRUE)
                    Component.text("disabled").style(disabled).shouldHaveDecoration(decoration, State.FALSE)
                    Component.text("unset").style(unset).shouldHaveDecoration(decoration, State.NOT_SET)
                }
            }

            "sets component-local style attributes through the shared style scope" {
                val font = Key.key("minecraft", "alt")
                val component =
                    component {
                        text("Styled") {
                            style {
                                font(font)
                                insertion("/inspect")
                                decoration(TextDecoration.BOLD, true)
                                decoration(TextDecoration.ITALIC, false)
                                decoration(TextDecoration.UNDERLINED, State.NOT_SET)
                                strikethrough(State.FALSE)
                                obfuscated(null)
                            }
                        }
                    }

                val child = component.childAt(0)
                child shouldHaveFont font
                child shouldHaveInsertion "/inspect"
                child.shouldHaveDecoration(TextDecoration.BOLD, State.TRUE)
                child.shouldHaveDecoration(TextDecoration.ITALIC, State.FALSE)
                child.shouldHaveDecoration(TextDecoration.UNDERLINED, State.NOT_SET)
                child.shouldHaveDecoration(TextDecoration.STRIKETHROUGH, State.FALSE)
                child.shouldHaveDecoration(TextDecoration.OBFUSCATED, State.NOT_SET)
            }

            "sets a raw shadow color on a reusable style and a component shortcut" {
                val shadow = ShadowColor.shadowColor(0xFF112233.toInt())

                val styled = Component.text("Spawn").style(style { shadow(shadow) })
                val component =
                    component {
                        text("Spawn") {
                            shadow(shadow)
                        }
                    }

                styled shouldHaveShadowColor shadow
                component.childAt(0) shouldHaveShadowColor shadow
            }

            "derives a shadow color from a text color and optional alpha" {
                val opaque =
                    component {
                        text("Opaque") {
                            shadow(NamedTextColor.BLACK)
                        }
                    }
                val translucent =
                    component {
                        text("Translucent") {
                            shadow(NamedTextColor.BLACK, alpha = 0x80)
                        }
                    }

                opaque.childAt(0) shouldHaveShadowColor ShadowColor.shadowColor(NamedTextColor.BLACK, 0xFF)
                translucent.childAt(0) shouldHaveShadowColor ShadowColor.shadowColor(NamedTextColor.BLACK, 0x80)
            }

            "clears the shadow color when null is provided" {
                val base = Style.style().shadowColor(ShadowColor.shadowColor(0xFF112233.toInt())).build()

                val component =
                    component {
                        style(base)
                        style {
                            shadow(null)
                        }
                    }

                component.shouldNotHaveShadowColor()
            }

            "can clear nullable color font and insertion from an existing component style" {
                val base =
                    Style
                        .style()
                        .color(NamedTextColor.RED)
                        .font(Key.key("minecraft", "uniform"))
                        .insertion("/old")
                        .build()

                val component =
                    component {
                        style(base)
                        style {
                            color(null)
                            font(null)
                            insertion(null)
                        }
                    }

                component.shouldNotHaveColor()
                component.shouldNotHaveFont()
                component.shouldNotHaveInsertion()
            }
        },
    )
