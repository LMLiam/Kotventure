package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.nbt.blockNbt
import io.github.lmliam.kotventure.core.nbt.entityNbt
import io.github.lmliam.kotventure.core.nbt.nbtPath
import io.github.lmliam.kotventure.core.nbt.storageNbt
import io.github.lmliam.kotventure.core.score.score
import io.github.lmliam.kotventure.core.selector.allPlayers
import io.github.lmliam.kotventure.core.selector.nearestPlayer
import io.github.lmliam.kotventure.core.selector.parseSelector
import io.github.lmliam.kotventure.core.selector.selector
import io.github.lmliam.kotventure.core.translatable.translatable
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeBlockNbtComponent
import io.github.lmliam.kotventure.test.text.shouldBeEntityNbtComponent
import io.github.lmliam.kotventure.test.text.shouldBeKeybindComponent
import io.github.lmliam.kotventure.test.text.shouldBeScoreComponent
import io.github.lmliam.kotventure.test.text.shouldBeSelectorComponent
import io.github.lmliam.kotventure.test.text.shouldBeStorageNbtComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveBlockPos
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveEntitySelector
import io.github.lmliam.kotventure.test.text.shouldHaveKeybind
import io.github.lmliam.kotventure.test.text.shouldHaveNbtPath
import io.github.lmliam.kotventure.test.text.shouldHaveScoreName
import io.github.lmliam.kotventure.test.text.shouldHaveScoreObjective
import io.github.lmliam.kotventure.test.text.shouldHaveSelectorPattern
import io.github.lmliam.kotventure.test.text.shouldHaveSelectorSeparator
import io.github.lmliam.kotventure.test.text.shouldHaveStorageKey
import io.github.lmliam.kotventure.test.text.shouldHaveStyle
import io.github.lmliam.kotventure.test.text.shouldHaveTranslationKey
import io.github.lmliam.kotventure.test.text.shouldNotHaveDecoration
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

