package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SystemMessageDslTest :
    StringSpec(
        {
            "builds a bare system message without unsigned content" {
                val message = systemMessage("Server restarting soon")

                message.isSystem shouldBe true
                message.message() shouldBe "Server restarting soon"
                message.unsignedContent().shouldBeNull()
                message.canDelete() shouldBe false
            }

            "builds unsigned content from the block" {
                val message =
                    systemMessage("Server restarting soon") {
                        text("Server restarting ") { color(gold) }
                        text("soon")
                    }

                message.isSystem shouldBe true
                message.message() shouldBe "Server restarting soon"
                val content = message.unsignedContent().shouldNotBeNull()
                content shouldHaveChildCount 2
                content.childAt(0) shouldContainText "Server restarting "
                content.childAt(0) shouldHaveColor gold
                content.childAt(1) shouldContainText "soon"
            }
        },
    )
