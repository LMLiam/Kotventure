package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.placeholder.placeholder
import io.github.lmliam.kotventure.minimessage.template.MiniTemplate
import io.github.lmliam.kotventure.minimessage.template.bind
import io.github.lmliam.kotventure.minimessage.template.invoke
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.text.shouldContainComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

private object WelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
    val player by placeholder<Component>()
    val count by placeholder<Int>()
}

/** Declares an invalid placeholder that the markup does not use. */
private object SparseTemplate : MiniTemplate("<gold>Hello <name></gold>") {
    val name by placeholder<String>()
    val unused by placeholder<Int>()
}

/** Declares `player` with a different type from [WelcomeTemplate]. */
private object AltTemplate : MiniTemplate("<red>Alt <player>") {
    val player by placeholder<String>()
}

/** Declares a distinct `player` descriptor with the same type as [WelcomeTemplate]. */
private object SameTypeAltTemplate : MiniTemplate("<red>Alt <player>") {
    val player by placeholder<Component>()
}

/** Uses an explicit tag name that differs from the property name. */
private object RenamedTagTemplate : MiniTemplate("<gold>Hi <user></gold>") {
    val displayName = placeholder<String>("user")
}

class MiniTemplateTest :
    DescribeSpec(
        {
            describe("required-placeholder enforcement") {
                it("rejects a render that omits one binding") {
                    shouldThrow<IllegalArgumentException> {
                        WelcomeTemplate { player bind text("Alex") }
                    }
                }

                it("rejects a render that omits all bindings") {
                    shouldThrow<IllegalArgumentException> {
                        WelcomeTemplate { }
                    }
                }

                it("rejects a render that binds only the second-declared placeholder") {
                    shouldThrow<IllegalArgumentException> {
                        WelcomeTemplate { count bind 99 }
                    }
                }
            }

            describe("placeholder identity") {
                it("rejects a placeholder not declared on this template") {
                    val outsider = placeholder<String>("outsider")

                    shouldThrow<IllegalArgumentException> {
                        WelcomeTemplate {
                            player bind text("Alex")
                            count bind 1
                            @Suppress("UNCHECKED_CAST")
                            (outsider as MiniMessagePlaceholder<Component>) bind text("x")
                        }
                    }
                }

                it("rejects another template's same-named but differently-typed descriptor") {
                    shouldThrow<IllegalArgumentException> {
                        WelcomeTemplate {
                            @Suppress("UNCHECKED_CAST")
                            (AltTemplate.player as MiniMessagePlaceholder<Component>) bind text("Alex")
                            count bind 1
                        }
                    }
                }

                it("rejects another template's structurally equal descriptor") {
                    shouldThrow<IllegalArgumentException> {
                        WelcomeTemplate {
                            SameTypeAltTemplate.player bind text("Alex")
                            count bind 1
                        }
                    }
                }

                it("rejects binding the same placeholder twice in one render") {
                    shouldThrow<IllegalArgumentException> {
                        WelcomeTemplate {
                            player bind text("First") { color(green) }
                            count bind 1
                            player bind text("Second") { color(red) }
                        }
                    }
                }
            }

            describe("rendering") {
                it("renders independent components for repeated invocations") {
                    val forAlex =
                        WelcomeTemplate {
                            player bind text("Alex") { color(green) }
                            count bind 3
                        }
                    val forSam =
                        WelcomeTemplate {
                            player bind text("Sam") { color(red) }
                            count bind 0
                        }

                    forAlex shouldHaveColor gold
                    forAlex shouldContainText "Alex"
                    forAlex shouldContainText "3"

                    forSam shouldHaveColor gold
                    forSam shouldContainText "Sam"
                    forSam shouldContainText "0"
                }

                it("inlines the bound component placeholder into the tree") {
                    val alex = text("Alex") { color(aqua) }

                    val rendered =
                        WelcomeTemplate {
                            player bind alex
                            count bind 5
                        }

                    rendered shouldHaveColor gold
                    rendered shouldContainText "Alex"
                    rendered shouldContainComponent alex
                }

                it("renders every literal and bound segment") {
                    val rendered =
                        WelcomeTemplate {
                            player bind text("Alex")
                            count bind 7
                        }

                    rendered shouldHaveColor gold
                    rendered shouldContainText "Welcome"
                    rendered shouldContainText "Alex"
                    rendered shouldContainText "7"
                }
            }

            describe("compile-time type safety") {
                it("rejects an Int placeholder bound with a String value") {
                    assertDoesNotCompile(
                        fileName = "TemplateIntStringMismatch.kt",
                        source =
                            """
                        import io.github.lmliam.kotventure.minimessage.placeholder.placeholder
                        import io.github.lmliam.kotventure.minimessage.template.MiniTemplateBindingScope

                        fun test(scope: MiniTemplateBindingScope) {
                            val count = placeholder<Int>("count")
                            scope.bind(count, "three")
                        }
                        """.trimIndent(),
                        "Argument type mismatch",
                        "String",
                        "Int",
                    )
                }

                it("rejects a Component placeholder bound with an Int value") {
                    assertDoesNotCompile(
                        fileName = "TemplateComponentIntMismatch.kt",
                        source =
                            """
                        import io.github.lmliam.kotventure.minimessage.placeholder.placeholder
                        import io.github.lmliam.kotventure.minimessage.template.MiniTemplateBindingScope
                        import net.kyori.adventure.text.Component

                        fun test(scope: MiniTemplateBindingScope) {
                            val player = placeholder<Component>("player")
                            scope.bind(player, 42)
                        }
                        """.trimIndent(),
                        "Argument type mismatch",
                        "Int",
                        "Component",
                    )
                }

                it("rejects calling placeholder() inside the render lambda") {
                    // A render block cannot access the protected placeholder factory.
                    assertDoesNotCompile(
                        fileName = "TemplatePlaceholderAtRenderSite.kt",
                        source =
                            """
                        import io.github.lmliam.kotventure.minimessage.template.MiniTemplate
                        import io.github.lmliam.kotventure.minimessage.template.invoke

                        private object ScopeTestTemplate : MiniTemplate("<gold><name>") { }

                        fun test() {
                            ScopeTestTemplate {
                                placeholder<Int>("x")
                            }
                        }
                        """.trimIndent(),
                        "Cannot access",
                    )
                }
            }

            describe("delegated placeholder names") {
                it("uses the property name as the MiniMessage tag") {
                    WelcomeTemplate.player.name shouldBe "player"
                    WelcomeTemplate.count.name shouldBe "count"
                }

                it("renders when the tag name is set explicitly and differs from the property") {
                    val rendered = RenamedTagTemplate { displayName bind "Alex" }

                    rendered shouldContainText "Alex"
                }
            }

            describe("definition validation") {
                it("rejects duplicate placeholder names at construction") {
                    shouldThrow<IllegalArgumentException> {
                        object : MiniTemplate("<a>") {
                            @Suppress("unused")
                            val a by placeholder<String>()

                            @Suppress("unused")
                            val second = placeholder<Int>("a")
                        }
                    }
                }

                it("rejects empty markup at construction") {
                    shouldThrow<IllegalArgumentException> {
                        object : MiniTemplate("") {}
                    }
                }

                it("rejects blank markup at construction") {
                    shouldThrow<IllegalArgumentException> {
                        object : MiniTemplate("   ") {}
                    }
                }

                it("rejects a blank placeholder name") {
                    shouldThrow<IllegalArgumentException> {
                        placeholder<String>("")
                    }
                }

                it("rejects a declared placeholder that is absent from the markup") {
                    shouldThrow<IllegalArgumentException> {
                        SparseTemplate {
                            name bind "Alex"
                            unused bind 99
                        }
                    }
                }
            }
        },
    )