class ComponentDslTest :
    StringSpec(
        {
            "builds a text component with content" {
                val component =
                    component {
                        text("Hello")
                    }

                component shouldContainText "Hello"
            }

            "applies color to the root text component" {
                val component =
                    component {
                        color(NamedTextColor.RED)
                        text("Warning")
                    }

                component shouldHaveColor NamedTextColor.RED
            }

            "appends nested text children in declaration order" {
                val component =
                    component {
                        text("Hello ")
                        text {
                            content("world")
                            color(NamedTextColor.AQUA)
                        }
                        text {
                            content("!")
                        }
                    }

                component shouldHaveChildCount 3
                component.childAt(0) shouldContainText "Hello "
                component.childAt(1) shouldContainText "world"
                component.childAt(1) shouldHaveColor NamedTextColor.AQUA
                component.childAt(2) shouldContainText "!"
            }

            "appends text children with initial content" {
                val component =
                    component {
                        text("Hello") {
                            color(NamedTextColor.AQUA)
                        }
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldContainText "Hello"
                component.childAt(0) shouldHaveColor NamedTextColor.AQUA
            }

            "appends existing Adventure components in declaration order" {
                val badge = Component.text("[OK]", NamedTextColor.GREEN)

                val component =
                    component {
                        text("Status: ")
                        append(badge)
                        text(" ready") {
                            italic()
                        }
                    }

                component shouldHaveChildCount 3
                component.childAt(0) shouldContainText "Status: "
                component.childAt(1) shouldBe badge
                component.childAt(2) shouldContainText " ready"
                component.childAt(2) shouldHaveDecoration TextDecoration.ITALIC
            }

            "appends a newline component" {
                val component =
                    component {
                        text("first")
                        newline()
                        text("second")
                    }

                component shouldHaveChildCount 3
                component.childAt(0) shouldContainText "first"
                component.childAt(1) shouldBe Component.newline()
                component.childAt(2) shouldContainText "second"
            }

            "appends specialized component children in declaration order" {
                val separator = Component.text(", ")

                val component =
                    component {
                        translatable("item.minecraft.diamond") {
                            fallback("Diamond")
                        }
                        keybind("key.jump") {
                            color(NamedTextColor.YELLOW)
                        }
                        score("Alex", "kills")
                        selector(allPlayers()) {
                            separator(separator)
                        }
                    }

                component shouldHaveChildCount 4
                component.childAt(0) shouldHaveTranslationKey "item.minecraft.diamond"
                val keybind = component.childAt(1).shouldBeKeybindComponent()
                keybind shouldHaveKeybind "key.jump"
                keybind shouldHaveColor NamedTextColor.YELLOW
                val score = component.childAt(2).shouldBeScoreComponent()
                score shouldHaveScoreName "Alex"
                score shouldHaveScoreObjective "kills"
                val selector = component.childAt(3).shouldBeSelectorComponent()
                selector shouldHaveSelectorPattern "@a"
                selector shouldHaveSelectorSeparator separator
            }

            "appends nbt component children in declaration order" {
                val pos = BlockNBTComponent.Pos.fromString("1 2 3")
                val storage = Key.key("kotventure", "messages")

                val component =
                    component {
                        blockNbt(pos, nbtPath("Items[0].id"))
                        entityNbt(nearestPlayer(), nbtPath("Inventory[0].id"))
                        storageNbt(storage, nbtPath("entries[0].id"))
                    }

                component shouldHaveChildCount 3
                val blockNbt = component.childAt(0).shouldBeBlockNbtComponent()
                blockNbt shouldHaveBlockPos pos
                blockNbt shouldHaveNbtPath "Items[0].id"
                val entityNbt = component.childAt(1).shouldBeEntityNbtComponent()
                entityNbt shouldHaveEntitySelector "@p"
                entityNbt shouldHaveNbtPath "Inventory[0].id"
                val storageNbt = component.childAt(2).shouldBeStorageNbtComponent()
                storageNbt shouldHaveStorageKey storage
                storageNbt shouldHaveNbtPath "entries[0].id"
            }

            "applies a complete Adventure style" {
                val style = Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)

                val component =
                    component {
                        style(style)
                    }

                component shouldHaveStyle style
            }

            "applies a style block to the current component" {
                val component =
                    component {
                        style {
                            color(NamedTextColor.GOLD)
                            bold()
                            underlined()
                        }
                    }

                component shouldHaveColor NamedTextColor.GOLD
                component shouldHaveDecoration TextDecoration.BOLD
                component shouldHaveDecoration TextDecoration.UNDERLINED
            }

            "keeps nested style blocks scoped to their current child component" {
                val component =
                    component {
                        style {
                            color(NamedTextColor.GRAY)
                        }
                        text("child") {
                            style {
                                color(NamedTextColor.AQUA)
                                obfuscated()
                            }
                        }
                    }

                component shouldHaveColor NamedTextColor.GRAY
                component shouldHaveChildCount 1
                component.childAt(0) shouldHaveColor NamedTextColor.AQUA
                component.childAt(0) shouldHaveDecoration TextDecoration.OBFUSCATED
                component.childAt(0) shouldNotHaveDecoration TextDecoration.BOLD
            }

            "keeps style blocks additive with surrounding style calls" {
                val styleAfterColor =
                    component {
                        color(NamedTextColor.GOLD)
                        style {
                            bold()
                        }
                    }

                val colorAfterStyle =
                    component {
                        style {
                            bold()
                        }
                        color(NamedTextColor.GOLD)
                    }

                val styleAfterDecoration =
                    component {
                        bold()
                        style {
                            color(NamedTextColor.GOLD)
                        }
                    }

                styleAfterColor shouldHaveColor NamedTextColor.GOLD
                styleAfterColor shouldHaveDecoration TextDecoration.BOLD
                colorAfterStyle shouldHaveColor NamedTextColor.GOLD
                colorAfterStyle shouldHaveDecoration TextDecoration.BOLD
                styleAfterDecoration shouldHaveColor NamedTextColor.GOLD
                styleAfterDecoration shouldHaveDecoration TextDecoration.BOLD
            }

            "applies a decoration to the root text component" {
                val component =
                    component {
                        decorate(TextDecoration.BOLD)
                    }

                component shouldHaveDecoration TextDecoration.BOLD
            }

            "applies a decoration through the generic decoration hook" {
                val component =
                    component {
                        text("Marked") {
                            decorate(TextDecoration.UNDERLINED)
                        }
                    }

                component.childAt(0) shouldHaveDecoration TextDecoration.UNDERLINED
            }

            "applies each named decoration toggle" {
                val component =
                    component {
                        text("Decorated") {
                            bold()
                            italic()
                            underlined()
                            strikethrough()
                            obfuscated()
                        }
                    }

                component.childAt(0) shouldHaveDecoration TextDecoration.BOLD
                component.childAt(0) shouldHaveDecoration TextDecoration.ITALIC
                component.childAt(0) shouldHaveDecoration TextDecoration.UNDERLINED
                component.childAt(0) shouldHaveDecoration TextDecoration.STRIKETHROUGH
                component.childAt(0) shouldHaveDecoration TextDecoration.OBFUSCATED
            }

            "applies decorations independently to nested children" {
                val component =
                    component {
                        text("plain")
                        text("loud") {
                            bold()
                        }
                    }

                component shouldHaveChildCount 2
                component.childAt(0) shouldContainText "plain"
                component.childAt(0) shouldNotHaveDecoration TextDecoration.BOLD
                component.childAt(1) shouldContainText "loud"
                component.childAt(1) shouldHaveDecoration TextDecoration.BOLD
            }

            "prevents implicit outer scope access in Kotventure-marked DSL blocks" {
                assertDoesNotCompile(
                    fileName = "DslMarkerScopeTest.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
                        import io.github.lmliam.kotventure.core.component.component

                        @KotventureDslMarker
                        class OuterScope {
                            fun outerOnly() {
                            }
                        }

                        fun outer(init: OuterScope.() -> Unit) {
                            OuterScope().init()
                        }

                        fun shouldNotCompile() {
                            outer {
                                component {
                                    outerOnly()
                                }
                            }
                        }
                        """.trimIndent(),
                    "implicit receiver",
                )
            }

            "prevents text content access from the generic component root" {
                assertDoesNotCompile(
                    fileName = "ComponentRootScopeTest.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.core.component.component

                        fun shouldNotCompile() {
                            component {
                                content("root text")
                            }
                        }
                        """.trimIndent(),
                    "Unresolved reference 'content'",
                )
            }

            "prevents text scope access inside style blocks" {
                assertDoesNotCompile(
                    fileName = "StyleScopeLeakTest.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.core.component.component
                        import io.github.lmliam.kotventure.core.text.text
                        import net.kyori.adventure.text.format.NamedTextColor

                        fun shouldNotCompile() {
                            component {
                                text {
                                    style {
                                        color(NamedTextColor.GOLD)
                                        content("leaked")
                                    }
                                }
                            }
                        }
                        """.trimIndent(),
                    "implicit receiver",
                )
            }

            "builds a component tree when nested scopes stay explicit" {
                var outerWasCalled = false

                @KotventureDslMarker
                class OuterScope {
                    fun outerOnly() {
                        outerWasCalled = true
                    }
                }

                fun outer(init: OuterScope.() -> Unit) {
                    OuterScope().init()
                }

                lateinit var component: Component

                outer outer@{
                    component =
                        component {
                            text("child") {
                                bold()
                            }
                            this@outer.outerOnly()
                        }
                }

                outerWasCalled shouldBe true
                component shouldHaveChildCount 1
                component.childAt(0) shouldContainText "child"
                component.childAt(0) shouldHaveDecoration TextDecoration.BOLD
            }
        },
    )
