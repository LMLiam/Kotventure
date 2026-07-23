package io.github.lmliam.kotventure.core.replacement

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.translatable.translatable
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldHaveArguments
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.github.lmliam.kotventure.test.text.shouldHaveHoverText
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import java.util.regex.Pattern

class ReplaceDslTest :
    StringSpec(
        {
            "literal replacement equals the raw matchLiteral and ComponentLike construction" {
                val message = Component.text("Hello %player%!")
                val replacementComponent = Component.text("Alex").color(NamedTextColor.GOLD)

                val dsl = message.replace("%player%") { replacement("Alex") { color(gold) } }
                val raw =
                    message.replaceText(
                        TextReplacementConfig
                            .builder()
                            .matchLiteral("%player%")
                            .replacement(replacementComponent as ComponentLike)
                            .build(),
                    )

                dsl shouldBe raw
            }

            "regex replacement equals the raw match(Pattern) and ComponentLike construction" {
                val message = Component.text("Value: 123")
                val replacementComponent = Component.text("NUM")

                val dsl = message.replace(Regex("""\d+""")) { replacement("NUM") }
                val raw =
                    message.replaceText(
                        TextReplacementConfig
                            .builder()
                            .match(Pattern.compile("""\d+"""))
                            .replacement(replacementComponent as ComponentLike)
                            .build(),
                    )

                dsl shouldBe raw
            }

            "a literal matcher is not parsed as a regular expression" {
                val message = Component.text("cost: 1+1")

                val literal = message.replace("1+1") { replacement("2") }
                literal shouldHaveContent "cost: 2"

                val asRegex = message.replace(Regex("1+1")) { replacement("2") }
                asRegex shouldHaveContent "cost: 1+1"
            }

            "modify keeps the pre-populated matched text and adds a colour" {
                val message = Component.text("Hello world")

                val dsl = message.replace("world") { modify { color(NamedTextColor.RED) } }
                val raw =
                    message.replaceText(
                        TextReplacementConfig
                            .builder()
                            .matchLiteral("world")
                            .replacement { builder -> builder.color(NamedTextColor.RED) }
                            .build(),
                    )

                dsl shouldBe raw
                dsl shouldHaveContent "Hello world"
            }

            "content inside modify overwrites the pre-populated text" {
                val message = Component.text("Hello world")

                val dsl = message.replace("world") { modify { content("Kotlin") } }
                val raw =
                    message.replaceText(
                        TextReplacementConfig
                            .builder()
                            .matchLiteral("world")
                            .replacement { builder -> builder.content("Kotlin") }
                            .build(),
                    )

                dsl shouldBe raw
                dsl shouldHaveContent "Hello Kotlin"
            }

            "replacement(component) swaps in a prepared component for every match" {
                val message = Component.text("Hello world")
                val badge = Component.text("Kotlin").color(NamedTextColor.GOLD)

                val dsl = message.replace("world") { replacement(badge) }
                val raw =
                    message.replaceText(
                        TextReplacementConfig
                            .builder()
                            .matchLiteral("world")
                            .replacement(badge as ComponentLike)
                            .build(),
                    )

                dsl shouldBe raw
            }

            "a replacement block computes a translatable component directly from the match" {
                val message = Component.text("Hello role:admin")

                val dsl =
                    message.replace(Regex("""role:(\w+)""")) {
                        replacement { translatable("role.${match[1]}") }
                    }
                val raw =
                    message.replaceText(
                        TextReplacementConfig
                            .builder()
                            .match(Pattern.compile("""role:(\w+)"""))
                            .replacement { result, _ -> Component.translatable("role.${result.group(1)}") }
                            .build(),
                    )

                dsl shouldBe raw
            }

            "a replacement block's scoped remove deletes only the matching branch" {
                val message = Component.text("keep:a drop:b keep:c")

                val dsl =
                    message.replace(Regex("""(keep|drop):(\w)""")) {
                        replacement {
                            if (match[1] == "drop") remove else Component.text(match[2].orEmpty())
                        }
                    }
                val raw =
                    message.replaceText(
                        TextReplacementConfig
                            .builder()
                            .match(Pattern.compile("""(keep|drop):(\w)"""))
                            .replacement { result, _ ->
                                if (result.group(1) == "drop") null else Component.text(result.group(2))
                            }.build(),
                    )

                dsl shouldBe raw
                dsl shouldHaveContent "a  c"
            }

            "groups expose indexed and named captured text, with value and range" {
                lateinit var captured: TextMatch
                val content = "user: alex#42"
                val message = Component.text(content)

                message.replace(Regex("""(?<user>\w+)#(?<num>\d+)(?<extra>-x)?""")) {
                    replacement {
                        captured = match
                        Component.empty()
                    }
                }

                val start = content.indexOf("alex#42")
                captured.value shouldBe "alex#42"
                captured.range shouldBe (start until (start + "alex#42".length))
                captured.groups shouldBe listOf("alex#42", "alex", "42", null)
                captured[0] shouldBe "alex#42"
                captured[1] shouldBe "alex"
                captured["user"] shouldBe "alex"
                captured["num"] shouldBe "42"
                captured["extra"] shouldBe null
            }

            "an invalid group index or an unknown group name throws" {
                lateinit var captured: TextMatch
                Component.text("value").replace(Regex("value")) {
                    replacement {
                        captured = match
                        Component.empty()
                    }
                }

                shouldThrow<IndexOutOfBoundsException> { captured[5] }
                shouldThrow<IllegalArgumentException> { captured["missing"] }
            }

            "once and times equal the raw builder methods" {
                val message = Component.text("a a a")

                val dslOnce =
                    message.replace("a") {
                        once()
                        replacement("b")
                    }
                val rawOnce =
                    message.replaceText(
                        TextReplacementConfig
                            .builder()
                            .matchLiteral("a")
                            .once()
                            .replacement("b")
                            .build(),
                    )
                dslOnce shouldBe rawOnce

                val dslTimes =
                    message.replace("a") {
                        times(2)
                        replacement("b")
                    }
                val rawTimes =
                    message.replaceText(
                        TextReplacementConfig
                            .builder()
                            .matchLiteral("a")
                            .times(2)
                            .replacement("b")
                            .build(),
                    )
                dslTimes shouldBe rawTimes
            }

            "times rejects a non-positive count" {
                shouldThrow<IllegalArgumentException> {
                    Component.text("a").replace("a") {
                        times(0)
                        replacement("b")
                    }
                }
                shouldThrow<IllegalArgumentException> {
                    Component.text("a").replace("a") {
                        times(-1)
                        replacement("b")
                    }
                }
            }

            "a rejected times count leaves the match-limit slot free for a later valid call" {
                val message = Component.text("a a a")

                val dsl =
                    message.replace("a") {
                        shouldThrow<IllegalArgumentException> { times(0) }
                        times(2)
                        replacement("b")
                    }
                val raw =
                    message.replaceText(
                        TextReplacementConfig
                            .builder()
                            .matchLiteral("a")
                            .times(2)
                            .replacement("b")
                            .build(),
                    )

                dsl shouldBe raw
            }

            "condition selects skip for every match, leaving the component unchanged" {
                val message =
                    component {
                        text("a1 ")
                        text("a2 ")
                        text("a3")
                    }

                val dsl =
                    message.replace(Regex("""a\d""")) {
                        condition { skip }
                        replacement("X")
                    }

                dsl shouldBe message
            }

            "condition selects replace for every match" {
                val message =
                    component {
                        text("a1 ")
                        text("a2 ")
                        text("a3")
                    }

                val dsl =
                    message.replace(Regex("""a\d""")) {
                        condition { replace }
                        replacement("X")
                    }

                dsl shouldHaveContent "X X X"
            }

            "condition can replace an early match and stop before a later one, based on matchCount" {
                val message =
                    component {
                        text("a1 ")
                        text("a2 ")
                        text("a3")
                    }

                val dsl =
                    message.replace(Regex("""a\d""")) {
                        condition { if (matchCount == 1) replace else stop }
                        replacement("X")
                    }

                dsl shouldHaveContent "X a2 a3"
            }

            "condition counts skipped matches in matchCount but not in replacementCount" {
                val message = Component.text("a1 a2 a3 a4")

                val dsl =
                    message.replace(Regex("""a\d""")) {
                        condition {
                            when {
                                matchCount == 1 -> skip
                                replacementCount < 2 -> replace
                                else -> stop
                            }
                        }
                        replacement("X")
                    }

                dsl shouldHaveContent "a1 X X a4"
            }

            "remove deletes a whole-component match and a partial match" {
                val whole = Component.text("secret")
                whole.replace("secret") { remove() } shouldBe Component.empty()

                val partial = Component.text("keep secret please")
                partial.replace("secret") { remove() } shouldHaveContent "keep  please"
            }

            "insideHoverEvents controls whether hover text is rewritten" {
                val message = Component.text("info").hoverEvent(HoverEvent.showText(Component.text("secret")))

                val rewritten = message.replace("secret") { replacement("public") }
                rewritten shouldHaveHoverText Component.text("public")

                val untouched =
                    message.replace("secret") {
                        replacement("public")
                        insideHoverEvents(false)
                    }
                untouched shouldHaveHoverText Component.text("secret")
            }

            "rewrites matches inside children and inside translatable arguments" {
                val message =
                    component {
                        text("Hello %name%, ")
                        translatable("greeting.detail") {
                            arg(Component.text("%name%"))
                        }
                    }

                val dsl = message.replace("%name%") { replacement("Alex") }

                dsl shouldHaveContent "Hello Alex, "
                dsl.childAt(1).shouldHaveArguments(TranslationArgument.component(Component.text("Alex")))
            }

            "a component with no match is returned unchanged" {
                val message = Component.text("nothing to see here")

                val dsl = message.replace("missing") { replacement("found") }

                dsl shouldBe message
            }

            "two modify calls throw" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        modify { }
                        modify { }
                    }
                }
            }

            "modify followed by replacement(value) throws" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        modify { }
                        replacement("y")
                    }
                }
            }

            "modify followed by replacement(component) throws" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        modify { }
                        replacement(Component.text("y"))
                    }
                }
            }

            "modify followed by replacement(block) throws" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        modify { }
                        replacement { remove }
                    }
                }
            }

            "modify followed by remove throws" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        modify { }
                        remove()
                    }
                }
            }

            "two replacement calls throw" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        replacement("y")
                        replacement("z")
                    }
                }
            }

            "replacement followed by remove throws" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        replacement("y")
                        remove()
                    }
                }
            }

            "once followed by times throws" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        once()
                        times(2)
                        replacement("y")
                    }
                }
            }

            "once followed by condition throws" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        once()
                        condition { replace }
                        replacement("y")
                    }
                }
            }

            "times followed by condition throws" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        times(1)
                        condition { replace }
                        replacement("y")
                    }
                }
            }

            "two insideHoverEvents calls throw" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") {
                        insideHoverEvents(false)
                        insideHoverEvents(true)
                        replacement("y")
                    }
                }
            }

            "a missing replacement action throws" {
                shouldThrow<IllegalStateException> {
                    Component.text("x").replace("x") { once() }
                }
            }

            "outer ReplaceScope members do not resolve inside modify" {
                assertDoesNotCompile(
                    fileName = "ModifyScopeLeakTest.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.core.replacement.replace
                        import net.kyori.adventure.text.Component

                        fun shouldNotCompile() {
                            Component.text("x").replace("x") {
                                modify {
                                    once()
                                }
                            }
                        }
                        """.trimIndent(),
                    "cannot be called in this context with an implicit receiver",
                )
            }

            "outer ReplaceScope members do not resolve inside a replacement block" {
                assertDoesNotCompile(
                    fileName = "ReplacementScopeLeakTest.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.core.replacement.replace
                        import net.kyori.adventure.text.Component

                        fun shouldNotCompile() {
                            Component.text("x").replace("x") {
                                replacement {
                                    once()
                                    remove
                                }
                            }
                        }
                        """.trimIndent(),
                    "cannot be called in this context with an implicit receiver",
                )
            }

            "a replacement block that only styles the match does not compile" {
                assertDoesNotCompile(
                    fileName = "ReplacementBlockTypeTest.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.core.color.red
                        import io.github.lmliam.kotventure.core.replacement.replace
                        import net.kyori.adventure.text.Component

                        fun shouldNotCompile() {
                            Component.text("x").replace("x") {
                                replacement { color(red) }
                            }
                        }
                        """.trimIndent(),
                    "Unresolved reference 'color'",
                )
            }
        },
    )
