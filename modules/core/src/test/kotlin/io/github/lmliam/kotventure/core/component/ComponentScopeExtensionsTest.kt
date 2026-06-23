package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.shouldBeKeybindComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.mockk
import io.mockk.verify
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

class ComponentScopeExtensionsTest :
    StringSpec(
        {
            "nested component extensions emit through ComponentScope.append" {
                // A relaxed mock stands in for any ComponentScope, proving the extensions depend only on the
                // public append() contract rather than on the production ComponentBuilder.
                val scope = mockk<ComponentScope>(relaxed = true)
                val appended = mutableListOf<Component>()

                with(scope) {
                    text("Hello")
                    keybind("key.jump") { bold() }
                }

                verify { scope.append(capture(appended)) }
                appended shouldHaveSize 2
                appended[0] shouldContainText "Hello"
                appended[1].shouldBeKeybindComponent() shouldHaveDecoration TextDecoration.BOLD
            }
        },
    )
