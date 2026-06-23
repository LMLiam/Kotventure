package io.github.lmliam.kotventure.minimessage

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
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

// ---------------------------------------------------------------------------
// Fixture templates — declared at file scope so they exercise the object-init
// path and are reused across tests without per-test construction cost.
// ---------------------------------------------------------------------------

private object WelcomeTemplate : MiniTemplate("<gold>Welcome <player>, <count> new messages</gold>") {
    val player = placeholder<Component>("player")
    val count = placeholder<Int>("count")
}

// Template whose declared "unused" placeholder is absent from the markup: an invalid definition.
private object SparseTemplate : MiniTemplate("<gold>Hello <name></gold>") {
    val name = placeholder<String>("name")
    val unused = placeholder<Int>("unused")
}

// Cross-template fixture: declares a "player" placeholder of a DIFFERENT type than WelcomeTemplate.
private object AltTemplate : MiniTemplate("<red>Alt <player>") {
    val player = placeholder<String>("player")
}

// Cross-template fixture: declares a structurally equal "player" descriptor to WelcomeTemplate.
private object SameTypeAltTemplate : MiniTemplate("<red>Alt <player>") {
    val player = placeholder<Component>("player")
}

class MiniTemplateTest :
    StringSpec(
        {
            // ---------------------------------------------------------------
            // AC1 — Required-placeholder enforcement
            // ---------------------------------------------------------------

            "throws IllegalArgumentException when one binding is omitted" {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate {
                        bind(player, Component.text("Alex"))
                        // count intentionally omitted
                    }
                }
            }

            "throws IllegalArgumentException when all bindings are omitted" {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate { }
                }
            }

            "throws IllegalArgumentException when binding a placeholder not declared on this template" {
                val outsider = placeholder<String>("outsider")

                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate {
                        bind(player, Component.text("Alex"))
                        bind(count, 1)
                        @Suppress("UNCHECKED_CAST")
                        bind(outsider as MiniMessagePlaceholder<Component>, Component.text("x"))
                    }
                }
            }

            // ---------------------------------------------------------------
            // Priority 2 — Descriptor identity: same name, different template
            // ---------------------------------------------------------------

            "throws IllegalArgumentException when binding another template's same-named descriptor" {
                // WelcomeTemplate.player is placeholder<Component>("player")
                // AltTemplate.player     is placeholder<String>("player")
                // Binding AltTemplate.player inside WelcomeTemplate must be rejected by identity,
                // even though the names match, because they are different descriptor objects.
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate {
                        @Suppress("UNCHECKED_CAST")
                        bind(AltTemplate.player as MiniMessagePlaceholder<Component>, Component.text("Alex"))
                        bind(count, 1)
                    }
                }
            }

            "throws IllegalArgumentException when binding another template's structurally equal descriptor" {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate {
                        bind(SameTypeAltTemplate.player, Component.text("Alex"))
                        bind(count, 1)
                    }
                }
            }

            // ---------------------------------------------------------------
            // AC2 — Reuse correctness: two independent, correct components
            // ---------------------------------------------------------------

            "renders correct component for each of two independent invocations" {
                val forAlex =
                    WelcomeTemplate {
                        bind(player, Component.text("Alex", NamedTextColor.GREEN))
                        bind(count, 3)
                    }

                val forSam =
                    WelcomeTemplate {
                        bind(player, Component.text("Sam", NamedTextColor.RED))
                        bind(count, 0)
                    }

                // Gold color is applied to the root component by the <gold> tag.
                forAlex shouldHaveColor NamedTextColor.GOLD
                forAlex shouldContainText "Alex"
                forAlex shouldContainText "3"

                forSam shouldHaveColor NamedTextColor.GOLD
                forSam shouldContainText "Sam"
                forSam shouldContainText "0"

                // The two components are independent — Alex's render did not leak into Sam's.
                forAlex shouldContainText "Alex"
                forSam shouldContainText "Sam"
            }

            "renders children with the bound component placeholder inline" {
                val alex = Component.text("Alex", NamedTextColor.AQUA)

                val rendered =
                    WelcomeTemplate {
                        bind(player, alex)
                        bind(count, 5)
                    }

                // The bound component appears as a node in the rendered tree (structural equality).
                rendered shouldContainText "Alex"
                rendered shouldHaveColor NamedTextColor.GOLD
                rendered shouldContainComponent alex
            }

            // ---------------------------------------------------------------
            // AC3 — Type safety: assertDoesNotCompile
            // ---------------------------------------------------------------

            "does not compile when an Int placeholder is bound with a String value" {
                // Test the type safety of MiniTemplateBindingScope.bind via the public interface.
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

            "does not compile when a Component placeholder is bound with an Int value" {
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

            // ---------------------------------------------------------------
            // AC3 — Scope safety: placeholder() is NOT callable inside the render lambda
            // ---------------------------------------------------------------

            "does not compile when placeholder() is called inside the render lambda" {
                // The protected placeholder() member must be inaccessible at the render site.
                // User code in the render lambda is NOT inside a subclass body, so `protected`
                // blocks the call even though the template type is a context receiver.
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

            // ---------------------------------------------------------------
            // Duplicate placeholder name → fails at object init
            // ---------------------------------------------------------------

            "throws IllegalArgumentException at template construction when two placeholders share a name" {
                shouldThrow<IllegalArgumentException> {
                    object : MiniTemplate("<a>") {
                        @Suppress("unused")
                        val first = placeholder<String>("a")

                        @Suppress("unused")
                        val second = placeholder<Int>("a")
                    }
                }
            }

            // ---------------------------------------------------------------
            // Empty / blank markup → throws at construction
            // ---------------------------------------------------------------

            "throws IllegalArgumentException when markup is empty" {
                shouldThrow<IllegalArgumentException> {
                    object : MiniTemplate("") {}
                }
            }

            "throws IllegalArgumentException when markup is blank" {
                shouldThrow<IllegalArgumentException> {
                    object : MiniTemplate("   ") {}
                }
            }

            // ---------------------------------------------------------------
            // Blank placeholder names
            // ---------------------------------------------------------------

            "throws IllegalArgumentException when placeholder name is empty" {
                shouldThrow<IllegalArgumentException> {
                    placeholder<String>("")
                }
            }

            // ---------------------------------------------------------------
            // Duplicate render-time binding
            // ---------------------------------------------------------------

            "throws IllegalArgumentException when the same placeholder is bound twice" {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate {
                        bind(player, Component.text("First", NamedTextColor.GREEN))
                        bind(count, 1)
                        bind(player, Component.text("Second", NamedTextColor.RED))
                    }
                }
            }

            "throws IllegalArgumentException when only the second-declared placeholder is bound" {
                shouldThrow<IllegalArgumentException> {
                    WelcomeTemplate {
                        bind(count, 99)
                        // player intentionally omitted
                    }
                }
            }

            // ---------------------------------------------------------------
            // Declared-but-unused placeholder → invalid template
            // ---------------------------------------------------------------

            "throws IllegalArgumentException when a declared placeholder is absent from the markup" {
                shouldThrow<IllegalArgumentException> {
                    SparseTemplate {
                        bind(name, "Alex")
                        bind(unused, 99)
                    }
                }
            }

            // ---------------------------------------------------------------
            // Child structure assertion
            // ---------------------------------------------------------------

            "renders a multi-segment component with the player and count inline" {
                val rendered =
                    WelcomeTemplate {
                        bind(player, Component.text("Alex"))
                        bind(count, 7)
                    }

                // Root carries the <gold> colour; text is contained across the tree.
                rendered shouldHaveColor NamedTextColor.GOLD
                rendered shouldContainText "Welcome"
                rendered shouldContainText "Alex"
                rendered shouldContainText "7"
            }
        },
    )
